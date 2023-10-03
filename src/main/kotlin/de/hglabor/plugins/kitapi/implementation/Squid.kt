package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.event.KitDisableEvent
import de.hglabor.plugins.hungergames.event.KitEnableEvent
import de.hglabor.plugins.hungergames.utils.ChanceUtils
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import net.axay.kspigot.runnables.taskRunLater
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class SquidProperties : KitProperties() {
    val effectDuration by int(3)
    val effectMultiplier by int(0)
    val probability by int(8)
}

val Squid by Kit("Squid", ::SquidProperties) {
    displayMaterial = Material.INK_SACK
    description {
        +"${ChatColor.WHITE}Hit ${ChatColor.GRAY}an enemy to blind them"
        +"${ChatColor.GRAY}You receive ${ChatColor.WHITE}permanent water-breathing"
    }

    kitPlayerEvent<EntityDamageByEntityEvent>({ it.damager as? Player }, priority = EventPriority.HIGH) { it, _ ->
        if (it.isCancelled) return@kitPlayerEvent
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

    kitPlayerEvent<KitEnableEvent> {
        it.player.addPotionEffect(PotionEffect(PotionEffectType.WATER_BREATHING, Int.MAX_VALUE, 200))
    }

    kitPlayerEvent<KitDisableEvent> {
        it.player.removePotionEffect(PotionEffectType.WATER_BREATHING)
    }

    kitPlayerEvent<PlayerItemConsumeEvent> {
        if (it.item.type == Material.MILK_BUCKET) {
            taskRunLater(1) {
                it.player.addPotionEffect(PotionEffect(PotionEffectType.WATER_BREATHING, Int.MAX_VALUE, 200))
            }
        }
    }
}






