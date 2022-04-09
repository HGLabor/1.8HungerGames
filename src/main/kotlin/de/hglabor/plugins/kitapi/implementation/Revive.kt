package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.kitapi.cooldown.CooldownProperties
import de.hglabor.plugins.kitapi.cooldown.applyCooldown
import de.hglabor.plugins.kitapi.kit.Kit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent


class ReviveProperties : CooldownProperties(50000)

val Revive = Kit("Revive", ::ReviveProperties) {
    displayMaterial = Material.GOLDEN_APPLE

    kitPlayerEvent<EntityDamageEvent>({ it.entity as? Player }) { it, player ->
        if (player.health - it.finalDamage <= 0.0) {
            applyCooldown(player) {
                it.isCancelled = true
                // TODO cool animation or sound?
            }
        }
    }
}






