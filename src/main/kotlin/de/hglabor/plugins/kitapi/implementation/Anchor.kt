package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.phase.GamePhase
import de.hglabor.plugins.hungergames.game.phase.phases.InvincibilityPhase
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import de.hglabor.plugins.kitapi.player.PlayerKits.hasKit
import net.axay.kspigot.runnables.taskRunLater
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector

class AnchorProperties : KitProperties() {
    val slownessDuration by int(3)
    val slownessAmplifier by int(0)
}

val Anchor = Kit("Anchor", ::AnchorProperties) {
    displayMaterial = Material.ANVIL
    description = "${ChatColor.GRAY}You neither deal nor take knockback"

    fun slownessEffect() = PotionEffect(PotionEffectType.SLOW, kit.properties.slownessDuration * 20, kit.properties.slownessAmplifier)


    kitPlayerEvent<EntityDamageByEntityEvent>({ it.damager as? Player }) { it, _ ->
        if (GameManager.phase == InvincibilityPhase) return@kitPlayerEvent
        val target = (it.entity as? LivingEntity) ?: return@kitPlayerEvent
        if (target is Player) {
            if (target.hasKit(Counter)) return@kitPlayerEvent
        }
        taskRunLater(delay = 1L) {
            target.velocity = Vector(0, 0, 0)
            target.addPotionEffect(slownessEffect())
        }
    }

    kitPlayerEvent<EntityDamageByEntityEvent>({ it.entity as? Player }) { it, player ->
        if (GameManager.phase == InvincibilityPhase) return@kitPlayerEvent
        val damager = it.entity as? LivingEntity ?: return@kitPlayerEvent
        if (damager is Player) {
            if (damager.hasKit(Counter)) return@kitPlayerEvent
        }
        taskRunLater(delay = 1L) {
            player.velocity = Vector(0, 0, 0)
            player.addPotionEffect(slownessEffect())
        }
    }
}
