package de.hglabor.plugins.hungergames.commands

import de.hglabor.plugins.hungergames.game.GameManager
import net.axay.kspigot.extensions.broadcast
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
        if (!sender.isOp) return false
        broadcast("Starting next phase")
        GameManager.startNextPhase()
        return true
    }

}