package de.hglabor.plugins.hungergames.player

import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.scoreboard.Board
import de.hglabor.plugins.hungergames.scoreboard.setScoreboard
import net.axay.kspigot.extensions.bukkit.feedSaturate
import net.axay.kspigot.extensions.bukkit.heal
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

open class HGPlayer(val uuid: UUID, val name: String) {
    val bukkitPlayer: Player?
        get() = Bukkit.getPlayer(uuid)

    val isAlive: Boolean
        get() = status == PlayerStatus.INGAME || status == PlayerStatus.OFFLINE
    var status: PlayerStatus = PlayerStatus.LOBBY

    //TODO var combatLogMob: UUID? = null
    var offlineTime: Int = 120
    //var hasBeenRevived: Boolean = false

    var kills = 0
    var isInCombat = false

    var board: Board? = null

    fun login() {
        val player = bukkitPlayer ?: return
        if (board != null) {
            board!!.setScoreboard(player)
            return
        }

        fun kills(): Int = kills

        board = player.setScoreboard {
            title = "${ChatColor.DARK_PURPLE}HGLabor"
            period = 20
            content {
                +{ GameManager.phase.getTimeString() }
                +{ "Kills: ${kills()}" }
                +{ "Spieler: ${PlayerList.getShownPlayerCount()}" }
                +{ "Kit: Gar keins" }
            }
        }
    }

    fun makeGameReady() {
        status = PlayerStatus.INGAME
        bukkitPlayer?.apply {
            inventory.clear()
            inventory.addItem(ItemStack(Material.COMPASS))
            gameMode = GameMode.SURVIVAL
            feedSaturate()
            heal()
        }
    }
}

val Player.hgPlayer get() = PlayerList.getPlayer(this)
/*
val Player.staffPlayer
    get() = hgPlayer as? StaffPlayer*/
