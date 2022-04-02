package de.hglabor.plugins.hungergames.player

import org.bukkit.entity.Player
import java.util.*

object PlayerList {
    private val players: MutableMap<UUID, HGPlayer> = mutableMapOf()
    val allPlayers get() = ArrayList(players.values)
    val alivePlayers: MutableList<HGPlayer> get() = players.values.filter { it.isAlive }.toMutableList()
    //val staffPlayers: MutableList<StaffPlayer> get() = allPlayers.filterIsInstance<StaffPlayer>().filter { it.isStaffMode }.toMutableList()
    val lobbyPlayers: MutableList<HGPlayer> get() = allPlayers.filter { it.status == PlayerStatus.LOBBY }.toMutableList()
    val spectatingPlayers: MutableList<HGPlayer> get() = allPlayers.filter { it.status == PlayerStatus.SPECTATOR }.toMutableList()

    fun getPlayer(player: Player) =
        players.computeIfAbsent(player.uniqueId) { uuid: UUID ->
            /*if (player.hasPermission("hglabor.staff") StaffPlayer(uuid, player.name)
            else HGPlayer(uuid, player.name)*/
            HGPlayer(uuid, player.name)
        }

    fun getPlayer(uuid: UUID): HGPlayer? = players[uuid]

    //fun getStaffPlayer(player: Player): StaffPlayer? = getPlayer(player) as? StaffPlayer

    fun remove(uuid: UUID?) {
        players.remove(uuid)
    }

    fun remove(player: HGPlayer) {
        remove(player.uuid)
    }
}