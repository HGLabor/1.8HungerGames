package de.hglabor.plugins.hungergames.player

import de.hglabor.plugins.hungergames.scoreboard.Board
import org.bukkit.Bukkit
import org.bukkit.entity.Player
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
}

val Player.hgPlayer get() = PlayerList.getPlayer(this)
/*
val Player.staffPlayer
    get() = hgPlayer as? StaffPlayer*/
