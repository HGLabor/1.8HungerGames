package de.hglabor.plugins.hungergames.game.mechanics.implementation

import de.hglabor.plugins.hungergames.game.mechanics.Mechanic
import net.axay.kspigot.event.listen
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffectType


val DamageNerf by Mechanic("Damage Nerf") {
    displayMaterial = Material.STONE_SWORD

    fun isCritical(player: Player): Boolean {
        return player.fallDistance > 0.0f &&
                !player.isOnGround &&
                !player.isInsideVehicle &&
                !player.hasPotionEffect(PotionEffectType.BLINDNESS) && player.location.block.type != Material.LADDER && player.location.block.type != Material.VINE
    }

    mechanicEvent<EntityDamageByEntityEvent>(priority = EventPriority.LOW) {
        val damager = it.damager as? Player ?: return@mechanicEvent
        if (!(it.damager is Player && it.entity is Player)) return@mechanicEvent
        val itemName = damager.itemInHand.type.name.lowercase()
        if (itemName.endsWith("_sword") || itemName.endsWith("_axe"))
            it.damage *= if (isCritical(damager)) 0.5 else 0.65
    }
}