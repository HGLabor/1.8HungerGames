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
        if (event.entity.killer != null) {
            announce(hgPlayer, event.entity.killer.hgPlayer)
        } else {
            if (event.deathMessage != null) {
                announce(hgPlayer, event.deathMessage)
            } else {
                announce(hgPlayer)
            }
        }
        event.deathMessage = null
        if (PlayerList.alivePlayers.size > 1)
            announcePlayerCount()
    }

    private fun announce(killer: HGPlayer, dead: HGPlayer) {
        val deadText = "${ChatColor.LIGHT_PURPLE}${dead.name}"
        val killerText = "${ChatColor.LIGHT_PURPLE}${killer.name}"
        val slainText = " ${KColors.GRAY}was eliminated by "
        broadcast(Prefix + deadText + slainText + killerText)
    }

    private fun announce(dead: HGPlayer) {
        val deadText = "${ChatColor.LIGHT_PURPLE}${dead.name}"
        broadcast(Prefix + deadText + ChatColor.GRAY + " was eliminated")
    }

    private fun announce(dead: HGPlayer, deathMessage: String) {
        val deadText = "${ChatColor.LIGHT_PURPLE}${dead.name}${KColors.GRAY}"
        var message = deathMessage
        if (message.contains("was slain by")) {
            message = message.replace("was slain by", "was eliminated by${ChatColor.LIGHT_PURPLE}")
        }
        broadcast(Prefix + message.replace(dead.name.toRegex(), deadText))
    }

    private fun announcePlayerCount() {
        broadcast("${Prefix}There are ${ChatColor.WHITE}${PlayerList.alivePlayers.size} ${ChatColor.GRAY}players left.")
    }
}