package de.hglabor.plugins.hungergames.game.mechanics

import net.axay.kspigot.event.listen
import net.axay.kspigot.items.setMeta
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.inventory.meta.ItemMeta
import java.util.UUID

// todo: enable as setting
object NoInvDropOnClose {
    private val IS_DEL_MARKER = UUID.randomUUID().toString()

    fun register() {
        listen<InventoryCloseEvent>(EventPriority.LOWEST) { e ->
            val cursorStack = e.view.cursor
            if (cursorStack != null && cursorStack.itemMeta != null) {
                e.player.inventory.addItem(cursorStack)
                cursorStack.setMeta<ItemMeta> { displayName = IS_DEL_MARKER }
            }

            val topInv = e.view.topInventory
            fun processContents(indexToIgnore: Int) { // indexToIgnore is usually the output/result slot
                topInv.contents.forEachIndexed { index, itemStack ->
                    if (itemStack == null || index == indexToIgnore)
                        return@forEachIndexed

                    val toDrop = e.player.inventory.addItem(itemStack)
                    if (toDrop.isEmpty()) {
                        itemStack.setMeta<ItemMeta> { displayName = IS_DEL_MARKER }
                    }
                }
            }

            when (topInv.type) {
                InventoryType.WORKBENCH, InventoryType.CRAFTING -> processContents(0)
                InventoryType.ENCHANTING -> processContents(1) // lapis slot, because of "LapisInEnchanter"
                InventoryType.ANVIL, InventoryType.MERCHANT -> processContents(2)
                else -> Unit
            }
        }

        listen<PlayerDropItemEvent> { e ->
            if (!e.isCancelled) return@listen
            val itemStackMeta = e.itemDrop.itemStack?.itemMeta ?: return@listen
            if (itemStackMeta.displayName == IS_DEL_MARKER) {
                e.isCancelled = false
            }
        }

        listen<ItemSpawnEvent> { e ->
            val itemStackMeta = e.entity.itemStack?.itemMeta ?: return@listen
            if (itemStackMeta.displayName == IS_DEL_MARKER) {
                e.isCancelled = true
            }
        }
    }
}