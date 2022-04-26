package de.hglabor.plugins.hungergames.game.mechanics.recraft

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.broadcast
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.inventory.ItemStack

object RecraftNerf {
    private const val RECRAFT_LIMIT = 64
    private val recraftList = listOf(
        Recraft(Material.BROWN_MUSHROOM, Material.RED_MUSHROOM),
        Recraft(Material.COCOA),
        Recraft(Material.CACTUS)
    )

    // todo: compatibility with NoInvDropOnClose

    fun register() {
        // todo: nur wenn items von oben in das eigene Inventar kommen und einen nerf brauchen, dann
        listen<InventoryClickEvent> { e ->
            broadcast("action=${e.action}")
            broadcast("" + e.currentItem.type) // pickup
            broadcast("" + e.cursor.type) // place
            // if ist iwie falsch
            if (e.clickedInventory == e.view.bottomInventory && e.currentItem.isRecraftMaterial()) {
                broadcast("InventoryClickEvent, von oben von unten")

                val eventItemStack = e.currentItem ?: return@listen
                val recraftInfo =
                    processRecraft(eventItemStack, e.view.bottomInventory.contents.toList()) ?: return@listen
                val itemStack = recraftInfo.eventItemStack

                if (recraftInfo.needsNerf) {
                    e.isCancelled = true
                    if (itemStack.amount == 0) itemStack.type = Material.AIR
                    e.currentItem = itemStack
                    broadcast("stackAmount=${eventItemStack.amount}")
                    broadcast("stackType=${eventItemStack.type}")
                }
            }
        }

        // maybe: nerf every material component to rc limit
        listen<PlayerPickupItemEvent> { e ->
            broadcast("PlayerPickupItemEvent")
            val eventItemStack = e.item.itemStack
            val recraftInfo = processRecraft(eventItemStack, e.player.inventory.contents.toList()) ?: return@listen
            val itemStack = recraftInfo.eventItemStack

            if (recraftInfo.needsNerf) {
                e.isCancelled = true
                if (itemStack.amount == 0) e.item.remove()
                else e.item.itemStack = itemStack
                broadcast("stackAmount=${itemStack.amount}")
                broadcast("stackType=${itemStack.type}")
            }
        }
    }

    data class RecraftInfo(val needsNerf: Boolean, val eventItemStack: ItemStack)

    private fun processRecraft(eventItemStack: ItemStack, inventoryContents: List<ItemStack?>): RecraftInfo? {
        if (!eventItemStack.isRecraftMaterial())
            return null

        val recraftComponents = mutableListOf<RecraftComponent>()

        for (itemStack in inventoryContents.filterNotNull()) {
            if (itemStack.isRecraftMaterial()) {
                recraftComponents.add(RecraftComponent(itemStack.realMaterial(), itemStack.amount))
            }
        }

        recraftComponents.forEach { broadcast("$it") }

        recraftComponents.add(RecraftComponent(eventItemStack.realMaterial(), eventItemStack.amount))
        val futureSoups = recraftList.sumOf { it.soups(recraftComponents) } // futureSoups

        if (futureSoups > RECRAFT_LIMIT) {
            broadcast("futureSoups=$futureSoups - RECRAFT_LIMIT=$RECRAFT_LIMIT = ${futureSoups - RECRAFT_LIMIT}")
            broadcast("eventItemStack.amount=${eventItemStack.amount}")
            broadcast("vermindertAmount=${eventItemStack.amount - (futureSoups - RECRAFT_LIMIT)}")

            eventItemStack.amount -= futureSoups - RECRAFT_LIMIT
            return RecraftInfo(true, eventItemStack)
        }

        return null
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