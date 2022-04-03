package de.hglabor.plugins.hungergames.commands

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.phase.phases.EndPhase
import de.hglabor.plugins.hungergames.game.phase.phases.InvincibilityPhase
import de.hglabor.plugins.hungergames.game.phase.phases.LobbyPhase
import de.hglabor.plugins.hungergames.game.phase.phases.PvPPhase
import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.kitapi.implementation.None
import de.hglabor.plugins.kitapi.kit.KitManager
import de.hglabor.plugins.kitapi.player.PlayerKits.chooseKit
import net.axay.kspigot.extensions.broadcast
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object KitCommand : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        val player = sender as? Player ?: return false
        if (args.size != 1) {
            sender.sendMessage("${Prefix}Please specify a kit.")
            return false
        }

        when (GameManager.phase) {
            PvPPhase, EndPhase -> {
                sender.sendMessage("${Prefix}You can't choose a kit anymore.")
                return false
            }
            InvincibilityPhase -> {
                if (player.hgPlayer.kit != None.value) {
                    sender.sendMessage("${Prefix}You already have a kit.")
                    return false
                }
            }
        }

        val kit = KitManager.kits.firstOrNull { it.properties.kitname.lowercase() == args[0].lowercase() }
        if (kit == null) {
            sender.sendMessage("${Prefix}Please specify a kit.")
            return false
        }
        player.chooseKit(kit)
        return true
    }
}