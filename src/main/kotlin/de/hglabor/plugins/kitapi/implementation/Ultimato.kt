package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.hungergames.utils.WorldUtils
import de.hglabor.plugins.hungergames.utils.hasMark
import de.hglabor.plugins.hungergames.utils.mark
import de.hglabor.plugins.hungergames.utils.unmark
import de.hglabor.plugins.kitapi.cooldown.CooldownManager
import de.hglabor.plugins.kitapi.cooldown.CooldownProperties
import de.hglabor.plugins.kitapi.cooldown.hasCooldown
import de.hglabor.plugins.kitapi.kit.Kit
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.extensions.bukkit.spawnCleanEntity
import net.axay.kspigot.extensions.geometry.add
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import org.bukkit.ChatColor
import org.bukkit.Effect
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class UltimatoProperties : CooldownProperties(45000) {
    val radius by int(13)
    val duration by int(35)
    val boostStrength by double(0.8)
}

class UltimatoInstance(private val ultimato: Player) {
    val properties = Ultimato.properties
    private var centerLocation: Location = ultimato.location.block.location.clone().apply { add(0, 2, 0) }
    private var tempEntity: ArmorStand =
        (centerLocation.clone().add(0, 100, 0).spawnCleanEntity(EntityType.ARMOR_STAND) as ArmorStand).apply {
            isVisible = false
            isSmall = true
        }

    val players = tempEntity
        .getNearbyEntities(properties.radius.toDouble(), 256.0, properties.radius.toDouble())
        .filterIsInstance<Player>().filterNot { it.isInGladiator }.onEach { it.mark("inUltimato") }

    var particleTask: KSpigotRunnable? = null
    var arenaTask: KSpigotRunnable? = null

    init {
        createArena()
        startTimer()
    }

    private fun startTimer() {
        var count = 0
        arenaTask = task(true, 2, 2) {
            if (players.any { p -> !p.isOnline || !p.hgPlayer.isAlive }) {
                endFight()
                return@task
            }
            if (count / 10 >= properties.duration) {
                endFight()
                return@task
            }

            val radius = properties.radius
            val strength = properties.boostStrength

            for (entity in tempEntity.getNearbyEntities(radius + 0.5, 256.0, radius + 0.5)) {
                if ((entity as? Player ?: return@task).isInGladiator) continue
                // TODO radius ist ein viereck sollte aber ein kreis sein aber ka wie :<
                    if (entity.location.distance(centerLocation) >= radius) {
                        val direction = centerLocation.toVector().subtract(entity.location.toVector()).normalize()
                        if (entity in players) {
                            entity.velocity = direction.multiply(strength)
                        } else {
                            entity.velocity = direction.multiply(-(strength / 2.5))
                        }
                    }
            }

            count++
        }

        particleTask = task(true, 10, 10) {
            createArena()
        }
    }

    private fun createArena() {
        WorldUtils.makeCircle(centerLocation.clone().add(0, -3, 0), properties.radius, 12, true, false)
            .forEach {
                it.world.playEffect(it, Effect.COLOURED_DUST, 3)
            }
    }

    private fun endFight() {
        particleTask?.cancel()
        particleTask = null
        arenaTask?.cancel()
        arenaTask = null
        tempEntity.remove()
        players.forEach {
            it.unmark("inUltimato")
        }
        if (ultimato.hgPlayer.isAlive) {
            CooldownManager.addCooldown(properties.cooldownInstance, ultimato)
        }
    }
}


val Ultimato by Kit("Ultimato", ::UltimatoProperties) {
    displayItem = ItemStack(Material.STAINED_GLASS_PANE, 1, 14)
    description = "${ChatColor.GRAY}Create an arena to fight nearby players"

    clickableItem(ItemStack(Material.STAINED_GLASS_PANE, 1, 14), useInInvincibility = false) {
        if (it.player.isInUltimato || it.player.isInGladiator) return@clickableItem
        val radius = kit.properties.radius
        if (it.player.getNearbyEntities(radius.toDouble(), 128.0, radius.toDouble())
                .filterIsInstance<Player>()
                .filterNot { it.isInGladiator || it.isInUltimato }
                .isEmpty()
        ) {
            it.player.sendMessage("${Prefix}There are no Players nearby.")
            return@clickableItem
        }
        if (!hasCooldown(it.player)) {
            UltimatoInstance(it.player)
        }
    }
}

val Player.isInUltimato: Boolean get() = hasMark("inUltimato")