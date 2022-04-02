package de.hglabor.plugins.hungergames.game.phase.phases

import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.phase.GamePhase
import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.hungergames.scoreboard.setScoreboard
import net.axay.kspigot.extensions.broadcast
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object LobbyPhase: GamePhase(120, InvincibilityPhase) {
    override fun onStart() {
        broadcast("Lobby!")
    }

    override fun tick() {

    }

    override fun getTimeString(): String {
        TODO("Not yet implemented")
    }

    override fun incrementElapsedTime() {
        //TODO if (PlayerList.) >= 2
        elapsedTime++
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.joinMessage = null
        PlayerList.getPlayer(event.player)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        event.quitMessage = null
        PlayerList.remove(event.player.uniqueId)
    }
}