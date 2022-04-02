package de.hglabor.plugins.hungergames.game

import de.hglabor.plugins.hungergames.Manager
import de.hglabor.plugins.hungergames.game.phase.GamePhase
import de.hglabor.plugins.hungergames.game.phase.phases.LobbyPhase
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList

object GameManager {
    var phase: GamePhase = LobbyPhase

    fun enable() {
        phase.start()
        phase.tick()
    }

    fun startNextPhase() {
        HandlerList.unregisterAll(phase);
        val newPhase = phase.nextPhase ?: return
        phase = newPhase
        Bukkit.getPluginManager().registerEvents(phase, Manager);
        phase.start()
    }
}