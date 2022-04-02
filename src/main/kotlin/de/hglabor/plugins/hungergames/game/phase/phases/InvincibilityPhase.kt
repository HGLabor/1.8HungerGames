package de.hglabor.plugins.hungergames.game.phase.phases

import de.hglabor.plugins.hungergames.game.phase.GamePhase
import net.axay.kspigot.extensions.broadcast

object InvincibilityPhase: GamePhase(120, PvPPhase) {
    override fun onStart() {
        broadcast("InvincibilityPhase")
    }

    override fun getTimeString(): String {
        TODO("Not yet implemented")
    }
}