package de.hglabor.plugins.hungergames.commands

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.phase.phases.PvPPhase
import net.axay.kspigot.extensions.broadcast
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

object StartCommand: CommandExecutor {
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
        if (GameManager.phase == PvPPhase) {
            sender.sendMessage("${Prefix}? x D")
            return false
        }
        broadcast("${Prefix}${ChatColor.WHITE}${ChatColor.BOLD}The next game phase has been started!")
        GameManager.startNextPhase()
        return true
    }

}