package de.hglabor.plugins.kitapi.kit

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

sealed class KitItem(open val useInInvincibility: Boolean = true) {
    abstract val stack: ItemStack
}

class SimpleKitItem(override val stack: ItemStack) : KitItem()

class ClickableKitItem(
    override val stack: ItemStack,
    override val useInInvincibility: Boolean = true,
    val onClick: (PlayerInteractEvent) -> Unit,
) : KitItem()

class ClickOnEntityKitItem(
    override val stack: ItemStack,
    override val useInInvincibility: Boolean = true,
    val onClick: (PlayerInteractAtEntityEvent) -> Unit,
) : KitItem()

class PlaceableKitItem(
    override val stack: ItemStack,
    override val useInInvincibility: Boolean = true,
    val onBlockPlace: (BlockPlaceEvent) -> Unit,
) : KitItem()

val ItemStack?.isKitItem: Boolean
    get() {
        if (this == null) return false
        if (type == Material.AIR) return false
        if (itemMeta == null) return false
        if (itemMeta.lore == null || itemMeta.lore.isEmpty()) return false
        return itemMeta.lore.first() == "${ChatColor.DARK_PURPLE}Kititem"
    }