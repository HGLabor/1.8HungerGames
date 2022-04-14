package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.kitapi.cooldown.CooldownProperties
import de.hglabor.plugins.kitapi.cooldown.applyCooldown
import de.hglabor.plugins.kitapi.kit.Kit
import org.bukkit.Bukkit
import org.bukkit.Effect
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent


class ReviveProperties : CooldownProperties(50000)

val Revive = Kit("Revive", ::ReviveProperties) {
    displayMaterial = Material.GOLDEN_APPLE

    kitPlayerEvent<EntityDamageEvent>({ it.entity as? Player }) { it, player ->
        if (player.health - it.finalDamage <= 0.0) {
            applyCooldown(player) {
                it.isCancelled = true
                val scaleX = 1
                val scaleY = 1
                val density = 1.0
                var i = 0.0
                while (i < 2 * Math.PI) {
                    val x = Math.cos(i) * scaleX
                    val y = Math.sin(i) * scaleY
                    player.playEffect(player.location, Effect.LARGE_SMOKE, 15)
                    player.playSound(player.location, Sound.ANVIL_BREAK, 3f, 1f)
                    i += density
                }
            }
        }
    }
}
