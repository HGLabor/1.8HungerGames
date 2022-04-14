    package de.hglabor.plugins.hungergames.player

import de.hglabor.plugins.hungergames.event.KitDisableEvent
import de.hglabor.plugins.hungergames.event.KitEnableEvent
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
    var isKitEnabled = true

    fun login() {
        OfflineTimer.stopTimer(this)
        setScoreboard()
    }

    private fun setScoreboard(forceReset: Boolean = false) {
        val player = bukkitPlayer ?: return
        if (board != null && !forceReset) {
            board!!.setScoreboard(player)
            return
        }

        board = player.setScoreboard {
            title = "${ChatColor.AQUA}${ChatColor.BOLD}HG${ChatColor.WHITE}${ChatColor.BOLD}Labor.de"
            period = 4
            content {
                +" "
                +{ "${ChatColor.GREEN}${ChatColor.BOLD}Players: ${ChatColor.WHITE}${PlayerList.getShownPlayerCount()}" }
                +{ "${ChatColor.AQUA}${ChatColor.BOLD}Kit: ${ChatColor.WHITE}${kit.properties.kitname}" }
                +{ "${ChatColor.RED}${ChatColor.BOLD}Kills: ${ChatColor.WHITE}${kills.get()}" }
                +{ "${ChatColor.YELLOW}${ChatColor.BOLD}${GameManager.phase.timeName}: ${ChatColor.WHITE}${GameManager.phase.getTimeString()}" }
                +" "
            }
        }
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
            teleport(GameManager.world.spawnLocation)
        }
    }

    fun enableKit() {
        isKitEnabled = true
        Bukkit.getPluginManager().callEvent(KitEnableEvent(bukkitPlayer ?: return, kit))
    }

    fun disableKit() {
        isKitEnabled = false
        Bukkit.getPluginManager().callEvent(KitDisableEvent(bukkitPlayer ?: return, kit))
    }
}

val Player.hgPlayer get() = PlayerList.getPlayer(this)
/*
val Player.staffPlayer
    get() = hgPlayer as? StaffPlayer*/
