package de.hglabor.plugins.hungergames.game.mechanics.recraft

import de.hglabor.plugins.hungergames.game.mechanics.recraft.RecraftNerf.realMaterial
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.broadcast
import org.bukkit.Material
import org.bukkit.event.EventPriority
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.inventory.ItemStack

// todo: send feedback if events are cancelled or recraft stacks nerfed
object RecraftNerf {
    private const val RECRAFT_LIMIT = 128
    private val recraftList = listOf(
        Recraft(Material.BROWN_MUSHROOM, Material.RED_MUSHROOM),
        Recraft(Material.COCOA),
        Recraft(Material.CACTUS)
    )

    fun register() {
        // todo: einzelne pilz itemstacks daran hindern das rc limit zu überschreiten,
        //  auch wenn man sich erst mit 2 itemstacks suppen bauen kann

        // todo: compatibility with NoInvDropOnClose
        // cursorStack ; inventory contents ; set NoInvDropOnClose priority to normal
/*        listen<InventoryCloseEvent>(EventPriority.LOW) { e ->
            val cursorItemStack = e.view.cursor ?: return@listen
            val recraftComponents = e.player.inventory.contents.recraftComponents()
            val recraftInfo = processRecraft(cursorItemStack, recraftComponents)

            if (recraftInfo.needsNerf) {
                if (recraftInfo.pickUpAmount > 0) {
                    val stack = cursorItemStack.clone().apply { amount = recraftInfo.remainingAmount }
                    e.player.world.dropItemNaturally(e.player.location, stack) // does not spawn?
                    e.view.cursor = null // sets it to ItemStack(Material.AIR, 0) anyway NoInvDrop should not process it
                    e.player.inventory.addItem(cursorItemStack.clone().apply { amount = recraftInfo.pickUpAmount })
                }
            }
            // val recraftInfoContents = processRecraft(inventoryContents, recraftComponents)
        }*/

        listen<InventoryDragEvent> { e ->
            val topInvSlots: IntRange = (0 until e.view.topInventory.size)
            val playerInvContents = e.newItems.filter { it.key !in topInvSlots }.map { it.value }
            val amountPlaced = playerInvContents.sumOf { it.amount }
            val stack = e.oldCursor.clone().apply { amount = amountPlaced }
            val recraftComponents = playerInvContents.recraftComponents()
            val recraftInfo = processRecraft(stack, recraftComponents)

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
            val playerInv = e.view.bottomInventory
            val recraftComponents = playerInv.contents.recraftComponents()

            if (clickedInv == e.view.topInventory && action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                val recraftInfo = processRecraft(currentItemStack, recraftComponents)
                if (recraftInfo.needsNerf) {
                    e.isCancelled = true
                    if (recraftInfo.pickUpAmount > 0) {
                        e.currentItem = currentItemStack.apply { amount = recraftInfo.remainingAmount }
                        playerInv.addItem(currentItemStack.clone().apply { amount = recraftInfo.pickUpAmount })
                    }
                }
                return@listen
            }

            if (clickedInv != playerInv)
                return@listen

            val recraftInfo: RecraftInfo?
            when (action) {
                InventoryAction.PLACE_ALL -> {
                    recraftInfo = processRecraft(cursorItemStack, recraftComponents)
                    if (recraftInfo.needsNerf) {
                        e.isCancelled = true
                        if (recraftInfo.pickUpAmount > 0) {
                            e.cursor = cursorItemStack.apply { amount = recraftInfo.remainingAmount }
                            playerInv.addItem(cursorItemStack.clone().apply { amount = recraftInfo.pickUpAmount })
                        }
                    }
                }

                InventoryAction.PLACE_SOME -> {
                    val amountToPlace = currentItemStack.maxStackSize - currentItemStack.amount
                    val itemStackToPlace = cursorItemStack.clone().apply { amount = amountToPlace }
                    recraftInfo = processRecraft(itemStackToPlace, recraftComponents)
                    if (recraftInfo.needsNerf) {
                        e.isCancelled = true
                        if (recraftInfo.pickUpAmount > 0) {
                            // subtract what we placed, add the remaining to the cursor
                            val remaining = (cursorItemStack.amount - amountToPlace) + recraftInfo.remainingAmount
                            e.cursor = cursorItemStack.apply { amount = remaining }
                            playerInv.addItem(cursorItemStack.clone().apply { amount = recraftInfo.pickUpAmount })
                        }
                    }
                }

                InventoryAction.PLACE_ONE -> {
                    val itemStackToPlace = cursorItemStack.clone().apply { amount = 1 }
                    recraftInfo = processRecraft(itemStackToPlace, recraftComponents)
                    if (recraftInfo.needsNerf) e.isCancelled = true // add 1, if too much do not add
                }

                InventoryAction.SWAP_WITH_CURSOR -> {
                    recraftInfo = processRecraft(cursorItemStack, recraftComponents.apply { removeAt(e.slot) })
                    if (recraftInfo.needsNerf) e.isCancelled = true
                }

                else -> {}
            }
        }

        listen<PlayerPickupItemEvent> { e ->
            val eventItemStack = e.item.itemStack
            val recraftComponents = e.player.inventory.contents.recraftComponents()
            val recraftInfo = processRecraft(eventItemStack, recraftComponents)
            if (recraftInfo.needsNerf) {
                e.isCancelled = true
                if (recraftInfo.pickUpAmount == 0) e.item.remove()
                else e.item.itemStack = eventItemStack.apply { amount = recraftInfo.pickUpAmount }
                // beim nächsten aufheben ist es also okay
            }
        }
    }

