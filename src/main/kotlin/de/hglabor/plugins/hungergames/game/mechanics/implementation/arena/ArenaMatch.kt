package de.hglabor.plugins.hungergames.game.mechanics.implementation.arena

import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.mechanics.implementation.DeathMessages
import de.hglabor.plugins.hungergames.player.HGPlayer
import de.hglabor.plugins.hungergames.player.PlayerStatus
import de.hglabor.plugins.hungergames.player.hgPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.axay.kspigot.event.SingleListener
import net.axay.kspigot.event.listen
import net.axay.kspigot.event.unregister
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.extensions.bukkit.give
import net.axay.kspigot.extensions.bukkit.isFeetInWater
import net.axay.kspigot.extensions.bukkit.title
import net.axay.kspigot.extensions.geometry.add
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import java.util.concurrent.atomic.AtomicInteger

class ArenaMatch(vararg val players: HGPlayer) {
    companion object {
        const val MAX_DURATION = 60
        private val coroutineScope = CoroutineScope(Dispatchers.IO)
    }

    val timer = AtomicInteger(-4)
    var isEnded = false
    private val listeners = mutableListOf<SingleListener<out Event>>()

    fun start() {
        registerListeners()
        broadcast("${Arena.Prefix}Starting a fight between ${players.joinToString(" ${ChatColor.GRAY}and ") { "${ChatColor.WHITE}${it.name}" }}${ChatColor.GRAY}.")
        players.forEachIndexed { index, hgPlayer ->
            hgPlayer.bukkitPlayer?.let { player ->
                val loc = if (index == 1) ArenaWorld.spawn1Location else ArenaWorld.spawn2Location
                player.teleport(loc)
                player.give(ItemStack(Material.STONE_SWORD))
                repeat(8) {
                    player.give(ItemStack(Material.MUSHROOM_SOUP))
                }
            }
        }
    }

    fun tick() {
        val currentTimer = timer.getAndIncrement()
        sendCountdown(currentTimer)
        if (checkIfPlayerIsInWater())
            if (currentTimer >= MAX_DURATION) end(null)
    }

    private fun sendCountdown(time: Int) {
        if (time > 0) return
        players.forEach { fighting ->
            fighting.bukkitPlayer?.title(
                when (timer.get()) {
                    -3 -> "${ChatColor.RED}3"
                    -2 -> "${ChatColor.YELLOW}2"
                    -1 -> "${ChatColor.DARK_GREEN}1"
                    0 -> "${ChatColor.GREEN}Go!"
                    else -> " "
                }
            )
        }
    }

    private fun checkIfPlayerIsInWater(): Boolean {
        players.forEach { hgPlayer ->
            if (hgPlayer.bukkitPlayer?.isFeetInWater == true) {
                end(hgPlayer.bukkitPlayer)
                return true
            }
        }
        return false
    }

    private fun end(loser: Player?) {
        isEnded = true
        players.forEach { it.setGameScoreboard(true) }

        if (loser != null) {
            val winner = players.first { op -> op != loser.hgPlayer }
            DeathMessages.announceArenaDeath(winner, loser.hgPlayer)
            winner.makeGameReady()
            winner.bukkitPlayer?.inventory?.apply {
                addItem(ItemStack(Material.STONE_SWORD))
                for (i in 0..35) {
                    addItem(ItemStack(Material.MUSHROOM_SOUP))
                }
            }
        } else {
            broadcast(
                "${Arena.Prefix}Current fight ${ChatColor.RED}timed out${ChatColor.GRAY}. Eliminating both, ${
                    players.joinToString(" ${ChatColor.GRAY}and ") { "${ChatColor.WHITE}${it.name}" }
                }${ChatColor.GRAY}."
            )
            players.forEach { fighting ->
                fighting.bukkitPlayer?.inventory?.clear()
                fighting.bukkitPlayer?.gameMode = GameMode.SPECTATOR
                fighting.bukkitPlayer?.teleport(GameManager.world.spawnLocation.clone().add(0, 10, 0))
            }
        }
        listeners.onEach { it.unregister() }
    }

    fun registerListeners() {
        listeners.addAll(
            listOf(
                listen<PlayerDeathEvent> {
                    if (it.entity.world != ArenaWorld.world) return@listen
                    if (it.entity.hgPlayer !in players) return@listen
                    it.deathMessage = null
                    end(it.entity)
                },

                listen<EntityDamageByEntityEvent> {
                    if (it.entity.world != ArenaWorld.world) return@listen
                    val entity = it.entity
                    val damager = it.damager
                    if (entity !is Player || damager !is Player || timer.get() <= 0) {
                        it.isCancelled = true
                        return@listen
                    }

                    if (entity.hgPlayer !in players || damager.hgPlayer !in players || timer.get() < 0) {
                        it.isCancelled = true
                        return@listen
                    }
                },

                listen<PlayerQuitEvent> {
                    val player = it.player
                    if (it.player.world != ArenaWorld.world) return@listen
                    if (player.hgPlayer in Arena.queuedPlayers) {
                        player.hgPlayer.status = PlayerStatus.ELIMINATED
                        Arena.queuedPlayers.remove(player.hgPlayer)
                        player.teleport(GameManager.world.spawnLocation)
                        player.gameMode = GameMode.SPECTATOR
                    }

                    if (player.hgPlayer in players) {
                        player.hgPlayer.status = PlayerStatus.ELIMINATED
                        Arena.queuedPlayers.remove(player.hgPlayer)
                        player.teleport(GameManager.world.spawnLocation)
                        player.gameMode = GameMode.SPECTATOR
                        end(player)
                    }
                }
            )
        )
    }
}