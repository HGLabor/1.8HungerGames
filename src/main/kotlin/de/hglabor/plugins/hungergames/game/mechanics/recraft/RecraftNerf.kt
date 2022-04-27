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
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryType.SlotType
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.inventory.ItemStack

// todo: send feedback if events are cancelled or recraft stacks nerfed
object RecraftNerf {
    private const val RECRAFT_LIMIT = 64
    private val recraftList = listOf(
        Recraft(Material.BROWN_MUSHROOM, Material.RED_MUSHROOM),
        Recraft(Material.COCOA),
        Recraft(Material.CACTUS)
    )

    fun register() {
        // todo: compatibility with NoInvDropOnClose
        // cursorStack ; inventory contents ; set NoInvDropOnClose priority to normal
/*        listen<InventoryCloseEvent>(EventPriority.LOW) { e ->
            val playerInvContents = e.player.inventory.contents.toList()
            val cursorItemStack = e.view.cursor ?: return@listen
            val recraftInfo = processRecraft(cursorItemStack, playerInvContents) ?: return@listen

            if (recraftInfo.needsNerf) {
                if (recraftInfo.aufnehmenAmount > 0) {
                    val stack = cursorItemStack.clone().apply { amount = recraftInfo.uebrigAmount }
                    e.player.world.dropItemNaturally(e.player.location, stack) // does not spawn?
                    e.view.cursor = null // sets it to ItemStack(Material.AIR, 0) anyway NoInvDrop should not process it
                    e.player.inventory.addItem(cursorItemStack.clone().apply { amount = recraftInfo.aufnehmenAmount })
                }
            }
        }*/

        listen<InventoryDragEvent> { e ->
            val topInvSlots: IntRange = (0 until e.view.topInventory.size)
            val playerInvContents = e.newItems.filter { it.key !in topInvSlots }.map { it.value }
            val amountPlaced = playerInvContents.sumOf { it.amount }
            val stack = e.oldCursor.clone().apply { amount = amountPlaced }
            val recraftComponents =
                playerInvContents.mapNotNull { RecraftComponent(it.realMaterial(), it.amount) }.toMutableList()
            val recraftInfo = processRecraft(stack, recraftComponents) ?: return@listen

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
            val recraftComponents =
                playerInv.contents.mapNotNull { RecraftComponent(it.realMaterial(), it.amount) }.toMutableList()

            if (clickedInv == e.view.topInventory && action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                val recraftInfo = processRecraft(currentItemStack, recraftComponents) ?: return@listen
                if (recraftInfo.needsNerf) {
                    e.isCancelled = true
                    if (recraftInfo.aufnehmenAmount > 0) {
                        e.currentItem = currentItemStack.apply { amount = recraftInfo.uebrigAmount }
                        playerInv.addItem(currentItemStack.clone().apply { amount = recraftInfo.aufnehmenAmount })
                    }
                }
                return@listen
            }

            if (clickedInv != playerInv)
                return@listen

            if (action == InventoryAction.PLACE_ALL) {
                val recraftInfo = processRecraft(cursorItemStack, recraftComponents) ?: return@listen
                if (recraftInfo.needsNerf) {
                    e.isCancelled = true
                    if (recraftInfo.aufnehmenAmount > 0) {
                        e.cursor = cursorItemStack.apply { amount = recraftInfo.uebrigAmount }
                        playerInv.addItem(cursorItemStack.clone().apply { amount = recraftInfo.aufnehmenAmount })
                    }
                }
                return@listen
            }

            if (action == InventoryAction.PLACE_SOME) {
                val amountToPlace = currentItemStack.maxStackSize - currentItemStack.amount
                val itemStackToPlace = cursorItemStack.clone().apply { amount = amountToPlace }
                val recraftInfo = processRecraft(itemStackToPlace, recraftComponents) ?: return@listen
                if (recraftInfo.needsNerf) {
                    e.isCancelled = true
                    if (recraftInfo.aufnehmenAmount > 0) {
                        // das abziehen was wir gegeben haben und das dazugeben, was von dem gegebenen uebrig geblieben ist
                        val uebrig = (cursorItemStack.amount - amountToPlace) + recraftInfo.uebrigAmount
                        e.cursor = cursorItemStack.apply { amount = uebrig }
                        playerInv.addItem(cursorItemStack.clone().apply { amount = recraftInfo.aufnehmenAmount })
                    }
                }
                return@listen
            }

            if (action == InventoryAction.PLACE_ONE) {
                val itemStackToPlace = cursorItemStack.clone().apply { amount = 1 }
                val recraftInfo = processRecraft(itemStackToPlace, recraftComponents) ?: return@listen
                // man fügt 1 hinzu und wenn das zu viel ist (needsNerf), dann fügt man den halt nicht hinzu (cancel)
                if (recraftInfo.needsNerf) {
                    e.isCancelled = true
                }
                return@listen
            }

            if (action == InventoryAction.SWAP_WITH_CURSOR) {
                val recraftInfo =
                    processRecraft(cursorItemStack, recraftComponents.apply { removeAt(e.slot) })
                        ?: return@listen
                if (recraftInfo.needsNerf) {
                    e.isCancelled = true
                }
                return@listen
            }
        }

        listen<PlayerPickupItemEvent> { e ->
            val eventItemStack = e.item.itemStack
            val recraftComponents =
                e.player.inventory.contents.mapNotNull { RecraftComponent(it.realMaterial(), it.amount) }
                    .toMutableList()
            val recraftInfo = processRecraft(eventItemStack, recraftComponents) ?: return@listen
            if (recraftInfo.needsNerf) {
                e.isCancelled = true
                if (recraftInfo.aufnehmenAmount == 0) e.item.remove()
                else e.item.itemStack = eventItemStack.apply { amount = recraftInfo.aufnehmenAmount }
                // beim nächsten aufheben ist es also okay
            }
        }
    }

