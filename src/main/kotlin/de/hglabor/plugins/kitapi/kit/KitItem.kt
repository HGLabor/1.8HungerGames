package de.hglabor.plugins.kitapi.kit

import org.bukkit.ChatColor
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

sealed class KitItem {
    abstract val stack: ItemStack
}

class SimpleKitItem(override val stack: ItemStack) : KitItem()

class ClickableKitItem(
    override val stack: ItemStack,
    val onClick: (PlayerInteractEvent) -> Unit,
) : KitItem()

val ItemStack.isKitItem: Boolean
    get() = itemMeta.lore.first() == "${ChatColor.DARK_PURPLE}Kititem"