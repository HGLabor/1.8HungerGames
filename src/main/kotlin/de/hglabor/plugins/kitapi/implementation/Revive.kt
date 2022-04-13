package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.kitapi.cooldown.CooldownProperties
import de.hglabor.plugins.kitapi.cooldown.applyCooldown
import de.hglabor.plugins.kitapi.kit.Kit
import net.axay.kspigot.runnables.taskRunLater
import org.bukkit.Effect
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType


class ReviveProperties : CooldownProperties(50000)

val Revive = Kit("Revive", ::ReviveProperties) {
    displayMaterial = Material.GOLDEN_APPLE

    kitPlayerEvent<EntityDamageEvent>({ it.entity as? Player }) { it, player ->
        if (player.health - it.finalDamage <= 0.0) {
            applyCooldown(player) {
                it.isCancelled = true
                // TODO cool animation or sound?
                player.addPotionEffects(
                    listOf(
                        PotionEffect(PotionEffectType.ABSORPTION, 100, 2),
                        PotionEffect(PotionEffectType.REGENERATION, 10, 4),
                        PotionEffect(PotionEffectType.FIRE_RESISTANCE, 800, 0)
                    )
                )
                taskRunLater(10) {
                    PotionEffect(PotionEffectType.REGENERATION, 900, 2)
                }
            }
        }
    }
}






