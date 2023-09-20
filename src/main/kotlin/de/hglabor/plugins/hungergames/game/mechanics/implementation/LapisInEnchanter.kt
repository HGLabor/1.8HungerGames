package de.hglabor.plugins.hungergames.game.mechanics.implementation

import de.hglabor.plugins.hungergames.game.mechanics.Mechanic
import net.axay.kspigot.event.listen
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.EnchantingInventory
import org.bukkit.material.Dye

val LapisInEnchanter = Mechanic("Lapis in Enchanter") {
    displayMaterial = Material.LAPIS_ORE

    mechanicEvent<InventoryOpenEvent> {
        if (it.inventory !is EnchantingInventory) return@mechanicEvent
        it.inventory.setItem(1, Dye().apply { color = DyeColor.BLUE }.toItemStack(64))
    }

    mechanicEvent<InventoryClickEvent> {
        if (it.clickedInventory !is EnchantingInventory) return@mechanicEvent
        if (it.currentItem.type != Material.INK_SACK) return@mechanicEvent
        it.isCancelled = true
    }

    mechanicEvent<InventoryCloseEvent> {
        if (it.inventory !is EnchantingInventory) return@mechanicEvent
        it.inventory.setItem(1, null)
    }

}