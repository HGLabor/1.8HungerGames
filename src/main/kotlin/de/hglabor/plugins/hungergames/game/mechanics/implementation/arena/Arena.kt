package de.hglabor.plugins.hungergames.game.mechanics.implementation.arena

import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.mechanics.implementation.DeathMessages
import de.hglabor.plugins.hungergames.game.phase.phases.EndPhase
import de.hglabor.plugins.hungergames.player.HGPlayer
import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.hungergames.player.PlayerStatus
import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.hungergames.scoreboard.setScoreboard
import de.hglabor.plugins.hungergames.utils.TimeConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.extensions.bukkit.give
import net.axay.kspigot.extensions.bukkit.isFeetInWater
import net.axay.kspigot.extensions.bukkit.title
import net.axay.kspigot.extensions.geometry.add
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.sync
import net.axay.kspigot.runnables.task
import org.bukkit.*
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import java.util.concurrent.atomic.AtomicInteger

object Arena {
    val Prefix = " ${ChatColor.DARK_GRAY}| ${ChatColor.RED}Arena ${ChatColor.DARK_GRAY}» ${ChatColor.GRAY}"
    var isOpen = true
    val queuedPlayers = mutableListOf<HGPlayer>()
    var currentMatch: ArenaMatch? = null

    fun queuePlayer(player: Player) {
        player.spigot().respawn()
        player.teleport(ArenaWorld.queueLocation)
        player.hgPlayer.status = PlayerStatus.GULAG
        queuedPlayers += player.hgPlayer
        player.hgPlayer.wasInArena = true
        player.inventory.clear()

        player.setScoreboard {
            title = "${ChatColor.AQUA}${ChatColor.BOLD}HG${ChatColor.WHITE}${ChatColor.BOLD}Labor.de"
            period = 20
            content {
                fun fightDuration(): String {
                    if (currentMatch == null) return " "
                    val timer = currentMatch?.timer?.get() ?: return " "
                    if (timer >= 0) return TimeConverter.stringify(ArenaMatch.MAX_DURATION - timer)
                    return TimeConverter.stringify(ArenaMatch.MAX_DURATION)
                }
                +" "
                +{ "${ChatColor.GREEN}${ChatColor.BOLD}Players:#${ChatColor.WHITE}${PlayerList.getShownPlayerCount()}" }
                +{ "${ChatColor.YELLOW}${ChatColor.BOLD}${GameManager.phase.timeName}:#${ChatColor.WHITE}${GameManager.phase.getTimeString()}" }
                +" "
                +{ "${ChatColor.AQUA}${ChatColor.BOLD}Waiting:#${ChatColor.WHITE}${queuedPlayers.size}" }
                +{ "${ChatColor.RED}${ChatColor.BOLD}Fighting:#${ChatColor.WHITE}${fightDuration()}" }
                +{ "  ${ChatColor.GRAY}-#${(currentMatch?.players?.firstOrNull()?.name ?: "None").take(15)}" }
                +{ "  ${ChatColor.GRAY}-#${(currentMatch?.players?.lastOrNull()?.name ?: "None").take(15)}" }
                +" "
            }
        }
    }

    fun startNewMatch() {
        if (currentMatch?.isEnded == false) return
        if (queuedPlayers.size >= 2) {
            val players = queuedPlayers.take(2).toTypedArray()
            queuedPlayers.removeAll(players)
            currentMatch = ArenaMatch(*players).also {
                it.start()
            }
        }
    }

    fun register() {

    }
}
