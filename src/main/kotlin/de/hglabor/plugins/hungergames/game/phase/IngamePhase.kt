package de.hglabor.plugins.hungergames.game.phase

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.event.PlayerKilledEntityEvent
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.agnikai.Agnikai
import de.hglabor.plugins.hungergames.game.mechanics.DeathMessages
import de.hglabor.plugins.hungergames.game.mechanics.OfflineTimer
import de.hglabor.plugins.hungergames.game.phase.phases.InvincibilityPhase
import de.hglabor.plugins.hungergames.game.phase.phases.PvPPhase
import de.hglabor.plugins.hungergames.player.HGPlayer
import de.hglabor.plugins.hungergames.player.PlayerStatus
import de.hglabor.plugins.hungergames.player.hgPlayer
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.runnables.taskRunLater
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.concurrent.atomic.AtomicLong

open class IngamePhase(maxDuration: Long, nextPhase: GamePhase) : GamePhase(maxDuration, nextPhase) {
    override fun getTimeString(): String = ""
    override val timeName: String = ""

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity

        if (player.hgPlayer.status == PlayerStatus.INGAME && Agnikai.isOpen && player.hgPlayer !in Agnikai.wasInAgnikai) {
            Agnikai.queuePlayer(player)
        } else {
            player.hgPlayer.status = PlayerStatus.ELIMINATED
            taskRunLater(1) { player.spigot().respawn() }
            player.gameMode = GameMode.SPECTATOR
            DeathMessages.announce(event)
            if (event.entity.killer != null) {
                val killer = event.entity.killer
                Bukkit.getPluginManager().callEvent(PlayerKilledEntityEvent(killer, player))
            }
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
                player.sendMessage("${Prefix}Hurry up! The game just started.")
            }
        } else if (GameManager.phase == PvPPhase) {
            if (hgPlayer.status == PlayerStatus.LOBBY) {
                hgPlayer.status = PlayerStatus.SPECTATOR
                player.sendMessage("${Prefix}You are too late, the game has already started.")
                player.gameMode = GameMode.SPECTATOR
            }
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        OfflineTimer.putAndStartTimer(event.player.hgPlayer)
    }
}
