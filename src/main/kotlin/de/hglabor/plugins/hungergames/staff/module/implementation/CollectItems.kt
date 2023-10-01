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
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.inventory.ItemStack

object CollectItems : InteractModule(), IStaffCommand {
    override val item: ItemStack = staffItem(Material.LEASH) {
        meta {
            name = "${KColors.RED}Toggle Collecting Items"
        }
    }

    override val onRightClickItem: PlayerInteractEvent.() -> Unit = {
        player.staffPlayer?.toggleCollectingItems()
    }

    init {
        listen<PlayerPickupItemEvent> {
            handlePickUpEvent(it.player, it)
        }
    }

    private fun handlePickUpEvent(player: Player, event: Cancellable) {
        val staffPlayer = PlayerList.getStaffPlayer(player) ?: return
        if (!staffPlayer.isStaffMode) return
        if (!staffPlayer.canCollectItems) event.isCancelled = true
    }

    override val command = staffCommand("collectitems") { sender, _ ->
        sender.staffPlayer?.toggleCollectingItems()
    }

    override val commandUsage = "/staff collectitems"

    override val description = "Toggle collecting items"
}