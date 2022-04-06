package de.hglabor.plugins.hungergames.game.mechanics

import net.axay.kspigot.event.listen
import org.bukkit.Material
import org.bukkit.event.block.BlockBreakEvent

object WoodToInv {
    fun register() {
        listen<BlockBreakEvent> {
            if (it.isCancelled) return@listen
            if (it.block.type == Material.LOG || it.block.type == Material.LOG_2) {
                val player = it.player
                if (player.inventory.contents.any { block ->  block == null || block.type == Material.AIR }) {
                    player.inventory.addItem(*it.block.drops.toTypedArray())
                    it.isCancelled = true
                    it.block.type = Material.AIR
                }
            }
        }
    }
}