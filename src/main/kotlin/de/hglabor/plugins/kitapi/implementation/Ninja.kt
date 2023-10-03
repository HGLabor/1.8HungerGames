package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.phase.phases.LobbyPhase
import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.kitapi.cooldown.CooldownProperties
import de.hglabor.plugins.kitapi.cooldown.applyCooldown
import de.hglabor.plugins.kitapi.kit.Kit
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import net.axay.kspigot.utils.OnlinePlayerMap
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import kotlin.math.cos
import kotlin.math.sin

class NinjaProperties : CooldownProperties(10000) {
    val maxDistance by int(30)
}

val Ninja = Kit("Ninja", ::NinjaProperties) {
    displayMaterial = Material.EMERALD
    description = "${ChatColor.GRAY}Sneak to teleport behind your latest enemy"

    val lastDamaged = OnlinePlayerMap<Player?>()
    val lastDamagedTask = OnlinePlayerMap<KSpigotRunnable?>()

    kitPlayerEvent<PlayerToggleSneakEvent> {
        if (GameManager.phase == LobbyPhase) return@kitPlayerEvent
        if (!it.player.isSneaking) return@kitPlayerEvent
        applyCooldown(it) {
            val toPlayer = lastDamaged[it.player]
            if (toPlayer == null || !toPlayer.isOnline || !toPlayer.hgPlayer.isAlive) {
                cancelCooldown()
            } else {
                val inReach = it.player.distanceTo(toPlayer) <= kit.properties.maxDistance * kit.properties.maxDistance
                if (inReach) it.player.teleport(calculateNinjaBehind(toPlayer))
                else cancelCooldown()
            }
        }
    }

    kitPlayerEvent<EntityDamageByEntityEvent>({ it.damager as? Player }, EventPriority.HIGH) { it, player ->
        if (GameManager.phase == LobbyPhase) return@kitPlayerEvent
        if (!it.isCancelled) {
            lastDamaged[player] = it.entity as? Player ?: return@kitPlayerEvent
            lastDamagedTask[player]?.cancel()

            var timer = 30
            lastDamagedTask[player] = task(false, 20, 20) {
                if (--timer == 0) {
                    lastDamaged[player] = null
                    lastDamagedTask[player] = null
                    it.cancel()
                }
            }
        }
    }
}

private fun calculateNinjaBehind(entity: Entity): Location {
    var nang: Float = entity.location.yaw + 90
    if (nang < 0) nang += 360f
    val nX = cos(Math.toRadians(nang.toDouble()))
    val nZ = sin(Math.toRadians(nang.toDouble()))
    return entity.location.clone().subtract(nX, 0.0, nZ)
}

private fun Entity.distanceTo(entity: Entity): Double {
    val ninjaLocation: Location = this.location.clone()
    val entityLocation: Location = entity.location.clone()
    ninjaLocation.y = 0.0
    entityLocation.y = 0.0
    return ninjaLocation.distanceSquared(entityLocation)
}
