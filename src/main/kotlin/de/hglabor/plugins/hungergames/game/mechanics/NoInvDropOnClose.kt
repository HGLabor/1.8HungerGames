package de.hglabor.plugins.hungergames.game.mechanics

import de.hglabor.plugins.hungergames.game.mechanics.recraft.RecraftNerf
import de.hglabor.plugins.hungergames.game.mechanics.recraft.isRecraftMaterial
import de.hglabor.plugins.hungergames.game.mechanics.recraft.recraftComponents
import net.axay.kspigot.event.listen
import net.axay.kspigot.items.setMeta
import org.bukkit.Material
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import java.util.UUID

// todo: enable as setting
object NoInvDropOnClose {
    private val DELETE_MARKER = UUID.randomUUID().toString()

    fun register() {
        listen<InventoryCloseEvent> { e ->
            val cursorStack = e.view.cursor
            val playerInv = e.player.inventory
            if (cursorStack != null && cursorStack.itemMeta != null && cursorStack.type != Material.AIR) {
                playerInv.addItemProcessed(cursorStack)
            }

            val topInv = e.view.topInventory
            fun processContents(indexToIgnore: Int) { // indexToIgnore is usually the output/result slot
                topInv.contents.forEachIndexed { index, itemStack ->
                    if (itemStack != null && index != indexToIgnore && itemStack.type != Material.AIR) {
                        playerInv.addItemProcessed(itemStack)
                    }
                }
            }

            when (topInv.type) {
                InventoryType.WORKBENCH, InventoryType.CRAFTING -> processContents(0)
                InventoryType.ENCHANTING -> processContents(1) // lapis slot, because of "LapisInEnchanter"
                InventoryType.ANVIL, InventoryType.MERCHANT -> processContents(2)
                else -> {}
            }
        }

        listen<PlayerDropItemEvent> { e ->
            if (!e.isCancelled) return@listen
            val itemStackMeta = e.itemDrop.itemStack?.itemMeta ?: return@listen
            if (itemStackMeta.displayName == DELETE_MARKER) {
                e.isCancelled = false
            }
        }

        listen<ItemSpawnEvent> { e ->
            val itemStackMeta = e.entity.itemStack?.itemMeta ?: return@listen
            if (itemStackMeta.displayName == DELETE_MARKER) {
                e.isCancelled = true
            }
        }
    }

    // add an item and decide what amount should be kept and dropped
    private fun Inventory.addItemProcessed(itemStack: ItemStack) {
        // add item; if added completely, delete the drop - otherwise let the remaining drop, drop
        fun Inventory.addItemMarked(itemStack: ItemStack): Int {
            val itemStackToDrop = addItem(itemStack).values.firstOrNull()
            return if (itemStackToDrop == null) {
                itemStack.setMeta<ItemMeta> { displayName = DELETE_MARKER };
                0
            } else itemStackToDrop.amount
        }

        // add recraft; if too much: add nerfed stack, drop the remaining amount + what could not be picked up
        if (RecraftNerf.isRegistered && itemStack.isRecraftMaterial()) {
            val recraftInfo = RecraftNerf.processRecraft(itemStack, recraftComponents())
            if (recraftInfo.needsNerf) {
                if (recraftInfo.pickUpAmount > 0) {
                    val pickUpItemStack = itemStack.clone().apply { amount = recraftInfo.pickUpAmount }
                    itemStack.apply { amount = recraftInfo.remainingAmount + addItemMarked(pickUpItemStack) }
                }
            } else addItemMarked(itemStack)
        } else addItemMarked(itemStack)
    }
}