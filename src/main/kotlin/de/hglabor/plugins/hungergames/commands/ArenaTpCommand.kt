package de.hglabor.plugins.hungergames.commands

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.game.mechanics.implementation.arena.ArenaWorld
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object ArenaTpCommand : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        val player = sender as? Player ?: return false
        if (!player.hasPermission("hglabor.admin")) {
            sender.sendMessage("${Prefix}${ChatColor.RED}You are not permitted to execute this command.")
            return false
        }

        player.teleport(Location(ArenaWorld.world, 0.0, 10.0, 0.0))
        return true
    }
}
