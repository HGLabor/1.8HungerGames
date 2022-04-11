package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.phase.phases.InvincibilityPhase
import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.hungergames.utils.ChanceUtils
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent

class MagmaProperties : KitProperties() {
    val fireTicks by int(5)
    val likelihood by int(25)
}

val Magma = Kit("Magma", ::MagmaProperties) {
    displayMaterial = Material.FIREBALL

    kitPlayerEvent<EntityDamageEvent>({ it.entity as? Player }) { it, player ->
        val isFireDamage = when (it.cause) {
            EntityDamageEvent.DamageCause.LAVA,
            EntityDamageEvent.DamageCause.FIRE,
            EntityDamageEvent.DamageCause.FIRE_TICK -> true
            else -> false
        }

        if (isFireDamage) {
            it.isCancelled = true
        }
    }

    kitPlayerEvent<EntityDamageByEntityEvent>({ it.damager as? Player }, EventPriority.HIGH, false) { it, damager ->
        if (it.isCancelled) return@kitPlayerEvent
        val entity = it.entity
        if (ChanceUtils.roll(kit.properties.likelihood)) {
            if (entity is Player) {
                if (!entity.hgPlayer.isAlive) return@kitPlayerEvent
            }

            entity.fireTicks = kit.properties.fireTicks * 20
        }
    }
}
