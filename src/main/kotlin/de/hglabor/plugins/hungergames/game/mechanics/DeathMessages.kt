package de.hglabor.plugins.hungergames.game.mechanics

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.game.agnikai.Agnikai
import de.hglabor.plugins.hungergames.player.HGPlayer
import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.hungergames.player.hgPlayer
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.runnables.taskRunLater
import org.bukkit.ChatColor
import org.bukkit.event.entity.PlayerDeathEvent

object DeathMessages {
    fun announceAgnikaiDeath(winner: HGPlayer, loser: HGPlayer) {
        broadcast("${Agnikai.Prefix}${ChatColor.GREEN}${winner.name} ${ChatColor.GRAY}won the fight against ${ChatColor.RED}${loser.name}${ChatColor.GRAY}.")
    }

    fun announce(event: PlayerDeathEvent, enteredAgnikai: Boolean) {
        event.deathMessage = null
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
        taskRunLater(2) {
            announcePlayerCount(enteredAgnikai)
        }
    }

    private fun announce(killer: HGPlayer, dead: HGPlayer) {
        val deadText = "${ChatColor.RED}${dead.name} ${ChatColor.GRAY}(${ChatColor.DARK_RED}${dead.kit.properties.kitname}${ChatColor.GRAY})"
        val killerText = "${ChatColor.GREEN}${killer.name} ${ChatColor.GRAY}(${ChatColor.DARK_GREEN}${killer.kit.properties.kitname}${ChatColor.GRAY})"
        val slainText = " ${ChatColor.GRAY}was eliminated by "
        broadcast(Prefix + deadText + slainText + killerText)
    }

    private fun announce(dead: HGPlayer) {
        val deadText = "${ChatColor.RED}${dead.name}"
        broadcast(Prefix + deadText + ChatColor.GRAY + " was eliminated")
    }

    private fun announce(dead: HGPlayer, deathMessage: String) {
        val deadText = "${ChatColor.RED}${dead.name}${ChatColor.GRAY}"
        var message = deathMessage
        if (message.contains("was slain by")) {
            message = message.replace("was slain by", "was eliminated by${ChatColor.GREEN}")
        }
        broadcast(Prefix + message.replace(dead.name.toRegex(), deadText))
    }

    private fun announcePlayerCount(enteredAgnikai: Boolean) {
        if (enteredAgnikai) {
            broadcast("${Agnikai.Prefix}They have entered the ${ChatColor.AQUA}Agnikai${ChatColor.GRAY}.")
        } else {
            broadcast("${Prefix}There are ${ChatColor.WHITE}${PlayerList.alivePlayers.size} ${ChatColor.GRAY}players left.")
        }
    }
}