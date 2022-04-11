package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class SpiderProperties : KitProperties() {
    val effectDuration by int(3)
    val effectMultiplier by int(1)
    val probability by int(30)
}

val Spider = Kit("Spinne", ::SpiderProperties) {
    displayMaterial = Material.SPIDER_EYE

    kitPlayerEvent<EntityDamageByEntityEvent>({ it.damager as? Player }) { it, damager ->
        val target = (it.entity as? LivingEntity) ?: return@kitPlayerEvent
        if ((1..100).random() > this.kit.properties.probability) return@kitPlayerEvent
        target.addPotionEffect(
            PotionEffect(
                PotionEffectType.POISON,
                this.kit.properties.effectDuration * 20,
                this.kit.properties.effectMultiplier
            )
        )
    }
}
