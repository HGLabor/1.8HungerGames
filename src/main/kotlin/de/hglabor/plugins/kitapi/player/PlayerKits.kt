package de.hglabor.plugins.kitapi.player

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.phase.phases.LobbyPhase
import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.kitapi.kit.ClickableKitItem
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.PlaceableKitItem
import de.hglabor.plugins.kitapi.kit.isKitItem
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.broadcast
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent

object PlayerKits {

    fun register() {
        listen<PlayerInteractEvent> { event ->
            val item = event.item ?: return@listen
            val hgPlayer = event.player.hgPlayer
            val playerKit = hgPlayer.kit
            if (item.isKitItem) {
                val kitKey = item.itemMeta.displayName.split("${ChatColor.DARK_PURPLE}").last()
                if (playerKit.properties.kitname != kitKey) return@listen
                val kitItem = playerKit.internal.items.toList().first { it.second.stack == item }.second
                if (kitItem is ClickableKitItem) {
                    kitItem.onClick.invoke(event)
                }
            }
        }

        listen<BlockPlaceEvent> { event ->
            val item = event.player.itemInHand ?: return@listen
            val hgPlayer = event.player.hgPlayer
            val playerKit = hgPlayer.kit
            if (item.isKitItem) {
                val kitKey = item.itemMeta.displayName.split("${ChatColor.DARK_PURPLE}").last()
                if (playerKit.properties.kitname != kitKey) return@listen
                val kitItem = playerKit.internal.items.toList().first { it.second.stack == item }.second
                if (kitItem is PlaceableKitItem) {
                    kitItem.onBlockPlace.invoke(event)
                }
            }
        }

        listen<PlayerDropItemEvent> {
            if (it.itemDrop.itemStack.isKitItem) {
                it.isCancelled = true
            }
        }

        listen<ItemSpawnEvent> {
            if (it.entity.itemStack.isKitItem) {
                it.isCancelled = true
            }
        }

        /*
        // TODO cancel putting items in other inventory...
        listen<InventoryClickEvent> {
            if (it.clickedInventory.type != InventoryType.PLAYER || it.clickedInventory.type != InventoryType.CRAFTING) {
                if (it.currentItem.isKitItem) {
                    broadcast(it.clickedInventory.type.toString())
                    it.isCancelled = true
                }
            }
        }*/
    }

    /*fun Player.addKit(kit: Kit<*>) {
        hgPlayer.kits.add(kit)
    }*/

    fun Player.hasKit(kit: Kit<*>): Boolean {
        return hgPlayer.kit == kit && hgPlayer.isKitEnabled
    }

    fun Player.hasKit(kit: Lazy<Kit<*>>): Boolean {
        return hgPlayer.kit == kit && hgPlayer.isKitEnabled
    }

    fun Player.chooseKit(kit: Kit<*>) {
        hgPlayer.kit = kit
        sendMessage("${Prefix}You chose the kit ${ChatColor.LIGHT_PURPLE}${kit.properties.kitname}${ChatColor.GRAY}.")
        if (GameManager.phase != LobbyPhase)
            kit.internal.givePlayer(this)
    }
}