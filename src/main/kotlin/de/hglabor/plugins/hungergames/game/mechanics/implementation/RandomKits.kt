package de.hglabor.plugins.hungergames.game.mechanics.implementation

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.SecondaryColor
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.mechanics.Mechanic
import de.hglabor.plugins.hungergames.game.phase.phases.LobbyPhase
import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.kitapi.implementation.None
import de.hglabor.plugins.kitapi.kit.KitManager
import de.hglabor.plugins.kitapi.player.PlayerKits.chooseKit
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.extensions.onlinePlayers
import org.bukkit.ChatColor
import org.bukkit.event.player.PlayerJoinEvent

val RandomKits by Mechanic("Random Kits", isEvent = true) {
    onEnable {
        if (GameManager.phase != LobbyPhase) return@onEnable
        PlayerList.allPlayers.forEach { hgPlayer ->
            hgPlayer.kit = None.value
            hgPlayer.changedKitBefore = false
            hgPlayer.bukkitPlayer?.let { player ->
                player.inventory.remove(KitSelector.kitSelectorItem)
                player.sendMessage("${Prefix}Your kit was removed.")
            }
        }
        broadcast("${ChatColor.GREEN}${ChatColor.BOLD}Random Kits has been enabled for this round.")
    }

    onGameStart {
        onlinePlayers.forEach { player ->
            if (player.hgPlayer.kit == None.value && !player.hgPlayer.changedKitBefore) {
                val kit = KitManager.kits.filter { it != None.value }.random()
                player.chooseKit(kit, false)
                player.sendMessage("${Prefix}You have been given the kit $SecondaryColor${kit.properties.kitname}${ChatColor.GRAY}.")
            }
        }
    }

    onDisable {
        if (GameManager.phase != LobbyPhase) return@onDisable
        onlinePlayers.forEach {
            it.inventory.addItem(KitSelector.kitSelectorItem)
        }
        broadcast("${ChatColor.RED}${ChatColor.BOLD}Random Kits has been disabled for this round.")
    }

    mechanicEvent<PlayerJoinEvent> {
        if (GameManager.phase != LobbyPhase) return@mechanicEvent
        val player = it.player
        if (player.hgPlayer.kit != None.value) return@mechanicEvent
        val kit = KitManager.kits.filter { it != None.value }.random()
        player.chooseKit(kit, false)
        player.sendMessage("${Prefix}You have been given the kit $SecondaryColor${kit.properties.kitname}${ChatColor.GRAY}.")
    }
}