package de.hglabor.plugins.hungergames.staff.module.implementation

import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.hungergames.player.staffPlayer
import de.hglabor.plugins.hungergames.staff.StaffMode
import de.hglabor.plugins.hungergames.staff.module.InteractModule
import de.hglabor.plugins.hungergames.staff.module.command.IStaffCommand
import de.hglabor.plugins.hungergames.staff.module.command.staffCommand
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

object RandomTP : InteractModule(), IStaffCommand {
    override val item: ItemStack = staffItem(Material.SKULL_ITEM) {
        meta {
            name = "${ChatColor.RED}Random Teleport"
        }
    }

    override val onRightClickItem: PlayerInteractEvent.() -> Unit = {
        teleportRandom(player)
    }

    private fun teleportRandom(staff: Player) {
        val destination = PlayerList.alivePlayers.randomOrNull()?.bukkitPlayer?.location
        if (destination == null) {
            staff.sendMessage("${StaffMode.prefix}${ChatColor.RED}Es wurde kein Spieler gefunden.")
            return
        }
        staff.teleport(destination)
    }

    override val command = staffCommand("randomtp") { sender, _ ->
        teleportRandom(sender)
    }

    override val commandUsage = "/staff randomtp"

    override val description = "Teleport to a random player"
}