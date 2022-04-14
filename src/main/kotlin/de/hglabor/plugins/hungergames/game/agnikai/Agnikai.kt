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
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack


object Agnikai {
    private val plugin: HungerGames? = null
    val queuedPlayers = buildList<Player>{}.toMutableList()

    fun queuePlayer(player: Player) {
        queuedPlayers.add(player)
        player.spigot().respawn()
        player.teleport(Location(Bukkit.getWorld("arena"), 0.0, 0.0, 0.0))
    }

    fun register() {
        listen<PlayerDeathEvent> {
            if(!queuedPlayers.contains(it.entity.killer) || it.entity.killer !is Player || it.entity !is Player) return@listen
            queuedPlayers.remove(it.entity.killer)
            it.entity.killer.hgPlayer.makeGameReady()
        }
    }

}
