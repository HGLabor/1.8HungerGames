package de.hglabor.plugins.hungergames.game.agnikai

import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.phase.phases.EndPhase
import de.hglabor.plugins.hungergames.player.HGPlayer
import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.hungergames.player.PlayerStatus
import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.hungergames.scoreboard.setScoreboard
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.extensions.bukkit.give
import net.axay.kspigot.extensions.bukkit.title
import net.axay.kspigot.extensions.geometry.add
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import java.util.concurrent.atomic.AtomicInteger

object Agnikai {
    val world = Bukkit.getWorld("arena").apply {
        setGameRuleValue("doMobSpawnIng", "false")
    }
    var isOpen = true
    val queuedPlayers = mutableListOf<HGPlayer>()
    val currentlyFighting = mutableListOf<HGPlayer>()
    var timer = AtomicInteger(-3)
    var task: KSpigotRunnable? = null

    fun queuePlayer(player: Player) {
        player.spigot().respawn()
        player.teleport(Location(world, 20.0, 0.0, 0.0))
        player.hgPlayer.status = PlayerStatus.GULAG
        queuedPlayers += player.hgPlayer
        player.hgPlayer.wasInAgnikai = true

        player.setScoreboard {
            title = "${ChatColor.AQUA}${ChatColor.BOLD}HG${ChatColor.WHITE}${ChatColor.BOLD}Labor.de"
            period = 4
            content {
                +" "
                +{ "${ChatColor.GREEN}${ChatColor.BOLD}Players:#${ChatColor.WHITE}${PlayerList.getShownPlayerCount()}" }
                +{ "${ChatColor.YELLOW}${ChatColor.BOLD}${GameManager.phase.timeName}:#${ChatColor.WHITE}${GameManager.phase.getTimeString()}" }
                +" "
                +{ "${ChatColor.AQUA}${ChatColor.BOLD}Waiting:#${ChatColor.WHITE}${queuedPlayers.size}" }
                +{ "${ChatColor.RED}${ChatColor.BOLD}Fighting:" }
                +{ "  ${ChatColor.GRAY}-#${currentlyFighting.getOrNull(0)?.name ?: "None" }" }
                +{ "  ${ChatColor.GRAY}-#${currentlyFighting.getOrNull(1)?.name ?: "None" }" }
                +" "
            }
        }
    }

    private fun startNewMatch() {
        if (currentlyFighting.isNotEmpty()) return
        if (queuedPlayers.size >= 2) {
            currentlyFighting.addAll(queuedPlayers.take(2))
            queuedPlayers.removeAll(currentlyFighting)
            broadcast("Now fighting: ${currentlyFighting.joinToString { it.name }}")
            giveKits()
            timer.set(-3)
        }
    }

    private fun giveKits() {
        currentlyFighting.forEach { fighting ->
            fighting.bukkitPlayer?.let { player ->
                player.teleport(world.getHighestBlockAt(0, 0).location.add(0, 1, 0))
                player.give(ItemStack(Material.STONE_SWORD))
                repeat(8) {
                    player.give(ItemStack(Material.MUSHROOM_SOUP))
                }
            }
        }
    }

    fun register() {
        task = task(true, 20, 20) {
            if (GameManager.phase == EndPhase) {
                it.cancel()
                task = null
                return@task
            }

            if (currentlyFighting.isEmpty()) {
                startNewMatch()
            } else {
                if (timer.get() <= 1) {
                    currentlyFighting.forEach { fighting ->
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

                if (timer.get() == 60) {
                    endFight(null)
                } else {
                    timer.incrementAndGet()
                }
            }

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
            if (it.entity.world != Bukkit.getWorld("arena")) return@listen
            val entity = it.entity
            val damager = it.damager
            if (entity !is Player || damager !is Player) {
                it.isCancelled = true
                return@listen
            }

            if (entity.hgPlayer !in currentlyFighting || damager.hgPlayer !in currentlyFighting || timer.get() < 0) {
                it.isCancelled = true
                return@listen
            }
        }

        listen<PlayerQuitEvent> {
            val player = it.player
            if (player.world != Bukkit.getWorld("arena")) return@listen
            if (player.hgPlayer in queuedPlayers) {
                player.hgPlayer.status = PlayerStatus.ELIMINATED
                queuedPlayers.remove(player.hgPlayer)
            }

            if (player.hgPlayer in currentlyFighting) {
                endFight(player)
            }
        }
    }

    private fun endFight(loser: Player?) {
        currentlyFighting.forEach { it.setGameScoreboard(true) }
        if (loser != null) {
            val winner = currentlyFighting.first { op -> op != loser.hgPlayer }
            broadcast("${ChatColor.GREEN}${loser.name} lost against ${winner.name}")
            winner.makeGameReady()
            winner.bukkitPlayer?.inventory?.apply {
                addItem(ItemStack(Material.STONE_SWORD))
                for (i in 0..35) {
                    addItem(ItemStack(Material.MUSHROOM_SOUP))
                }
            }
        } else {
            broadcast("${ChatColor.RED}Current Agnikai fight timed out. Removing ${currentlyFighting.joinToString { it.name }}")
            currentlyFighting.forEach { fighting ->
                fighting.bukkitPlayer?.inventory?.clear()
                fighting.bukkitPlayer?.gameMode = GameMode.SPECTATOR
                fighting.bukkitPlayer?.teleport(GameManager.world.spawnLocation.clone().add(0, 10, 0))
            }
        }
        currentlyFighting.clear()
    }
}
