package de.hglabor.plugins.hungergames.game

import de.hglabor.plugins.hungergames.Manager
import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.SecondaryColor
import de.hglabor.plugins.hungergames.game.agnikai.Agnikai
import de.hglabor.plugins.hungergames.game.mechanics.feast.Feast
import de.hglabor.plugins.hungergames.game.phase.GamePhase
import de.hglabor.plugins.hungergames.game.phase.phases.InvincibilityPhase
import de.hglabor.plugins.hungergames.game.phase.phases.LobbyPhase
import de.hglabor.plugins.hungergames.game.phase.phases.PvPPhase
import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.hungergames.utils.TimeConverter
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
import org.bukkit.Difficulty
import org.bukkit.entity.EntityType
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.player.PlayerJoinEvent
import java.util.concurrent.atomic.AtomicLong

object GameManager {
    val world = Bukkit.getWorld("world")
    var phase: GamePhase = LobbyPhase
    var elapsedTime: AtomicLong = AtomicLong(0)
    var feast: Feast? = null

    fun enable() {
        world.difficulty = Difficulty.NORMAL
        phase.start()
        Bukkit.getPluginManager().registerEvents(phase, Manager)
        world.setSpawnLocation(0, world.getHighestBlockYAt(0, 0) + 15, 0)
        world.loadChunk(world.spawnLocation.chunk)
        world.worldBorder.setCenter(0.0, 0.0)
        world.worldBorder.size = 600.0*2
        listen<PlayerJoinEvent> { it.player.hgPlayer.login() }
        listen<EntitySpawnEvent> {
            when(it.entity.type) {
                EntityType.CREEPER, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME, EntityType.WITCH, EntityType.ZOMBIE,
                EntityType.CAVE_SPIDER, EntityType.ENDERMAN, EntityType.SPIDER -> it.isCancelled = true
                else -> {}
            }
        }

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
        val remaining = "${ChatColor.WHITE}${TimeConverter.stringify(phase.remainingTime.toInt())}${ChatColor.GRAY}"
        when (phase) {
            LobbyPhase -> {
                when (LobbyPhase.remainingTime.toInt()) {
                    60, 30, 20, 10, 3, 2, 1 -> broadcast("${Prefix}The HungerGames are starting in ${remaining}.")
                    0 -> onlinePlayers.forEach { it.title("${SecondaryColor}gl hf") }
                }
            }

            InvincibilityPhase -> {
                when (InvincibilityPhase.remainingTime.toInt()) {
                    60, 30, 20, 10, 3, 2, 1 -> broadcast("${Prefix}The invincibility period ends in ${remaining}.")
                }
            }

            PvPPhase -> {
                when (PvPPhase.remainingTime.toInt()) {
                    60, 30, 20, 10, 3, 2, 1 -> broadcast("${Prefix}The player with the most eliminations wins in ${remaining}.")
                }

                if (Agnikai.isOpen && elapsedTime.toInt() > 900) {
                    Agnikai.isOpen = false
                    broadcast("${Agnikai.Prefix}${ChatColor.RED}The Agnikai has been closed!")
                    Agnikai.task?.cancel()
                    Agnikai.task = null
                }
            }
        }
    }
}