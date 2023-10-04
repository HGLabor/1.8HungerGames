package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.hungergames.utils.ChanceUtils
import de.hglabor.plugins.hungergames.utils.WorldUtils
import de.hglabor.plugins.hungergames.utils.hasMark
import de.hglabor.plugins.hungergames.utils.mark
import de.hglabor.plugins.kitapi.cooldown.CooldownProperties
import de.hglabor.plugins.kitapi.cooldown.applyCooldown
import de.hglabor.plugins.kitapi.cooldown.hasCooldown
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.isKitItem
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.actionBar
import net.axay.kspigot.runnables.taskRunLater
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector

class SpiderProperties : CooldownProperties(20) {
    val poisonDuration by int(3)
    val poisonAmplifier by int(1)
    val likelihood by int(30)

    val cobwebRadius by int(5)
    val cobwebHeight by int(5)
    val climbVelocity by double(0.3)
}

val Spider by Kit("Spider", ::SpiderProperties) {
    displayMaterial = Material.SPIDER_EYE
    description {
        +"${ChatColor.WHITE}Hit ${ChatColor.GRAY}your enemy to poison them"
        +"${ChatColor.WHITE}Hold your kit-item ${ChatColor.GRAY}to climb walls"
        +"${ChatColor.WHITE}Throw your kit-item ${ChatColor.GRAY}to create a sphere of cobwebs"
    }

    val spiderSnowball = "spidersb"

    // Viper ability
    kitPlayerEvent<EntityDamageByEntityEvent>({ it.damager as? Player }) { it, _ ->
        val target = (it.entity as? LivingEntity) ?: return@kitPlayerEvent
        if (!ChanceUtils.roll(kit.properties.likelihood)) return@kitPlayerEvent
        target.addPotionEffect(
            PotionEffect(
                PotionEffectType.POISON,
                this.kit.properties.poisonDuration * 20,
                this.kit.properties.poisonAmplifier
            )
        )
    }

    // Spiderman abilities
    clickableItem(ItemStack(Material.WEB), useInInvincibility = false) {
        applyCooldown(it) {
            val player = it.player

            val snowball = player.throwSnowball()
            snowball.mark(spiderSnowball)
        }
    }

    fun createSpiderNet(startLocation: Location): Set<Block> {
        val result: MutableSet<Block> = HashSet()
        val p = kit.properties
        for (location in WorldUtils.makeCircle(startLocation, p.cobwebRadius, p.cobwebHeight, true, true)) {
            if (location.block.type != Material.AIR) {
                continue
            }
            result.add(location.block)
            location.block.type = Material.WEB
        }
        return result
    }

    listen<ProjectileHitEvent> {
        val snowball = it.entity as? Snowball ?: return@listen
        if (!snowball.hasMark(spiderSnowball)) return@listen
        val spiderNet = createSpiderNet(snowball.location)
        taskRunLater(15 * 20) {
            for (block in spiderNet) {
                if (block.type == Material.WEB) {
                    block.type = Material.AIR
                }
            }
        }
        snowball.remove()
    }

    fun nearWall(distance: Double, player: Player): Boolean {
        val location = player.location
        val surroundingLocs = setOf(
            location.clone().add(distance, 1.0, 0.0),
            location.clone().add(-distance, 1.0, 0.0),
            location.clone().add(0.0, 1.0, +distance),
            location.clone().add(0.0, 1.0, -distance)
        )
        return surroundingLocs.any { loc -> loc.block.type.isSolid }
    }

    kitPlayerEvent<PlayerMoveEvent>( { it.player }) { _, player ->
        if (!player.hgPlayer.isAlive) return@kitPlayerEvent
        if (!player.itemInHand.isKitItem  || player.itemInHand.type != Material.WEB) return@kitPlayerEvent

        // Fly in cobweb
        /*
        val blockInHead = player.eyeLocation.block
        val blockInFeet = player.location.block
        if (player.gameMode == GameMode.SURVIVAL) {
            if (blockInHead.type == Material.WEB || blockInFeet.type == Material.WEB) {
                broadcast("in web")
                player.allowFlight = true
                player.isFlying = true
            } else {
                player.allowFlight = false
                player.isFlying = false
            }
        }*/

        // CLIMB WALLS
        if (nearWall(0.5, player)) {
            if (hasCooldown(player)) {
                player.actionBar("${ChatColor.GRAY}Can't climb while on cooldown")
                return@kitPlayerEvent
            }
            player.velocity = Vector(0.0, kit.properties.climbVelocity, 0.0)
        }
    }
}
