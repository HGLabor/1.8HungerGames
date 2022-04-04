package de.hglabor.plugins.hungergames.player

import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.mechanics.OfflineTimer
import de.hglabor.plugins.hungergames.game.mechanics.recraft.Recraft
import de.hglabor.plugins.hungergames.scoreboard.Board
import de.hglabor.plugins.hungergames.scoreboard.setScoreboard
import de.hglabor.plugins.kitapi.implementation.None
import de.hglabor.plugins.kitapi.kit.Kit
import net.axay.kspigot.extensions.bukkit.feedSaturate
import net.axay.kspigot.extensions.bukkit.heal
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

open class HGPlayer(val uuid: UUID, val name: String) {
    val bukkitPlayer: Player?
        get() = Bukkit.getPlayer(uuid)
    val isAlive: Boolean
        get() = status == PlayerStatus.INGAME || status == PlayerStatus.OFFLINE
    var status: PlayerStatus = PlayerStatus.LOBBY

    //TODO var combatLogMob: UUID? = null
    var offlineTime: AtomicInteger = AtomicInteger(120)

    //var hasBeenRevived: Boolean = false
    var kills: AtomicInteger = AtomicInteger(0)
    var isInCombat = false
    val recraft = Recraft()
    var board: Board? = null
    var kit: Kit<*> = None.value

    fun login() {
        val player = bukkitPlayer ?: return
        if (board != null) {
            board!!.setScoreboard(player)
            return
        }

        board = player.setScoreboard {
            title = "${ChatColor.DARK_PURPLE}${ChatColor.BOLD}HGLabor"
            period = 20
            val p = "${ChatColor.LIGHT_PURPLE}"
            content {
                +" "
                +{ "${p}Players${ChatColor.DARK_GRAY}: ${ChatColor.WHITE}${PlayerList.getShownPlayerCount()}" }
                +{ "${p}Kit${ChatColor.DARK_GRAY}: ${ChatColor.WHITE}${kit.properties.kitname}" }
                +{ "${p}Kills${ChatColor.DARK_GRAY}: ${ChatColor.WHITE}${kills.get()}" }
                +{ "${p}${GameManager.phase.timeName}${ChatColor.DARK_GRAY}: ${GameManager.phase.getTimeString()}" }
                +" "
            }
        }

        OfflineTimer.stopTimer(this)
    }

    fun makeGameReady() {
        status = PlayerStatus.INGAME
        bukkitPlayer?.apply {
            inventory.clear()
            inventory.addItem(ItemStack(Material.COMPASS))
            gameMode = GameMode.SURVIVAL
            closeInventory()
            feedSaturate()
            heal()
            kit.internal.givePlayer(this)
            if (!GameManager.world.spawnLocation.chunk.isLoaded)
                GameManager.world.loadChunk(GameManager.world.spawnLocation.chunk)
            val loc = GameManager.world.spawnLocation.clone().apply {
                x = 0.0
                y = 100.0
                z = 0.0
            }
            teleport(loc)
        }
    }
}

val Player.hgPlayer get() = PlayerList.getPlayer(this)
/*
val Player.staffPlayer
    get() = hgPlayer as? StaffPlayer*/
