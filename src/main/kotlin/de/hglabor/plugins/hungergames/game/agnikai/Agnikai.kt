package de.hglabor.plugins.hungergames.game.agnikai

import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.mechanics.DeathMessages
import de.hglabor.plugins.hungergames.game.phase.phases.EndPhase
import de.hglabor.plugins.hungergames.player.HGPlayer
import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.hungergames.player.PlayerStatus
import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.hungergames.scoreboard.setScoreboard
import de.hglabor.plugins.hungergames.utils.TimeConverter
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.extensions.bukkit.give
import net.axay.kspigot.extensions.bukkit.title
import net.axay.kspigot.extensions.geometry.add
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import org.bukkit.*
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import java.util.concurrent.atomic.AtomicInteger

object Agnikai {
    val Prefix = " ${ChatColor.DARK_GRAY}| ${ChatColor.AQUA}Agnikai ${ChatColor.DARK_GRAY}» ${ChatColor.GRAY}"
    val world: World = Bukkit.getWorld("arena")
    private val queueLocation = Location(world, 1230.5, 13.0, 309.5, 0f, 0F)
    private val spawn1Location = Location(world, 1230.5, 5.0, 313.5, 0f, 0F)
    private val spawn2Location = Location(world, 1230.5, 5.0, 328.5, 180f, 0F)

    var isOpen = true
    val queuedPlayers = mutableListOf<HGPlayer>()
    val currentlyFighting = mutableListOf<HGPlayer>()
    private const val MAX_FIGHT_LENGTH = 60
    private var timer = AtomicInteger(-3)
    var task: KSpigotRunnable? = null

    fun queuePlayer(player: Player) {
        player.spigot().respawn()
        player.teleport(queueLocation)
        player.hgPlayer.status = PlayerStatus.GULAG
        queuedPlayers += player.hgPlayer
        player.hgPlayer.wasInAgnikai = true

        player.setScoreboard {
            title = "${ChatColor.AQUA}${ChatColor.BOLD}HG${ChatColor.WHITE}${ChatColor.BOLD}Labor.de"
            period = 4
            content {
                fun fightDuration(): String {
                    if (currentlyFighting.isEmpty()) return " "
                    if (timer.get() >= 0) return TimeConverter.stringify(MAX_FIGHT_LENGTH - timer.get())
                    return TimeConverter.stringify(MAX_FIGHT_LENGTH)
                }
                +" "
                +{ "${ChatColor.GREEN}${ChatColor.BOLD}Players:#${ChatColor.WHITE}${PlayerList.getShownPlayerCount()}" }
                +{ "${ChatColor.YELLOW}${ChatColor.BOLD}${GameManager.phase.timeName}:#${ChatColor.WHITE}${GameManager.phase.getTimeString()}" }
                +" "
                +{ "${ChatColor.AQUA}${ChatColor.BOLD}Waiting:#${ChatColor.WHITE}${queuedPlayers.size}" }
                +{ "${ChatColor.RED}${ChatColor.BOLD}Fighting:#${ChatColor.WHITE}${fightDuration()}" }
                +{ "  ${ChatColor.GRAY}-#${currentlyFighting.getOrNull(0)?.name ?: "None"}" }
                +{ "  ${ChatColor.GRAY}-#${currentlyFighting.getOrNull(1)?.name ?: "None"}" }
                +" "
            }
        }
    }

    private fun startNewMatch() {
        if (currentlyFighting.isNotEmpty()) return
        if (queuedPlayers.size >= 2) {
            currentlyFighting.addAll(queuedPlayers.take(2))
            queuedPlayers.removeAll(currentlyFighting)
            broadcast("${Prefix}Starting a fight between ${currentlyFighting.joinToString(" ${ChatColor.GRAY}and ") { "${ChatColor.WHITE}${it.name}" }}${ChatColor.GRAY}.")
            giveKits()
            timer.set(-3)
        }
    }

