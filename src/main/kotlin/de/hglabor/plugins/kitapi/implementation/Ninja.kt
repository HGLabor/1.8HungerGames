package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.kitapi.cooldown.CooldownProperties
import de.hglabor.plugins.kitapi.cooldown.applyCooldown
import de.hglabor.plugins.kitapi.kit.Kit
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

    val lastDamaged = OnlinePlayerMap<Player>()
    kitPlayerEvent<PlayerToggleSneakEvent> {
        if (!it.player.isSneaking) return@kitPlayerEvent
        applyCooldown(it) {
            val toPlayer = lastDamaged[it.player]
            if (toPlayer == null || !toPlayer.isOnline) {
                cancelCooldown()
            } else {
                if (it.player.location.distance(toPlayer.location) <= kit.properties.maxDistance)
                    it.player.teleport(toPlayer)
            }
        }
    }

    kitPlayerEvent<EntityDamageByEntityEvent>({ it.damager as? Player }) { it, player ->
        lastDamaged[player] = it.entity as? Player ?: return@kitPlayerEvent
    }
}
