package de.hglabor.plugins.hungergames.commands

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.game.mechanics.SettingsGUI
import net.axay.kspigot.gui.openGUI
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object SettingsCommand : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        val player = sender as? Player ?: return false
        if (!player.isOp) {
            sender.sendMessage("$Prefix${ChatColor.RED}You are not permitted to execute this command.")
            return false
        }
        SettingsGUI.open(player)
        return true
    }
}