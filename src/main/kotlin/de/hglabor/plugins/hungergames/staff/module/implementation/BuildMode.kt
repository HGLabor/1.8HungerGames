package de.hglabor.plugins.hungergames.staff.module.implementation

import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.hungergames.player.staffPlayer
import de.hglabor.plugins.hungergames.staff.module.InteractModule
import de.hglabor.plugins.hungergames.staff.module.command.IStaffCommand
import de.hglabor.plugins.hungergames.staff.module.command.staffCommand
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.event.listen
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

object BuildMode : InteractModule(), IStaffCommand {
    override var item: ItemStack = staffItem(Material.COBBLESTONE) {
        meta {
            name = "${KColors.RED}Toggle Buildmode"
        }
    }

    override val onRightClickItem: PlayerInteractEvent.() -> Unit = {
        player.staffPlayer?.toggleBuildMode()
    }

    init {
        listen<BlockBreakEvent> {
            handleBuildEvent(it.player, it)
        }

        listen<BlockPlaceEvent> {
            handleBuildEvent(it.player, it)
        }
    }

    private fun handleBuildEvent(player: Player, event: Cancellable) {
        if (player.world.name != "world") return
        val staffPlayer = PlayerList.getStaffPlayer(player) ?: return
        if (!staffPlayer.isStaffMode) return
        if (!staffPlayer.isBuildMode) event.isCancelled = true
    }

    override val command = staffCommand("build") { sender, _ ->
        sender.staffPlayer?.toggleBuildMode()
    }

    override val commandUsage = "/staff build"

    override val description = "Toggle Buildmode"
}