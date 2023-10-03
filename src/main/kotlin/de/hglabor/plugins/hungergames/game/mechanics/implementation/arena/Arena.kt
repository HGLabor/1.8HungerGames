package de.hglabor.plugins.hungergames.game.mechanics.implementation.arena

import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.player.HGPlayer
import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.hungergames.player.PlayerStatus
import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.hungergames.scoreboard.setScoreboard
import de.hglabor.plugins.hungergames.utils.TimeConverter
import net.axay.kspigot.extensions.broadcast
import org.bukkit.ChatColor
import org.bukkit.entity.Player

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
                +"${ChatColor.GRAY}${ChatColor.STRIKETHROUGH}          #${ChatColor.GRAY}${ChatColor.STRIKETHROUGH}          "
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

    fun close() {
        isOpen = false
        broadcast("$Prefix${ChatColor.RED}${ChatColor.BOLD}The Arena has been closed!")
    }
}
