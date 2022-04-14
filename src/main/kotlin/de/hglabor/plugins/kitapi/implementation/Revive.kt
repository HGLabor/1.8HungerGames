package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.kitapi.cooldown.CooldownProperties
import de.hglabor.plugins.kitapi.cooldown.applyCooldown
import de.hglabor.plugins.kitapi.kit.Kit
import net.axay.kspigot.extensions.geometry.add
import net.axay.kspigot.runnables.taskRunLater
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Effect
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.cos
import kotlin.math.sin


class ReviveProperties : CooldownProperties(50000)

val Revive = Kit("Revive", ::ReviveProperties) {
    displayMaterial = Material.GOLDEN_APPLE

    kitPlayerEvent<EntityDamageEvent>({ it.entity as? Player }) { it, player ->
        if (player.health - it.finalDamage <= 0.0) {
            applyCooldown(player) {
                it.isCancelled = true
                player.addPotionEffects(
                    listOf(
                        PotionEffect(PotionEffectType.REGENERATION, 10, 9),
                        PotionEffect(PotionEffectType.ABSORPTION, 100, 1),
                        PotionEffect(PotionEffectType.FIRE_RESISTANCE, 800, 0)
                    )
                )
                taskRunLater(10) {
                    PotionEffect(PotionEffectType.REGENERATION, 790, 2)
                }

                val scaleX = 1
                val scaleY = 1
                val density = 1.0
                var i = 0.0
                player.world.playSound(player.location, Sound.ANVIL_BREAK, 3f, 1f)
                while (i < 2 * Math.PI) {
                    val x = cos(i) * scaleX
                    val y = sin(i) * scaleY
                    player.world.playEffect(player.location.clone().add(x, 0, y), Effect.HAPPY_VILLAGER, 15)
                    i += density
                }
            }
        }
    }
}
