package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.utils.ChanceUtils
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import net.axay.kspigot.event.listen
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionEffectType.WATER_BREATHING
import org.bukkit.potion.PotionType

class SquidProperties : KitProperties() {
    val effectDuration by int(3)
    val effectMultiplier by int(0)
    val probability by int(30)
}

val Squid = Kit("Squid", ::SquidProperties) {
    displayMaterial = Material.INK_SACK

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

kitPlayerEvent<PlayerJoinEvent> {
    it.player.addPotionEffect(PotionEffect(PotionEffectType.WATER_BREATHING, 1000000000, 10000))
}

}