    data class RecraftInfo(val needsNerf: Boolean, val remainingAmount: Int, val pickUpAmount: Int)

    private fun processRecraft(
        eventItemStack: ItemStack,
        recraftComponents: MutableList<RecraftComponent>
    ): RecraftInfo {
        val default = RecraftInfo(false, -1, -1)
        if (!eventItemStack.isRecraftMaterial())
            return default

        recraftComponents.forEach { broadcast("$it") }

        recraftComponents.add(RecraftComponent(eventItemStack.realMaterial(), eventItemStack.amount))
        val futureSoups = recraftList.sumOf { it.soups(recraftComponents) }
        if (futureSoups > RECRAFT_LIMIT) {
            val remainingAmount = futureSoups - RECRAFT_LIMIT // differenz, die gelöscht werden muss
            val pickUpAmount = eventItemStack.amount - remainingAmount // wie viel der spieler aufnehmen darf
            broadcast("futureSoups=$futureSoups | RECRAFT_LIMIT=$RECRAFT_LIMIT » needsNerf=true")
            broadcast("aufnehmenAmount=$pickUpAmount | uebrigAmount=$remainingAmount")
            return RecraftInfo(true, remainingAmount, pickUpAmount)
        }
        return default
    }

    data class RecraftComponent(val material: Material, val amount: Int)
    class Recraft(vararg _material: Material) {
        private val materials = _material.toList()
        fun soups(recraftComponents: List<RecraftComponent>) =
            materials.map { material -> recraftComponents.filter { it.material == material }.sumOf { it.amount } }
                .minOf { it }
    }

    private fun Array<ItemStack>.recraftComponents() =
        filterNotNull().map { RecraftComponent(it.realMaterial(), it.amount) }.toMutableList()

    private fun Collection<ItemStack>.recraftComponents() =
        filterNotNull().map { RecraftComponent(it.realMaterial(), it.amount) }.toMutableList()

    private val recraftMaterials =
        listOf(Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.CACTUS, Material.COCOA)
    private fun ItemStack.isCocoa() = type == Material.INK_SACK && data.data.toInt() == 3
    private fun ItemStack.realMaterial() = if (isCocoa()) Material.COCOA else type
    private fun ItemStack.isRecraftMaterial() = recraftMaterials.contains(realMaterial())
}