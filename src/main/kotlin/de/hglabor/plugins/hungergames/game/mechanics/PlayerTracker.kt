package de.hglabor.plugins.hungergames.game.mechanics

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.player.hgPlayer
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.event.listen
import org.bukkit.ChatColor
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
                player.sendMessage("${Prefix}${ChatColor.RED}Kein Spieler gefunden")
                return@listen
            }
            player.sendMessage("${Prefix}Du verfolgst gerade ${ChatColor.LIGHT_PURPLE}${target.name}${ChatColor.GRAY}.")
            player.compassTarget = target.location
        }
    }

    private fun findTarget(trackingPlayer: Player): Player? =
        trackingPlayer.world.players.asSequence().filterIsInstance<Player>()
            .filter { other -> other != trackingPlayer && other.hgPlayer.isAlive }
            .map { other -> other.location.distanceSquared(trackingPlayer.location) to other }
            .sortedBy { other ->
                other.first
            }.firstOrNull() { other ->
                other.first > 30.0
            }?.second

}

