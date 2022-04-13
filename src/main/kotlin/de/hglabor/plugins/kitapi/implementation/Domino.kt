package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.util.Vector

class DominoProperties : KitProperties() {
    val radius by double(4.0)
}

val Domino = Kit("Domino", ::DominoProperties) {
    displayMaterial = Material.QUARTZ_BLOCK

    kitPlayerEvent<EntityDamageByEntityEvent>(
        { it.damager as? Player },
        priority = EventPriority.HIGHEST
    ) { it, player ->
        val target = it.entity
        val r = kit.properties.radius
        target.getNearbyEntities(r, r, r).forEach { nearby ->
            if (nearby !is LivingEntity) return@forEach
            if (nearby == target || nearby == player) return@forEach
            nearby.damage(it.damage)
            nearby.velocity = nearby.velocity.add(
                nearby.location.toVector().subtract(player.location.toVector().add(Vector(0.0, 0.8, 0.0))).normalize().multiply(1.2)
            )
        }
    }
}