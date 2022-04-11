package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.utils.ChanceUtils
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class SnailProperties : KitProperties() {
    val effectDuration by int(3)
    val effectMultiplier by int(0)
    val probability by int(30)
}

val Snail = Kit("Snail", ::SnailProperties) {
    displayMaterial = Material.SLIME_BALL

    kitPlayerEvent<EntityDamageByEntityEvent>({ it.damager as? Player }, priority = EventPriority.HIGH) { it, damager ->
        if (damager.isSneaking) {
            it.damage = it.finalDamage.coerceAtMost(1.0)
        }

        if (!ChanceUtils.roll(kit.properties.probability)) return@kitPlayerEvent
        val target = (it.entity as? LivingEntity) ?: return@kitPlayerEvent

        target.addPotionEffect(
            PotionEffect(
                PotionEffectType.SLOW,
                this.kit.properties.effectDuration * 20,
                this.kit.properties.effectMultiplier
            )
        )
    }

    kitPlayerEvent<EntityDamageByEntityEvent>({ it.entity as? Player }, priority = EventPriority.HIGH) { it, player ->
        if (player.isSneaking) {
            it.damage = it.finalDamage.coerceAtMost(1.0)
        }
    }
}