    data class RecraftInfo(val needsNerf: Boolean, val uebrigAmount: Int, val aufnehmenAmount: Int)

    private fun processRecraft(
        eventItemStack: ItemStack,
        recraftComponents: MutableList<RecraftComponent>
    ): RecraftInfo? {
        if (!eventItemStack.isRecraftMaterial())
            return null

        recraftComponents.forEach { broadcast("$it") }

        recraftComponents.add(RecraftComponent(eventItemStack.realMaterial(), eventItemStack.amount))
        val futureSoups = recraftList.sumOf { it.soups(recraftComponents) }

        if (futureSoups > RECRAFT_LIMIT) {
            val uebrigAmount = futureSoups - RECRAFT_LIMIT // differenz, die gelöscht werden muss
            val aufnehmenAmount = eventItemStack.amount - uebrigAmount // wie viel der spieler aufnehmen darf
            broadcast("futureSoups=$futureSoups | RECRAFT_LIMIT=$RECRAFT_LIMIT » needsNerf=true")
            broadcast("aufnehmenAmount=$aufnehmenAmount | uebrigAmount=$uebrigAmount")
            return RecraftInfo(true, uebrigAmount, aufnehmenAmount)
        }

        return null // no nerf needed so we can return faster with "?: return@listen"
    }

    data class RecraftComponent(val material: Material, val amount: Int)
    class Recraft(vararg _material: Material) {
        private val materials = _material.toList()
        fun soups(recraftComponents: List<RecraftComponent>) =
            materials.map { material -> recraftComponents.filter { it.material == material }.sumOf { it.amount } }
                .minOf { it }
    }

    private val recraftMaterials =
        listOf(Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.CACTUS, Material.COCOA)

    private fun ItemStack.isCocoa() = type == Material.INK_SACK && data.data.toInt() == 3
    private fun ItemStack.realMaterial() = if (isCocoa()) Material.COCOA else type
    private fun ItemStack.isRecraftMaterial() = recraftMaterials.contains(realMaterial())
}