package de.hglabor.plugins.hungergames.game.mechanics

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.player.HGPlayer
import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.hungergames.player.hgPlayer
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.extensions.broadcast
import org.bukkit.ChatColor
import org.bukkit.event.entity.PlayerDeathEvent

object DeathMessages {
    fun announce(event: PlayerDeathEvent) {
        val hgPlayer = event.entity.hgPlayer
        event.deathMessage = null
        if (event.entity.killer != null) {
            announce(hgPlayer, event.entity.killer.hgPlayer)
        } else {
            if (event.deathMessage != null) {
                announce(hgPlayer, event.deathMessage)
            } else {
                announce(hgPlayer)
            }
        }
        announcePlayerCount()
    }

    private fun announce(killer: HGPlayer, dead: HGPlayer) {
        val deadText = "${ChatColor.LIGHT_PURPLE}${dead.name}"
        val killerText = "${ChatColor.LIGHT_PURPLE}${killer.name}"
        val slainText = " ${KColors.GRAY}was slain by "
        broadcast(Prefix + deadText + slainText + killerText)
        announcePlayerCount()
    }

    private fun announce(dead: HGPlayer) {
        val deadText = "${ChatColor.LIGHT_PURPLE}${dead.name}"
        broadcast(Prefix + deadText + ChatColor.GRAY + " died")
        announcePlayerCount()
    }

    private fun announce(dead: HGPlayer, deathMessage: String) {
        val deadText = "${ChatColor.LIGHT_PURPLE}${dead.name}${KColors.GRAY}"
        broadcast(Prefix + deathMessage.replace(dead.name.toRegex(), deadText))
        announcePlayerCount()
    }

    private fun announcePlayerCount() {
        broadcast("${Prefix}There are ${ChatColor.WHITE}${PlayerList.alivePlayers.size} ${ChatColor.GRAY}players left.")
    }
}