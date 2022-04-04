package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import de.hglabor.plugins.kitapi.player.PlayerKits.hasKit
import net.axay.kspigot.runnables.taskRunLater
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.util.Vector

class AnchorProperties : KitProperties()

val Anchor = Kit("Anchor", ::AnchorProperties) {
    displayMaterial = Material.ANVIL

    kitPlayerEvent<EntityDamageByEntityEvent>({ it.damager as? Player }) { it, _ ->
        val target = (it.entity as? LivingEntity) ?: return@kitPlayerEvent
        if (target is Player) {
            if (target.hasKit(Counter)) return@kitPlayerEvent
        }
        taskRunLater(delay = 1L) {
            target.velocity = Vector(0, 0, 0)
        }
    }

    kitPlayerEvent<EntityDamageByEntityEvent>({ it.entity as? Player }) { it, player ->
        val damager = it.entity as? LivingEntity ?: return@kitPlayerEvent
        if (damager is Player) {
            if (damager.hasKit(Counter)) return@kitPlayerEvent
        }
        taskRunLater(delay = 1L) {
            player.velocity = Vector(0, 0, 0)
        }
    }
}
