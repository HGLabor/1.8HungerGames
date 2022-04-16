package de.hglabor.plugins.hungergames.game.mechanics

import de.hglabor.plugins.hungergames.player.hgPlayer
import net.axay.kspigot.event.listen
import org.bukkit.Material
import org.bukkit.event.block.BlockBreakEvent

object BlocksToInv {
    fun register() {
        listen<BlockBreakEvent> {
            if (it.isCancelled) return@listen
            if (it.player.hgPlayer.isInCombat) return@listen
            when (it.block.type) {
                Material.LOG,
                Material.LOG_2,
                Material.COBBLESTONE,
                Material.RED_MUSHROOM,
                Material.BROWN_MUSHROOM,
                Material.STONE -> {
                    val player = it.player
                    if (player.inventory.contents.any { block -> block == null || block.type == Material.AIR }) {
                        player.inventory.addItem(*it.block.drops.toTypedArray())
                        it.isCancelled = true
                        it.block.type = Material.AIR
                    }
                }
                else -> {}
            }
        }
    }
}