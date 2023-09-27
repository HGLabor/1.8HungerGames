package de.hglabor.plugins.hungergames.game.mechanics.implementation

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.SecondaryColor
import de.hglabor.plugins.hungergames.player.hgPlayer
import net.axay.kspigot.event.listen
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent

object PlayerTracker {
    fun register() {
        listen<PlayerInteractEvent> {
            val player = it.player
            if (player.itemInHand.type != Material.COMPASS) return@listen
            val target = findTarget(player)

            if (target == null) {
                player.sendMessage("${Prefix}${ChatColor.RED}No target found.")
                return@listen
            }
            player.sendMessage("${Prefix}You compass is pointing at ${SecondaryColor}${target.name}${ChatColor.GRAY}.")
            player.compassTarget = target.location
        }
    }

    private fun findTarget(trackingPlayer: Player): Player? {
        val trackingLocation = trackingPlayer.location.clone().setHeight(0.0)
        return trackingPlayer.world.players.asSequence().filterIsInstance<Player>()
            .filter { other -> other != trackingPlayer && other.hgPlayer.isAlive }
            .map { other ->
                other.location.setHeight(0.0).distanceSquared(trackingLocation) to other
            }
            .sortedBy { other ->
                other.first
            }.firstOrNull() { other ->
                other.first > 30.0
            }?.second
    }

    private fun Location.setHeight(y: Double): Location {
        this.y = y
        return this
    }
}

