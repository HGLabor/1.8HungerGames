package de.hglabor.plugins.hungergames.commands

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.mechanics.implementation.arena.Arena
import de.hglabor.plugins.hungergames.game.phase.IngamePhase
import de.hglabor.plugins.hungergames.player.HGPlayer
import de.hglabor.plugins.hungergames.player.PlayerList
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

object ListCommand : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (GameManager.phase !is IngamePhase) {
            sender.sendMessage("${Prefix}The game hasn't begun yet.")
        }
        val alivePlayers = PlayerList.alivePlayers
        val arenaPlayers = Arena.currentMatch?.players?.toMutableList()?.apply {
            addAll(Arena.queuedPlayers)
        } ?: Arena.queuedPlayers

        sender.sendMessage("${ChatColor.GREEN}Alive ${ChatColor.GRAY}(${alivePlayers.size}): ${alivePlayers.stringify()}")
        sender.sendMessage("${ChatColor.RED}Arena ${ChatColor.GRAY}(${arenaPlayers.size}): ${arenaPlayers.stringify()}")
        return true
    }

    private fun Collection<HGPlayer>.stringify() = joinToString(separator = "${ChatColor.GRAY}, ") { "${ChatColor.WHITE}${it.name}" }
}