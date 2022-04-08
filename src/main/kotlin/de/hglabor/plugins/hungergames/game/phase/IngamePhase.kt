package de.hglabor.plugins.hungergames.game.phase

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.mechanics.DeathMessages
import de.hglabor.plugins.hungergames.game.mechanics.OfflineTimer
import de.hglabor.plugins.hungergames.game.phase.phases.InvincibilityPhase
import de.hglabor.plugins.hungergames.game.phase.phases.PvPPhase
import de.hglabor.plugins.hungergames.player.PlayerStatus
import de.hglabor.plugins.hungergames.player.hgPlayer
import net.axay.kspigot.runnables.taskRunLater
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

open class IngamePhase(maxDuration: Long, nextPhase: GamePhase): GamePhase(maxDuration, nextPhase) {
    override fun getTimeString(): String = ""
    override val timeName: String = ""

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity
        player.hgPlayer.status = PlayerStatus.ELIMINATED
        taskRunLater(1) { player.spigot().respawn() }
        player.gameMode = GameMode.SPECTATOR
        DeathMessages.announce(event)

        if (player.killer != null) {
            val killer = player.killer ?: return
            killer.hgPlayer.kills.incrementAndGet()
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val hgPlayer = player.hgPlayer

        if (hgPlayer.status == PlayerStatus.ELIMINATED) {
            hgPlayer.status = PlayerStatus.SPECTATOR
            player.gameMode = GameMode.SPECTATOR
        }

        if (GameManager.phase == InvincibilityPhase) {
            if (hgPlayer.status == PlayerStatus.LOBBY) {
                hgPlayer.login()
                hgPlayer.makeGameReady()
                player.sendMessage("${Prefix}The game has already started. You should hurry now!")
            }
        } else if (GameManager.phase == PvPPhase) {
            if (hgPlayer.status == PlayerStatus.LOBBY) {
                hgPlayer.status = PlayerStatus.SPECTATOR
                player.sendMessage("${Prefix}The game has already started.")
                player.gameMode = GameMode.SPECTATOR
            }
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        OfflineTimer.putAndStartTimer(event.player.hgPlayer)
    }
}