package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.player.PlayerList.getPlayer
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack


class VikingProperties : KitProperties()

val Viking = Kit("Viking", ::VikingProperties) {
    displayMaterial = Material.IRON_AXE

    kitPlayerEvent<EntityDamageByEntityEvent>({ it.entity as? Player }) { it, player ->

        if (player.getItemInHand().getType() == (Material.DIAMOND_AXE)) {
            it.damage += 1.5;
        }
        if (player.getItemInHand().getType() == (Material.IRON_AXE)) {
            it.damage += 1.5;
        }
        if (player.getItemInHand().getType() == (Material.STONE_AXE)) {
            it.damage += 1.5;
        }

        if (player.getItemInHand().getType() == (Material.WOOD_AXE)) {
            it.damage += 1.5;
        }
    }
}