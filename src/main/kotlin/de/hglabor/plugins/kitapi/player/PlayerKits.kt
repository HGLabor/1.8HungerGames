package de.hglabor.plugins.kitapi.player

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.phase.GamePhase
import de.hglabor.plugins.hungergames.game.phase.phases.LobbyPhase
import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.kitapi.kit.ClickableKitItem
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.isKitItem
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.broadcast
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent

object PlayerKits {

    //private val kits = HashMap<UUID, MutableSet<Kit<*>>>()

    fun register() {
        listen<PlayerInteractEvent> { event ->
            val item = event.item ?: return@listen
            val hgPlayer = event.player.hgPlayer
            val playerKit = hgPlayer.kit
            // TODO FIX REQUIRED
            broadcast("1")
            if (item.isKitItem) {
                broadcast("is kit item")
                val kitKey = item.itemMeta.displayName.split("${ChatColor.DARK_PURPLE}").last()
                broadcast(kitKey)
                if (playerKit.properties.kitname != kitKey) return@listen
                broadcast("2")
                val kitItem = playerKit.internal.items.toList().first { it.second.stack == item }.second
                if (kitItem is ClickableKitItem) {
                    broadcast("3")
                    kitItem.onClick.invoke(event)
                }
            }
        }
    }

    /*fun Player.addKit(kit: Kit<*>) {
        hgPlayer.kits.add(kit)
    }*/

    fun Player.hasKit(kit: Kit<*>): Boolean {
        return hgPlayer.kit == kit
    }

    fun Player.chooseKit(kit: Kit<*>) {
        hgPlayer.kit = kit
        sendMessage("${Prefix}You chose the kit ${ChatColor.LIGHT_PURPLE}${kit.properties.kitname}${ChatColor.GRAY}.")
        if (GameManager.phase != LobbyPhase)
            kit.internal.givePlayer(this)
    }
}