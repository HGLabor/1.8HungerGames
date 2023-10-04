package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.phase.phases.InvincibilityPhase
import de.hglabor.plugins.hungergames.utils.ChanceUtils.roll
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import de.hglabor.plugins.kitapi.player.PlayerKits.hasKit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType


class CannibalProperties : KitProperties() {
    val likelihood by int(33)
    val hungerAmplifier by int(1)
    val hungerDuration by int(3)
}

val Cannibal by Kit("Cannibal", ::CannibalProperties) {
    displayMaterial = Material.ROTTEN_FLESH
    description {
        +"${ChatColor.WHITE}Hitting ${ChatColor.GRAY}players feeds you."
        +"${ChatColor.WHITE}Hit ${ChatColor.GRAY}players get hunger effect."
    }

    fun slownessEffect() = PotionEffect(PotionEffectType.HUNGER, kit.properties.hungerDuration * 20, kit.properties.hungerAmplifier)


    kitPlayerEvent<EntityDamageByEntityEvent>({ it.damager as? Player }) { it, player ->
        if (GameManager.phase == InvincibilityPhase) return@kitPlayerEvent
        val target = (it.entity as? Player) ?: return@kitPlayerEvent
        if (target.hasKit(Counter)) return@kitPlayerEvent

        val cannibalFoodLevel: Int = player.foodLevel
        if (cannibalFoodLevel < 20) {
            val difference: Int = 20 - cannibalFoodLevel
            target.foodLevel -= difference
            player.foodLevel += difference
            player.saturation += difference
        }

        if (roll(kit.properties.likelihood)) {
            target.addPotionEffect(slownessEffect())
        }
    }
}
