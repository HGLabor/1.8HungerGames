package de.hglabor.plugins.hungergames.game.phase.phases

import de.hglabor.plugins.hungergames.game.phase.GamePhase
import net.axay.kspigot.extensions.broadcast

object PvPPhase: GamePhase(30*60, EndPhase) {
    override fun onStart() {
        broadcast("PvPPhase")
    }

    override fun getTimeString(): String {
        TODO("Not yet implemented")
    }
}