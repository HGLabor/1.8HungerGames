package de.hglabor.plugins.hungergames.game.mechanics

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.player.HGPlayer
import de.hglabor.plugins.hungergames.player.PlayerStatus
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import org.bukkit.ChatColor
import java.util.*

object OfflineTimer {
    private val offlinePlayers: MutableMap<UUID, KSpigotRunnable?>

    init {
        offlinePlayers = HashMap()
    }

    fun putAndStartTimer(hgPlayer: HGPlayer) {
        if (hgPlayer.status != PlayerStatus.INGAME) return
        offlinePlayers[hgPlayer.uuid] = task(true, 0, 20) {
            if (hgPlayer.status == PlayerStatus.ELIMINATED) {
                eliminate(hgPlayer)
                it.cancel()
            }
            if (hgPlayer.offlineTime.getAndDecrement() == 0) {
                eliminate(hgPlayer)
                it.cancel()
            }
        }
    }

    private fun eliminate(hgPlayer: HGPlayer) {
        broadcast("${Prefix}${ChatColor.LIGHT_PURPLE}${hgPlayer.name} ${ChatColor.GRAY}has been offline for too long.")
        hgPlayer.status = PlayerStatus.ELIMINATED
        offlinePlayers[hgPlayer.uuid]?.cancel()
        offlinePlayers.remove(hgPlayer.uuid)
    }

    fun stopTimer(hgPlayer: HGPlayer) {
        if (hgPlayer.status == PlayerStatus.OFFLINE) {
            offlinePlayers[hgPlayer.uuid]?.cancel()
            offlinePlayers.remove(hgPlayer.uuid)
            hgPlayer.status = PlayerStatus.INGAME
        }
    }
}