    private fun giveKits() {
        currentlyFighting.forEachIndexed { index, fighting ->
            fighting.bukkitPlayer?.let { player ->
                val loc = if (index == 1) spawn1Location else spawn2Location
                player.teleport(loc)
                player.give(ItemStack(Material.STONE_SWORD))
                repeat(8) {
                    player.give(ItemStack(Material.MUSHROOM_SOUP))
                }
            }
        }
    }

    private fun endFight(loser: Player?) {
        currentlyFighting.forEach { it.setGameScoreboard(true) }
        if (loser != null) {
            val winner = currentlyFighting.first { op -> op != loser.hgPlayer }
            DeathMessages.announceAgnikaiDeath(winner, loser.hgPlayer)
            winner.makeGameReady()
            winner.bukkitPlayer?.inventory?.apply {
                addItem(ItemStack(Material.STONE_SWORD))
                for (i in 0..35) {
                    addItem(ItemStack(Material.MUSHROOM_SOUP))
                }
            }
        } else {
            broadcast(
                "${Prefix}Current fight ${ChatColor.RED}timed out${ChatColor.GRAY}. Eliminating both, ${
                    currentlyFighting.joinToString(
                        " ${ChatColor.GRAY}and "
                    ) { "${ChatColor.WHITE}${it.name}" }
                }${ChatColor.GRAY}."
            )
            currentlyFighting.forEach { fighting ->
                fighting.bukkitPlayer?.inventory?.clear()
                fighting.bukkitPlayer?.gameMode = GameMode.SPECTATOR
                fighting.bukkitPlayer?.teleport(GameManager.world.spawnLocation.clone().add(0, 10, 0))
            }
        }
        timer.set(0)
        currentlyFighting.clear()
    }

    fun register() {
        world.difficulty = Difficulty.NORMAL
        task = task(true, 20, 20) {
            if (GameManager.phase == EndPhase) {
                it.cancel()
                task = null
                return@task
            }
            if (!isOpen && queuedPlayers.size == 1) {
                val revived = queuedPlayers.single()
                revived.makeGameReady()
                revived.bukkitPlayer?.inventory?.apply {
                    addItem(ItemStack(Material.STONE_SWORD))
                    for (i in 0..35) {
                        addItem(ItemStack(Material.MUSHROOM_SOUP))
                    }
                }
                broadcast("${Prefix}${ChatColor.WHITE}${revived.name} ${ChatColor.GRAY}was revived.")
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

                if (timer.get() == MAX_FIGHT_LENGTH) {
                    endFight(null)
                } else {
                    timer.incrementAndGet()
                }
            }
        }

        listen<PlayerDeathEvent> {
            if (it.entity.world != Bukkit.getWorld("arena")) return@listen
            if (it.entity.hgPlayer !in currentlyFighting) return@listen
            it.deathMessage = null
            endFight(it.entity)
        }

        listen<BlockBreakEvent> {
            if (it.block.world != Bukkit.getWorld("arena")) return@listen
            it.isCancelled = true
        }

        listen<BlockPlaceEvent> {
            if (it.block.world != Bukkit.getWorld("arena")) return@listen
            it.isCancelled = true
        }

        listen<EntityDamageByEntityEvent> {
            if (it.entity.world != Bukkit.getWorld("arena")) return@listen
            val entity = it.entity
            val damager = it.damager
            if (entity !is Player || damager !is Player || timer.get() <= 0) {
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
                player.teleport(GameManager.world.spawnLocation)
                player.gameMode = GameMode.SPECTATOR
            }

            if (player.hgPlayer in currentlyFighting) {
                player.hgPlayer.status = PlayerStatus.ELIMINATED
                queuedPlayers.remove(player.hgPlayer)
                player.teleport(GameManager.world.spawnLocation)
                player.gameMode = GameMode.SPECTATOR
                endFight(player)
            }
        }

        listen<EntitySpawnEvent> {
            if (it.entity !is LivingEntity) return@listen
            if (it.entity.world == world) {
                it.isCancelled = true
            }
        }

        listen<FoodLevelChangeEvent> {
            if (it.entity !is LivingEntity) return@listen
            if (it.entity.world == world) {
                it.isCancelled = true
            }
        }
    }
}
