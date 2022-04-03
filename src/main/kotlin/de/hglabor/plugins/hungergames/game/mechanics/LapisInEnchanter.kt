package de.hglabor.plugins.hungergames.game.mechanics

import net.axay.kspigot.event.listen
import org.bukkit.DyeColor
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.EnchantingInventory
import org.bukkit.material.Dye


object LapisInEnchanter {
    fun register() {
        listen<InventoryOpenEvent> {
            if (it.inventory !is EnchantingInventory) return@listen
            it.inventory.setItem(1, Dye().apply { color = DyeColor.BLUE }.toItemStack(64))
        }
    }
}