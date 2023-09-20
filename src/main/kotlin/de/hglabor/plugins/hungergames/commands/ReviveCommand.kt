package de.hglabor.plugins.hungergames.commands

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.SecondaryColor
import de.hglabor.plugins.hungergames.game.arena.Arena
import de.hglabor.plugins.hungergames.game.mechanics.implementation.KitSelector
import de.hglabor.plugins.hungergames.player.hgPlayer
import net.axay.kspigot.gui.openGUI
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object ReviveCommand : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        val player = sender as? Player ?: return false
        if (!player.hasPermission("hglabor.hg.revive")) {
            sender.sendMessage("${Prefix}${ChatColor.RED}You are not permitted to execute this command.")
            return false
        }

        if (args.size != 1) {
            player.openGUI(KitSelector.gui)
            sender.sendMessage("${Prefix}Please use ${ChatColor.WHITE}/revive ${ChatColor.GRAY}<${SecondaryColor}Player${ChatColor.GRAY}>.")
            return false
        }

        val target = Bukkit.getPlayer(args[0])

        if (target == null || !target.isOnline) {
            sender.sendMessage("${Prefix}${ChatColor.RED}The player ${ChatColor.DARK_RED}${args[0]} ${ChatColor.RED}is not online.")
            return false
        }
        if (target.hgPlayer.isAlive) {
            sender.sendMessage("${Prefix}${ChatColor.GRAY}${target.name} is still ${ChatColor.RED}alive${ChatColor.GRAY}.")
            return false
        }

        if (Arena.currentlyFighting.contains(target.hgPlayer)) {
            sender.sendMessage("${Prefix}${ChatColor.GRAY}${target.name} is currently fighting in arena.")
            return false
        }
        sender.sendMessage("${Prefix}${SecondaryColor}${target.name}${ChatColor.GRAY} has been ${ChatColor.GREEN}revived${ChatColor.GRAY}.")
        target.hgPlayer.makeGameReady()
        target.hgPlayer.wasInArena = false
        target.hgPlayer.setGameScoreboard(true)
        Arena.queuedPlayers.remove(target.hgPlayer)
        target.sendMessage("${Prefix}You have been ${ChatColor.GREEN}revived${ChatColor.GRAY}.")
        return true
    }
}
