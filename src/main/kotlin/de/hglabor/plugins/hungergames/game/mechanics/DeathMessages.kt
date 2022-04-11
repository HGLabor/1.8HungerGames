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
            announce(event.entity.killer.hgPlayer, hgPlayer)
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
        val deadText = "${ChatColor.RED}${dead.name} ${ChatColor.GRAY}(${ChatColor.DARK_RED}${dead.kit.properties.kitname}${ChatColor.GRAY})"
        val killerText = "${ChatColor.GREEN}${killer.name} ${ChatColor.GRAY}(${ChatColor.DARK_GREEN}${killer.kit.properties.kitname}${ChatColor.GRAY})"
        val slainText = " ${KColors.GRAY}wurde von "
        broadcast(Prefix + deadText + slainText + killerText)
    }

    private fun announce(dead: HGPlayer) {
        val deadText = "${ChatColor.RED}${dead.name}"
        broadcast(Prefix + deadText + ChatColor.GRAY + " wurde eliminiert")
    }

    private fun announce(dead: HGPlayer, deathMessage: String) {
        val deadText = "${ChatColor.RED}${dead.name}${KColors.GRAY}"
        var message = deathMessage
        if (message.contains("wurde eliminiert von")) {
            message = message.replace("wurde eliminiert von", "wurde eliminiert von${ChatColor.GREEN}")
        }
        broadcast(Prefix + message.replace(dead.name.toRegex(), deadText))
    }

    private fun announcePlayerCount() {
        broadcast("${Prefix}Es sind noch ${ChatColor.WHITE}${PlayerList.alivePlayers.size} ${ChatColor.GRAY}Laborraten dabei.")
    }
}