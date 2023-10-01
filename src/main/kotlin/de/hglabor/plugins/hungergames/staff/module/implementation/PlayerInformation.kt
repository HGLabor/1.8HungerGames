package de.hglabor.plugins.hungergames.staff.module.implementation

import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.hungergames.staff.StaffMode
import de.hglabor.plugins.hungergames.staff.module.InteractWithPlayerModule
import de.hglabor.plugins.hungergames.staff.module.command.IStaffCommand
import de.hglabor.plugins.hungergames.staff.module.command.staffCommand
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.ItemStack

object PlayerInformation : InteractWithPlayerModule(), IStaffCommand {
    override val item: ItemStack = staffItem(Material.BOOK) {
        meta {
            name = "${ChatColor.RED}Player Information"
        }
    }

    override val onRightClickItem: PlayerInteractAtEntityEvent.() -> Unit = {
        sendInformation(player, rightClicked as? Player)
    }

    private fun sendInformation(staff: Player, target: Player?) {
        if (target == null) return
        val hgPlayer = target.hgPlayer
        staff.sendMessage("${ChatColor.DARK_GRAY}${ChatColor.STRIKETHROUGH}             ${ChatColor.DARK_GRAY}| ${ChatColor.DARK_PURPLE}Staff ${ChatColor.DARK_GRAY}|${ChatColor.DARK_PURPLE}             ")
        staff.sendMessage(" ${ChatColor.DARK_GRAY}| ${ChatColor.GRAY}Information about ${ChatColor.LIGHT_PURPLE}${target.name}")
        staff.sendMessage("   ${ChatColor.DARK_GRAY}• ${ChatColor.GRAY}Kills ${ChatColor.DARK_GRAY}» ${ChatColor.LIGHT_PURPLE}${hgPlayer.kills}")
        staff.sendMessage("   ${ChatColor.DARK_GRAY}• ${ChatColor.GRAY}Remaining offline time ${ChatColor.DARK_GRAY}» ${ChatColor.LIGHT_PURPLE}${hgPlayer.offlineTime.get()} seconds")
        staff.sendMessage("   ${ChatColor.DARK_GRAY}• ${ChatColor.GRAY}Was in Arena ${ChatColor.DARK_GRAY}» ${ChatColor.LIGHT_PURPLE}${hgPlayer.wasInArena}")
        staff.sendMessage("${ChatColor.DARK_GRAY}${ChatColor.STRIKETHROUGH}                                    ")
    }

    override val command = staffCommand("info") { sender, args ->
        val targetName = args[0]
        val target = Bukkit.getPlayer(targetName)

        if (target == null) {
            sender.sendMessage("${StaffMode.prefix}${ChatColor.RED}This player is not online.")
            return@staffCommand
        }

        sendInformation(sender, target)
    }

    override val commandUsage = "/staff info <player>"

    override val description = "Display information about a player"
}