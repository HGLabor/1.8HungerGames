package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.event.KitDisableEvent
import de.hglabor.plugins.hungergames.event.KitEnableEvent
import de.hglabor.plugins.hungergames.utils.ChanceUtils
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import net.axay.kspigot.runnables.taskRunLater
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType


class ShabbyProperties : KitProperties() {
    val effectDuration by int(3)
    val effectMultiplier by int(0)
    val probability by int(8)
}

val Shabby = Kit("Shabby", ::ShabbyProperties) {
    displayMaterial = Material.FERMENTED_SPIDER_EYE

    kitPlayerEvent<EntityDamageByEntityEvent>({ it.damager as? Player }) { it, damager ->
        val target = (it.entity as? LivingEntity) ?: return@kitPlayerEvent
        if (!ChanceUtils.roll(kit.properties.probability)) return@kitPlayerEvent
        target.addPotionEffect(
            PotionEffect(
                PotionEffectType.BLINDNESS,
                this.kit.properties.effectDuration * 20,
                this.kit.properties.effectMultiplier
            )
        )
    }
    kitPlayerEvent<EntityDamageByEntityEvent>({ it.damager as? Player }) { it, damager ->
        val target = (it.entity as? LivingEntity) ?: return@kitPlayerEvent
        if (!ChanceUtils.roll(kit.properties.probability)) return@kitPlayerEvent
        target.addPotionEffect(
            PotionEffect(
                PotionEffectType.SLOW,
                this.kit.properties.effectDuration * 20,
                this.kit.properties.effectMultiplier
            )
        )
    }
    kitPlayerEvent<EntityDamageByEntityEvent>({ it.damager as? Player }) { it, damager ->
        val target = (it.entity as? LivingEntity) ?: return@kitPlayerEvent
        if (!ChanceUtils.roll(kit.properties.probability)) return@kitPlayerEvent
        target.addPotionEffect(
            PotionEffect(
                PotionEffectType.POISON,
                this.kit.properties.effectDuration * 20,
                this.kit.properties.effectMultiplier
            )
        )
    }
    kitPlayerEvent<EntityDamageByEntityEvent>({ it.damager as? Player }) { it, damager ->
        val target = (it.entity as? LivingEntity) ?: return@kitPlayerEvent
        if (!ChanceUtils.roll(kit.properties.probability)) return@kitPlayerEvent
        target.addPotionEffect(
            PotionEffect(
                PotionEffectType.WEAKNESS,
                this.kit.properties.effectDuration * 20,
                this.kit.properties.effectMultiplier
            )
        )
    }
    kitPlayerEvent<EntityDamageByEntityEvent>({ it.damager as? Player }) { it, damager ->
        val target = (it.entity as? LivingEntity) ?: return@kitPlayerEvent
        if (!ChanceUtils.roll(kit.properties.probability)) return@kitPlayerEvent
        target.addPotionEffect(
            PotionEffect(
                PotionEffectType.WITHER,
                this.kit.properties.effectDuration * 20,
                this.kit.properties.effectMultiplier
            )
        )
    }
}



