package de.hglabor.plugins.hungergames.commands

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.player.PlayerList
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

object BanSpecsCommand: CommandExecutor {
    var allowSpecs = true

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (!sender.hasPermission("hglabor.hg.start")) {
            sender.sendMessage("${Prefix}${ChatColor.RED}You are not permitted to execute this command.")
            return false
        }
        allowSpecs = !allowSpecs
        sender.sendMessage("${Prefix}${ChatColor.RED}Allow specs: $allowSpecs.")
        return true
    }

}