package de.hglabor.plugins.hungergames.staff.module

import de.hglabor.plugins.hungergames.player.PlayerList
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.events.isRightClick
import org.bukkit.GameMode
import org.bukkit.event.player.PlayerInteractEvent

abstract class InteractModule: StaffModule {
    abstract val onRightClickItem: (PlayerInteractEvent.() -> Unit)

    init {
        listen<PlayerInteractEvent> {
            val player = it.player
            if (player.gameMode != GameMode.CREATIVE) return@listen
            val staffPlayer = PlayerList.getStaffPlayer(player) ?: return@listen
            if (!it.action.isRightClick) return@listen
            if (!staffPlayer.isStaffMode) return@listen
            if (!player.inventory.itemInHand.equals(item)) return@listen
            it.isCancelled = true
            onRightClickItem.invoke(it)
        }
    }
}