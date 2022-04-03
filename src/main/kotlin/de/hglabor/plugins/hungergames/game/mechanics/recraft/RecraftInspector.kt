package de.hglabor.plugins.hungergames.game.mechanics.recraft

import de.hglabor.plugins.hungergames.player.PlayerList.alivePlayers

class RecraftInspector {
    private val maxRecraftAmount = 64
    fun tick() {
        alivePlayers.forEach { hgPlayer ->
            hgPlayer.bukkitPlayer?.let { player ->
                val recraft = hgPlayer.recraft
                recraft.calcRecraft(player.inventory.contents)
                if (recraft.recraftPoints > maxRecraftAmount) {
                    while (recraft.recraftPoints > maxRecraftAmount) {
                        recraft.decrease(player, 1)
                    }
                }
            }
        }
    }
}