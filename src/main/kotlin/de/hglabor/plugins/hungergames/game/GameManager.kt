package de.hglabor.plugins.hungergames.game

import de.hglabor.plugins.hungergames.Manager
import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.SecondaryColor
import de.hglabor.plugins.hungergames.game.mechanics.feast.Feast
import de.hglabor.plugins.hungergames.game.phase.GamePhase
import de.hglabor.plugins.hungergames.game.phase.phases.InvincibilityPhase
import de.hglabor.plugins.hungergames.game.phase.phases.LobbyPhase
import de.hglabor.plugins.hungergames.game.phase.phases.PvPPhase
import de.hglabor.plugins.hungergames.player.hgPlayer
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.extensions.bukkit.title
import net.axay.kspigot.extensions.onlinePlayers
import net.axay.kspigot.runnables.sync
import net.axay.kspigot.runnables.task
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.world.ChunkUnloadEvent
import java.util.concurrent.atomic.AtomicLong

object GameManager {
    val world = Bukkit.getWorld("world")
    var phase: GamePhase = LobbyPhase
    var elapsedTime: AtomicLong = AtomicLong(0)
    var feast: Feast? = null

    fun enable() {
        phase.start()
        Bukkit.getPluginManager().registerEvents(phase, Manager)
        world.setSpawnLocation(0, world.getHighestBlockYAt(0, 0) + 15, 0)
        world.loadChunk(world.spawnLocation.chunk)
        world.worldBorder.setCenter(0.0, 0.0)
        world.worldBorder.size = 600.0*2
        listen<ChunkUnloadEvent> { if (it.chunk == world.spawnLocation.chunk) it.isCancelled = true }
        listen<PlayerJoinEvent> { it.player.hgPlayer.login() }

        runBlocking {
            launch {
                startTimer()
            }
        }
        task(true, 20, 20) { phase.tick(phase.tickCount++) }
    }

    fun startNextPhase() {
        HandlerList.unregisterAll(phase)
        val newPhase = phase.nextPhase ?: return
        phase = newPhase
        Bukkit.getPluginManager().registerEvents(phase, Manager)
        phase.start()
        elapsedTime.set(0)
    }

    private suspend fun startTimer() {
        task(false, 20, 20) {
            if (elapsedTime.get() == phase.maxDuration) {
                sync {
                    startNextPhase()
                }
                return@task
            }
            phaseBroadcasts()
            phase.incrementElapsedTime()
        }
    }

    private fun phaseBroadcasts() {
        when (phase) {
            LobbyPhase -> {
                when (LobbyPhase.remainingTime.toInt()) {
                    60, 30, 20, 10, 3, 2, 1 -> broadcast("${Prefix}The HungerGames are starting in ${ChatColor.WHITE}${LobbyPhase.getTimeString()}${ChatColor.GRAY}.")
                    0 -> onlinePlayers.forEach { it.title("${SecondaryColor}gl hf") }
                }
            }

            InvincibilityPhase -> {
                when (InvincibilityPhase.remainingTime.toInt()) {
                    60, 30, 20, 10, 3, 2, 1 -> broadcast("${Prefix}The invincibility period ends in ${ChatColor.WHITE}${InvincibilityPhase.getTimeString()}${ChatColor.GRAY}.")
                }
            }

            PvPPhase -> {
                when (PvPPhase.remainingTime.toInt()) {
                    60, 30, 20, 10, 3, 2, 1 -> broadcast("${Prefix}The player with the most eliminations wins in ${ChatColor.WHITE}${LobbyPhase.getTimeString()}${ChatColor.GRAY}.")
                }
            }
        }
    }
}