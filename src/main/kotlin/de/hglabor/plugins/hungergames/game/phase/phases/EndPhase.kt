package de.hglabor.plugins.hungergames.game.phase.phases

import de.hglabor.plugins.hungergames.game.phase.GamePhase
import net.axay.kspigot.extensions.broadcast
import org.bukkit.Bukkit

object EndPhase: GamePhase(25, null) {
    override fun getTimeString() = "Ended"

    override fun onStart() {
        broadcast("EndPhase")
    }

    override fun tick() {
        if (elapsedTime == 20L) {
            Bukkit.shutdown()
        }
    }
}