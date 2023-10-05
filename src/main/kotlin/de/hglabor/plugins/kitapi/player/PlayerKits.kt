package de.hglabor.plugins.kitapi.player

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.SecondaryColor
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.mechanics.implementation.KitSelector
import de.hglabor.plugins.hungergames.game.phase.phases.InvincibilityPhase
import de.hglabor.plugins.hungergames.game.phase.phases.LobbyPhase
import de.hglabor.plugins.hungergames.game.phase.phases.PvPPhase
import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.kitapi.kit.*
import net.axay.kspigot.event.listen
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

object PlayerKits {

    fun register() {
        listen<PlayerInteractEvent> { event ->
            val item = event.item ?: return@listen
            val hgPlayer = event.player.hgPlayer
            val playerKit = hgPlayer.kit
            if (item.isKitItem) {
                val kitKey = item.itemMeta.displayName.split("${ChatColor.DARK_PURPLE}").last()
                if (!event.player.hasKit(KitManager.kits.first { it.properties.kitname == kitKey })) return@listen
                val kitItem = playerKit.internal.items.toList().first { it.second.stack == item }.second
                if (kitItem is ClickableKitItem) {
                    if (!(!kitItem.useInInvincibility && GameManager.phase == InvincibilityPhase) || GameManager.phase == PvPPhase) {
                        kitItem.onClick.invoke(event)
                    } else {
                        event.player.sendMessage("${Prefix}You can't use this kit during the grace period.")
                    }
                }
            }
        }

        listen<PlayerInteractAtEntityEvent> { event ->
            val item = event.player.itemInHand ?: return@listen
            val hgPlayer = event.player.hgPlayer
            val playerKit = hgPlayer.kit
            if (item.isKitItem) {
                val kitKey = item.itemMeta.displayName.split("${ChatColor.DARK_PURPLE}").last()
                if (!event.player.hasKit(KitManager.kits.first { it.properties.kitname == kitKey })) return@listen
                val kitItem = playerKit.internal.items.toList().first { it.second.stack == item }.second
                if (kitItem is ClickOnEntityKitItem) {
                    if (!(!kitItem.useInInvincibility && GameManager.phase == InvincibilityPhase)) {
                        kitItem.onClick.invoke(event)
                    } else {
                        event.player.sendMessage("${Prefix}You can't use this kit during the grace period.")
                    }
                }
            }
        }

        listen<BlockPlaceEvent> { event ->
            val item = event.player.itemInHand ?: return@listen
            val hgPlayer = event.player.hgPlayer
            val playerKit = hgPlayer.kit
            if (item.isKitItem) {
                val kitKey = item.itemMeta.displayName.split("${ChatColor.DARK_PURPLE}").last()
                if (!event.player.hasKit(KitManager.kits.first { it.properties.kitname == kitKey })) return@listen
                val kitItem = playerKit.internal.items.toList().first { it.second.stack == item }.second
                if (kitItem is PlaceableKitItem) {
                    if (!(!kitItem.useInInvincibility && GameManager.phase == InvincibilityPhase)) {
                        kitItem.onBlockPlace.invoke(event)
                    } else {
                        event.isCancelled = true
                        event.player.sendMessage("${Prefix}You can't use this kit during the grace period.")
                    }
                } else {
                    event.isCancelled = true
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
        return hgPlayer.kit == kit && hgPlayer.isKitEnabled && kit.properties.isEnabled
    }

    fun Player.chooseKit(kit: Kit<out KitProperties>, sendMessage: Boolean = true) {
        hgPlayer.kit = kit
        hgPlayer.changedKitBefore = true
        if (sendMessage) {
            sendMessage("${Prefix}You chose the kit ${SecondaryColor}${kit.properties.kitname}${ChatColor.GRAY}.")
        }
        if (GameManager.phase != LobbyPhase)
            kit.internal.givePlayer(this)

        if (GameManager.phase == InvincibilityPhase) {
            inventory.remove(KitSelector.kitSelectorItem)
        }
    }
}