package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.phase.phases.InvincibilityPhase
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

class SpiderProperties : CooldownProperties(20000) {
    val effectDuration by int(3)
    val effectMultiplier by int(1)
    val probability by int(30)

    val spidernetRadius by int(5)
    val spidernetHeight by int(5)
    val climbVelocity by double(0.3)
}

val Spider = Kit("Spider", ::SpiderProperties) {
    displayMaterial = Material.SPIDER_EYE
    val spiderSnowball = "spidersb"

    // Viper ability
    kitPlayerEvent<EntityDamageByEntityEvent>({ it.damager as? Player }) { it, damager ->
        val target = (it.entity as? LivingEntity) ?: return@kitPlayerEvent
        if (!ChanceUtils.roll(kit.properties.probability)) return@kitPlayerEvent
        target.addPotionEffect(
            PotionEffect(
                PotionEffectType.POISON,
                this.kit.properties.effectDuration * 20,
                this.kit.properties.effectMultiplier
            )
        )
    }

    // Spiderman abilities
    clickableItem(ItemStack(Material.WEB)) {
        if (GameManager.phase == InvincibilityPhase) {
            it.player.sendMessage("${Prefix}You can't use this kit while during the grace period.")
            return@clickableItem
        }
        applyCooldown(it) {
            val player = it.player

            val snowball = player.throwSnowball()
            snowball.mark(spiderSnowball)
        }
    }

    fun createSpiderNet(startLocation: Location): Set<Block> {
        val result: MutableSet<Block> = HashSet()
        val p = kit.properties
        for (location in WorldUtils.makeCircle(startLocation, p.spidernetRadius, p.spidernetHeight, true, true)) {
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
        val loc = player.location
        val surroundingLocs = setOf(
            loc.clone().add(distance, 1.0, 0.0),
            loc.clone().add(-distance, 1.0, 0.0),
            loc.clone().add(0.0, 1.0, +distance),
            loc.clone().add(0.0, 1.0, -distance)
        )
        return surroundingLocs.any { loc -> loc.block.type.isSolid }
    }

    kitPlayerEvent<PlayerMoveEvent>( { it.player }) { event, player ->
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
