package de.hglabor.plugins.hungergames.commands

import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.mechanics.implementation.arena.Arena
import de.hglabor.plugins.hungergames.game.mechanics.implementation.arena.ArenaMechanic
import de.hglabor.plugins.hungergames.player.PlayerList
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

object InfoCommand : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        sender.sendMessage("${ChatColor.RED}${ChatColor.BOLD}${GameManager.phase.timeName}: ${ChatColor.WHITE}${GameManager.phase.getTimeString()}")
        sender.sendMessage("${ChatColor.GREEN}${ChatColor.BOLD}Players: ${ChatColor.WHITE}${PlayerList.getShownPlayerCount()}")
        if (ArenaMechanic.internal.isEnabled) {
            sender.sendMessage("")
            sender.sendMessage("${ChatColor.RED}${ChatColor.BOLD}Arena: ${ChatColor.WHITE}${if (Arena.isOpen) "Open" else "Closed"}")
            sender.sendMessage("${ChatColor.RED}${ChatColor.BOLD}Waiting: ${ChatColor.WHITE}${Arena.queuedPlayers.size}")
            sender.sendMessage("${ChatColor.RED}${ChatColor.BOLD}Fighting: ${ChatColor.WHITE}${Arena.currentMatch?.players?.size ?: 0}")
        }
        return true
    }
}