package de.hglabor.plugins.hungergames.game.mechanics

import net.axay.kspigot.event.listen
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.EnchantingInventory
import org.bukkit.material.Dye


object LapisInEnchanter {
    fun register() {
        listen<InventoryOpenEvent> {
            if (it.inventory !is EnchantingInventory) return@listen
            it.inventory.setItem(1, Dye().apply { color = DyeColor.BLUE }.toItemStack(64))
        }

        listen<InventoryClickEvent> {
            if (it.clickedInventory !is EnchantingInventory) return@listen
            if (it.currentItem.type != Material.INK_SACK) return@listen
            it.isCancelled = true
        }

        listen<InventoryCloseEvent> {
            if (it.inventory !is EnchantingInventory) return@listen
            it.inventory.setItem(1, null)
        }
    }
}