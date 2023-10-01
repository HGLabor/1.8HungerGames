package de.hglabor.plugins.hungergames.staff.module

import de.hglabor.plugins.hungergames.player.PlayerList
import net.axay.kspigot.event.listen
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

abstract class InteractWithPlayerModule: StaffModule {
    abstract val onRightClickItem: (PlayerInteractAtEntityEvent.() -> Unit)

    init {
        listen<PlayerInteractAtEntityEvent> {
            if (it.rightClicked !is Player) return@listen
            val player = it.player
            if (player.gameMode != GameMode.CREATIVE) return@listen
            val staffPlayer = PlayerList.getStaffPlayer(player) ?: return@listen
            if (!staffPlayer.isStaffMode) return@listen
            if (!player.inventory.itemInHand.equals(item)) return@listen
            it.isCancelled = true
            onRightClickItem.invoke(it)
        }

        listen<PlayerInteractEvent> {
            if (it.player.inventory.itemInHand.equals(item)) it.isCancelled = true
        }
    }
}