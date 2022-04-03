package de.hglabor.plugins.hungergames.game.mechanics.feast

import net.axay.kspigot.event.listen
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent

object FeastListener {
    fun register() {
        listen<BlockBreakEvent> {
            if (it.block.hasMetadata(Feast.BLOCK_KEY)) {
                it.isCancelled = true
            }
        }
        
        listen<BlockPlaceEvent> {
            if (it.block.hasMetadata(Feast.BLOCK_KEY)) {
                it.isCancelled = true
            }
        }
        
        listen<EntityExplodeEvent> {
            if (it.blockList().removeAll(it.blockList().filter { it.hasMetadata(Feast.BLOCK_KEY) })) {
                it.isCancelled = true
            }
        }
    }
}