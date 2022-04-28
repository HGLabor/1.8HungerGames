package de.hglabor.plugins.hungergames.game.mechanics.recraft

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.broadcast
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.math.max

// todo: send feedback if events are cancelled or recraft stacks nerfed
object RecraftNerf {
    private const val RECRAFT_LIMIT = 64
    val recraftList = listOf(
        Recraft(Material.BROWN_MUSHROOM, Material.RED_MUSHROOM),
        Recraft(Material.COCOA),
        Recraft(Material.CACTUS)
    )

    var isRegistered = false
    fun register() {
        isRegistered = true

        // bug: wrong calculation if recraft is dragged over other recraft
        listen<InventoryDragEvent> { e ->
            val oldCursorStack = e.oldCursor
            if (!oldCursorStack.isRecraftMaterial())
                return@listen

            val topInvSlots: IntRange = (0 until e.view.topInventory.size)
            val newItemStacks = e.newItems.filter { it.key !in topInvSlots }.map { it.value }
            val amountPlaced = newItemStacks.sumOf { it.amount }
            val stack = oldCursorStack.clone().apply { amount = amountPlaced }
            val recraftInfo = processRecraft(stack, e.view.bottomInventory.recraftComponents())

            if (recraftInfo.needsNerf)
                e.isCancelled = true
        }

        listen<InventoryClickEvent> { e ->
            val currentItemStack = e.currentItem ?: ItemStack(Material.AIR)
            val cursorItemStack = e.cursor ?: ItemStack(Material.AIR)
            if (!currentItemStack.isRecraftMaterial() && !cursorItemStack.isRecraftMaterial())
                return@listen

            val action = e.action
            val clickedInv = e.clickedInventory
            val topInv = e.view.topInventory
            val playerInv = e.view.bottomInventory

            fun applyProcessRecraft(
                itemStackToAdd: ItemStack,
                recraftComponents: MutableList<RecraftComponent> = playerInv.recraftComponents(),
                block: (RecraftInfo) -> Unit = {},
            ) {
                val recraftInfo = processRecraft(itemStackToAdd, recraftComponents)
                if (recraftInfo.needsNerf) {
                    e.isCancelled = true
                    if (recraftInfo.pickUpAmount > 0) block(recraftInfo)
                }
            }

            // bug: if pickUpAmount > inventory space the clicked stack is set to remaining even though
            // the amount of items picked up does not match pickUpAmount
            if (clickedInv == topInv && action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                applyProcessRecraft(currentItemStack) {
                    currentItemStack.apply { amount = it.remainingAmount }
                    playerInv.addItem(currentItemStack.clone().apply { amount = it.pickUpAmount })
                }
            } else if (action == InventoryAction.HOTBAR_SWAP || action == InventoryAction.HOTBAR_MOVE_AND_READD) {
                if (e.rawSlot in (0..topInv.size)) {
                    applyProcessRecraft(currentItemStack) {
                        currentItemStack.apply { amount = it.remainingAmount }
                        playerInv.addItem(currentItemStack.clone().apply { amount = it.pickUpAmount })
                    }
                }
                return@listen
            }

            if (clickedInv != playerInv || !cursorItemStack.isRecraftMaterial())
                return@listen

            when (action) {
                InventoryAction.PLACE_ALL -> {
                    applyProcessRecraft(cursorItemStack) {
                        cursorItemStack.apply { amount = it.remainingAmount }
                        playerInv.addItem(cursorItemStack.clone().apply { amount = it.pickUpAmount })
                    }
                }

                InventoryAction.PLACE_SOME -> {
                    val amountToPlace = currentItemStack.maxStackSize - currentItemStack.amount
                    val itemStackToPlace = cursorItemStack.clone().apply { amount = amountToPlace }
                    applyProcessRecraft(itemStackToPlace) {
                        // subtract what we placed, add the remaining to the cursor
                        val remaining = (cursorItemStack.amount - amountToPlace) + it.remainingAmount
                        itemStackToPlace.apply { amount = remaining }
                        playerInv.addItem(cursorItemStack.clone().apply { amount = it.pickUpAmount })
                    }
                }

                InventoryAction.PLACE_ONE -> {
                    val itemStackToPlace = cursorItemStack.clone().apply { amount = 1 }
                    applyProcessRecraft(itemStackToPlace)
                }

                InventoryAction.SWAP_WITH_CURSOR -> {
                    val newCursorStack = clickedInv.getItem(e.slot)
                    val recraftComponents = playerInv.recraftComponents()
                    if (newCursorStack.isRecraftMaterial())
                        recraftComponents.remove(RecraftComponent(newCursorStack.type, newCursorStack.amount))
                    applyProcessRecraft(cursorItemStack, recraftComponents)
                }

                else -> {}
            }
        }

        listen<PlayerPickupItemEvent> { e ->
            val eventItemStack = e.item.itemStack
            if (!eventItemStack.isRecraftMaterial())
                return@listen

            val recraftComponents = e.player.inventory.recraftComponents()
            val recraftInfo = processRecraft(eventItemStack, recraftComponents)
            if (recraftInfo.needsNerf) {
                e.isCancelled = true
                if (recraftInfo.pickUpAmount == 0) e.item.remove()
                else e.item.itemStack = eventItemStack.apply { amount = recraftInfo.pickUpAmount }
                // next pick up will be successful
            }
        }
    }

    data class RecraftInfo(val needsNerf: Boolean, val remainingAmount: Int, val pickUpAmount: Int)
    data class RecraftComponent(val material: Material, val amount: Int)
    class Recraft(vararg _material: Material) {
        val materials = _material.toList()
        fun soups(recraftComponents: List<RecraftComponent>) =
            materials.map { material -> recraftComponents.filter { it.material == material }.sumOf { it.amount } }.minOf { it }
    }

    fun processRecraft(
        itemStackToAdd: ItemStack,
        recraftComponents: MutableList<RecraftComponent>
    ): RecraftInfo {
        val default = RecraftInfo(false, -1, -1)
        if (!itemStackToAdd.isRecraftMaterial())
            return default
        recraftComponents.forEach { broadcast("$it") } // debug
        recraftComponents.add(RecraftComponent(itemStackToAdd.realMaterial(), itemStackToAdd.amount))
        val futureSoups = recraftList.sumOf { it.soups(recraftComponents) }
        if (futureSoups > RECRAFT_LIMIT) {
            val remainingAmount = futureSoups - RECRAFT_LIMIT // differenz, die gelöscht werden muss
            val pickUpAmount = max(0, itemStackToAdd.amount - remainingAmount) // wie viel der spieler aufnehmen darf
            broadcast("futureSoups=$futureSoups | RECRAFT_LIMIT=$RECRAFT_LIMIT | needsNerf=true") // debug
            broadcast("pickUpAmount=$pickUpAmount | remainingAmount=$remainingAmount") // debug
            return RecraftInfo(true, remainingAmount, pickUpAmount)
        }
        return default
    }
}

private fun ItemStack.isCocoa() = type == Material.INK_SACK && data.data.toInt() == 3
private fun ItemStack.realMaterial() = if (isCocoa()) Material.COCOA else type
private val recraftMaterials = RecraftNerf.recraftList.flatMap { it.materials }
fun ItemStack.isRecraftMaterial() = recraftMaterials.contains(realMaterial())

fun Inventory.recraftComponents() = contents.filterNotNull().filter { it.isRecraftMaterial() }
    .map { RecraftNerf.RecraftComponent(it.realMaterial(), it.amount) }.toMutableList()