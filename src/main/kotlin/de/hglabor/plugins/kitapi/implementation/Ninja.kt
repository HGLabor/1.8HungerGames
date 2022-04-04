package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.kitapi.cooldown.CooldownProperties
import de.hglabor.plugins.kitapi.cooldown.applyCooldown
import de.hglabor.plugins.kitapi.kit.Kit
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import net.axay.kspigot.runnables.taskRunLater
import net.axay.kspigot.utils.OnlinePlayerMap
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerToggleSneakEvent

class NinjaProperties : CooldownProperties(16000) {
    val maxDistance by int(30)
}

val Ninja = Kit("Ninja", ::NinjaProperties) {
    displayMaterial = Material.INK_SACK

    val lastDamaged = OnlinePlayerMap<Player?>()
    val lastDamagedTask = OnlinePlayerMap<KSpigotRunnable?>()

    kitPlayerEvent<PlayerToggleSneakEvent> {
        if (!it.player.isSneaking) return@kitPlayerEvent
        applyCooldown(it) {
            val toPlayer = lastDamaged[it.player]
            if (toPlayer == null || !toPlayer.isOnline || !toPlayer.hgPlayer.isAlive) {
                cancelCooldown()
            } else {
                if (it.player.location.distance(toPlayer.location) <= kit.properties.maxDistance)
                    it.player.teleport(toPlayer)
            }
        }
    }

    kitPlayerEvent<EntityDamageByEntityEvent>({ it.damager as? Player }) { it, player ->
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
