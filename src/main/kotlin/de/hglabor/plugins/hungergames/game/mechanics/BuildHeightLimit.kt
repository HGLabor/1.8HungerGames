package de.hglabor.plugins.hungergames.game.mechanics

import net.axay.kspigot.event.listen
import org.bukkit.event.block.BlockPlaceEvent

object BuildHeightLimit {
    fun register() {
        listen<BlockPlaceEvent> {
            if (it.block.location.y >= 120) it.isCancelled = true
        }
    }
}