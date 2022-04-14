package de.hglabor.plugins.hungergames.game.agnikai

import de.hglabor.plugins.hungergames.HungerGames
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.player.HGPlayer
import de.hglabor.plugins.hungergames.player.PlayerStatus
import de.hglabor.plugins.hungergames.player.hgPlayer
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.extensions.bukkit.feedSaturate
import net.axay.kspigot.extensions.bukkit.heal
import net.axay.kspigot.extensions.onlinePlayers
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack


object Agnikai {
    private val plugin: HungerGames? = null
    val queuedPlayers = buildList<Player>{}.toMutableList()

    fun queuePlayer(player: Player) {
        player.spigot().respawn()
        player.teleport(Location(Bukkit.getWorld("arena"), 0.0, 0.0, 0.0))
    }

    fun register() {
        listen<PlayerDeathEvent> {
            if(!queuedPlayers.contains(it.entity.killer) || it.entity.killer !is Player) return@listen
            queuedPlayers.remove(it.entity.killer)
            it.entity.killer.hgPlayer.makeGameReady()
            if(queuedPlayers.size/2 == 0) queuedPlayers.forEach{it.sendMessage("Waiting for player")}
            while(queuedPlayers.size <= 2) {
                val players = onlinePlayers.sortedBy { (it.hgPlayer.status == PlayerStatus.GULAG)}.toMutableList()
                val randomPlayer = players.random()
                queuedPlayers.add(randomPlayer)
                randomPlayer.teleport(Location(Bukkit.getWorld("arena"), 0.0, 5.0, 0.0))
                broadcast("${queuedPlayers[0]} vs ${queuedPlayers[1]}")
            }
        }
        listen<EntityDamageByEntityEvent> {
            if(it.damager !is Player) return@listen
            if(it.entity.world != Bukkit.getWorld("arena")) return@listen
            it.isCancelled = true
        }
    }

}
