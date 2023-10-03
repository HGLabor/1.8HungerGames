package de.hglabor.plugins.hungergames.staff.module.implementation

import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.hungergames.player.staffPlayer
import de.hglabor.plugins.hungergames.staff.StaffMode
import de.hglabor.plugins.hungergames.staff.module.InteractModule
import de.hglabor.plugins.hungergames.staff.module.command.IStaffCommand
import de.hglabor.plugins.hungergames.staff.module.command.staffCommand
import net.axay.kspigot.event.listen
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent

object Visibility : InteractModule(), IStaffCommand{
    override val item = staffItem(Material.GLASS_BOTTLE) {
        meta {
            name = "${ChatColor.RED}Toggle Visibility"
        }
    }

    override val onRightClickItem: PlayerInteractEvent.() -> Unit = {
        player.staffPlayer?.toggleVisibility()
    }

    init {
        listen<PlayerJoinEvent> {
            PlayerList.staffPlayers.filter { staff -> !staff.isVisible }.forEach { staff ->
                StaffMode.hide(staff)
            }
        }
    }

    override val command = staffCommand("visibility") { sender, _ ->
        sender.staffPlayer?.toggleBuildMode()
    }

    override val commandUsage = "/staff visibility"

    override val description = "Toggle Visibility"
}