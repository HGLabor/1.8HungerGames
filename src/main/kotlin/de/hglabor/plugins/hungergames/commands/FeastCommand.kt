package de.hglabor.plugins.hungergames.commands

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.SecondaryColor
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.player.hgPlayer
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object FeastCommand : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        val player = sender as? Player ?: return false
        if (!player.hgPlayer.isAlive) {
            player.sendMessage("${Prefix}You can't use this command while spectating.")
            return false
        }

        val feast = GameManager.feast
        if (feast == null) {
            player.sendMessage("${Prefix}The feast hasn't been announced yet.")
            return false
        }

        if (feast.isFinished || feast.inPreparation) {
            player.compassTarget = feast.feastCenter
            player.sendMessage("${Prefix}Your compass is now pointing towards the ${SecondaryColor}feast${ChatColor.GRAY}.")
        }
        return true
    }
}