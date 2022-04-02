package de.hglabor.plugins.hungergames.game

import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.entity.Player
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

class Soup : Listener {
    @EventHandler
    fun onSoup(event: PlayerInteractEvent) {
        val p = event.player
        if (p.inventory.getItemInMainHand().getType() === Material.MUSHROOM_STEW) {
            if (p.health < p.maxHealth) {
                var health = p.health * 3.5
                if (health > p.maxHealth) {
                    health = p.maxHealth
                }
                p.health = health
                p.inventory.setItemInMainHand(ItemStack(Material.BOWL))
            }
        }
    }
}