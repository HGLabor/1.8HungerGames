package de.hglabor.plugins.hungergames.game.mechanics

import net.axay.kspigot.event.listen
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent

object DamageNerf {
    fun register() {
        listen<EntityDamageByEntityEvent> {
            if (!(it.damager is Player && it.entity is Player)) return@listen
            val itemName = (it.damager as Player).itemInHand.type.name.lowercase()
            if (itemName.endsWith("_sword") || itemName.endsWith("_axe"))
                it.damage *= 0.65
        }
    }
}