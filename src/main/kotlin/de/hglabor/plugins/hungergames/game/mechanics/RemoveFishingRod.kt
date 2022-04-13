package de.hglabor.plugins.hungergames.game.mechanics

import net.axay.kspigot.event.listen
import org.bukkit.inventory.Recipe
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.CraftItemEvent

object RemoveFishingRod {
    fun register() {
        listen<CraftItemEvent> {
            if (it.getRecipe().getResult().getType() == Material.FISHING_ROD) it.isCancelled = true;
        }
    }
}


//@EventHandler
//Player player = (Player) event.getWhoClicked();
//
//if(event.getRecipe().getResult().getType() == Material.FISHING_ROD)
//event.setCancelled(true);