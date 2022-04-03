package de.hglabor.plugins.kitapi.kit

import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

sealed class KitItem {
    abstract val stack: ItemStack

    init {
        kitItems[stack] = this
    }
}

class SimpleKitItem(override val stack: ItemStack) : KitItem()

class ClickableKitItem(
    override val stack: ItemStack,
    val onClick: (PlayerInteractEvent) -> Unit,
) : KitItem()

private val kitItems = HashMap<ItemStack, KitItem>()

val ItemStack.isKitItem: Boolean
    get() = kitItems.contains(this)