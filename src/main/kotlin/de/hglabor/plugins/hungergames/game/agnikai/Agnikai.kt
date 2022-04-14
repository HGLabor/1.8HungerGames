package de.hglabor.plugins.hungergames.game.agnikai

import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.player.HGPlayer
import de.hglabor.plugins.hungergames.player.PlayerStatus
import de.hglabor.plugins.hungergames.player.hgPlayer
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.extensions.bukkit.give
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack


object Agnikai {
    var isOpen = true
    val queuedPlayers = mutableListOf<HGPlayer>()
    val currentlyFighting = mutableListOf<HGPlayer>()
    var task: KSpigotRunnable? = null

    fun queuePlayer(player: Player) {
        player.spigot().respawn()
        player.teleport(Location(Bukkit.getWorld("arena"), 0.0, 0.0, 0.0))
        player.hgPlayer.status = PlayerStatus.GULAG
        queuedPlayers += player.hgPlayer
        player.hgPlayer.wasInAgnikai = true
    }

    private fun startNewMatch() {
        if (currentlyFighting.isNotEmpty()) return
        if (queuedPlayers.size >= 2) {
            currentlyFighting.addAll(queuedPlayers.take(2))
            queuedPlayers.removeAll(currentlyFighting)

            broadcast("Now fighting: ${currentlyFighting.joinToString { it.name } }")

            currentlyFighting.forEach { fighting ->
                fighting.bukkitPlayer?.give(ItemStack(Material.STONE_SWORD))
            }
        }
    }

    fun register() {
        task = task(true, 20, 20) {
            startNewMatch()

            if (isOpen && GameManager.elapsedTime.toInt() > 900) {
                isOpen = false
                it.cancel()
                task = null
            }
        }

        listen<PlayerDeathEvent> {
            if (it.entity.world != Bukkit.getWorld("arena")) return@listen
            if (it.entity.hgPlayer !in currentlyFighting) return@listen
            endFight(it.entity)
        }

        listen<EntityDamageByEntityEvent> {
            if(it.entity.world != Bukkit.getWorld("arena")) return@listen
            val entity = it.entity
            val damager = it.damager
            if (entity !is Player || damager !is Player) {
                it.isCancelled = true
                return@listen
            }

            if (entity.hgPlayer !in currentlyFighting || damager.hgPlayer !in currentlyFighting) {
                it.isCancelled = true
                return@listen
            }
        }

        listen<PlayerQuitEvent> {
            val player = it.player
            if(player.world != Bukkit.getWorld("arena")) return@listen
            if (player.hgPlayer in queuedPlayers) {
                player.hgPlayer.status = PlayerStatus.ELIMINATED
                queuedPlayers.remove(player.hgPlayer)
            }

            if (player.hgPlayer in currentlyFighting) {
                endFight(player)
            }
        }
    }

    private fun endFight(loser: Player) {
        val winner = currentlyFighting.first { op -> op != loser.hgPlayer }
        currentlyFighting.clear()
        broadcast("${ChatColor.GREEN}${loser.name} lost against ${winner.name}")
        winner.makeGameReady()
    }
}
