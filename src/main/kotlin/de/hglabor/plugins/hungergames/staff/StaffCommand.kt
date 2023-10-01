package de.hglabor.plugins.hungergames.staff

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.player.staffPlayer
import de.hglabor.plugins.hungergames.staff.module.command.IStaffCommand
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object StaffCommand : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        val player = sender as? Player ?: return false
        if (!player.hasPermission("hglabor.staff")) {
            sender.sendMessage("${Prefix}${ChatColor.RED}You are not permitted to execute this command.")
            return false
        }

        run moduleCommands@{
            if (args.isNotEmpty()) {
                if (args[0].lowercase() == "help") {
                    showHelp(player)
                    return true
                }

                val staffModule = StaffMode.modules.filterIsInstance<IStaffCommand>()
                    .firstOrNull { it.command.name == args[0].lowercase() } ?: return@moduleCommands

                if (!shouldExecute(player)) {
                    player.sendMessage("${StaffMode.prefix}${ChatColor.RED}Please enable StaffMode first.")
                    return false
                }

                val moduleArgs = args.toMutableList().apply { removeFirst() }
                staffModule.command.commandCallback.invoke(player, moduleArgs)
                return true
            }
        }

        if (player.staffPlayer?.isStaffMode == false)
            showHelp(player)
        player.staffPlayer?.toggleStaffMode()
        return true
    }

    private fun showHelp(player: Player) {
        player.sendMessage("${ChatColor.DARK_GRAY}${ChatColor.STRIKETHROUGH}             ${ChatColor.DARK_GRAY}| ${ChatColor.DARK_PURPLE}Staff ${ChatColor.DARK_GRAY}|${ChatColor.DARK_PURPLE}             ")
        player.sendMessage(" ${ChatColor.DARK_GRAY}| ${ChatColor.GRAY}Possible commands:")
        StaffMode.modules.filterIsInstance<IStaffCommand>().forEach { module ->
            player.sendMessage("   ${ChatColor.DARK_GRAY}• ${ChatColor.WHITE}${module.commandUsage} ${ChatColor.DARK_GRAY}- ${ChatColor.GRAY}${module.description}")
        }
        player.sendMessage("${ChatColor.DARK_GRAY}${ChatColor.STRIKETHROUGH}                                    ")
    }

    private fun shouldExecute(player: Player): Boolean {
        val staffPlayer = player.staffPlayer ?: return false
        return staffPlayer.isStaffMode
    }
}
