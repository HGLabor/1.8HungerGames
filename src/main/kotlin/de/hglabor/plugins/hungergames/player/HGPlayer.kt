package de.hglabor.plugins.hungergames.player

import de.hglabor.plugins.hungergames.event.KitDisableEvent
import de.hglabor.plugins.hungergames.event.KitEnableEvent
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.mechanics.implementation.KitSelector
import de.hglabor.plugins.hungergames.game.mechanics.implementation.OfflineTimer
import de.hglabor.plugins.hungergames.game.mechanics.implementation.arena.Arena
import de.hglabor.plugins.hungergames.game.phase.phases.InvincibilityPhase
import de.hglabor.plugins.hungergames.scoreboard.Board
import de.hglabor.plugins.hungergames.scoreboard.setScoreboard
import de.hglabor.plugins.kitapi.implementation.None
import de.hglabor.plugins.kitapi.kit.Kit
import net.axay.kspigot.extensions.bukkit.feedSaturate
import net.axay.kspigot.extensions.bukkit.heal
import net.axay.kspigot.extensions.geometry.add
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
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

    var kills: AtomicInteger = AtomicInteger(0)
    var combatTimer: AtomicInteger = AtomicInteger(0)
    val isInCombat: Boolean
        get() = combatTimer.get() > 0 && isAlive
    var board: Board? = null
    var kit: Kit<*> = None.value
    var changedKitBefore: Boolean = false
    var isKitEnabled = true
    var isKitByRogueDisabled: Boolean = false
    var wasInArena: Boolean = false
    val roguePrefix: String
        get() = if(isKitByRogueDisabled) "${ChatColor.STRIKETHROUGH}" else ""

    fun login() {
        OfflineTimer.stopTimer(this)
        setGameScoreboard()
    }

    fun setGameScoreboard(forceReset: Boolean = false) {
        val player = bukkitPlayer ?: return
        if (board != null && !forceReset) {
            board!!.setScoreboard(player)
            return
        }

        board = player.setScoreboard {
            title = "${ChatColor.AQUA}${ChatColor.BOLD}HG${ChatColor.WHITE}${ChatColor.BOLD}Labor.de"
            period = 20
            content {
                +" "
                +{ "${ChatColor.GREEN}${ChatColor.BOLD}Players:#${ChatColor.WHITE}${PlayerList.getShownPlayerCount()} ${ChatColor.GRAY}(${Arena.queuedPlayers.size + (Arena.currentMatch?.players?.size ?: 0)})" }
                +{ "${ChatColor.AQUA}${ChatColor.BOLD}Kit:#${roguePrefix}${ChatColor.WHITE}${kit.properties.kitname}" }
                +{ "${ChatColor.RED}${ChatColor.BOLD}Kills:#${ChatColor.WHITE}${kills.get()}" }
                +{ "${ChatColor.YELLOW}${ChatColor.BOLD}${GameManager.phase.timeName}:#${ChatColor.WHITE}${GameManager.phase.getTimeString()}" }
                +{ if (isInCombat) "${ChatColor.RED}${ChatColor.BOLD}IN COMBAT" else " " }
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
            maxHealth = 20.0
            feedSaturate()
            heal()
            if (kit == None.value && GameManager.phase == InvincibilityPhase) {
                inventory.addItem(KitSelector.kitSelectorItem)
            } else {
                kit.internal.givePlayer(this)
            }
            hgPlayer.combatTimer.set(0)
            teleport(getSpawnLocation().add(0, 3 ,0))
        }
    }

    private fun getSpawnLocation(): Location {
        val spawnLoc = GameManager.world.spawnLocation
        val newLoc = spawnLoc.clone().add((-25..25).random(), 0, (-25..25).random())
        val highestBlock = newLoc.world.getHighestBlockAt(newLoc)
        return if (highestBlock.y > 85 || (!highestBlock.type.isSolid && !highestBlock.getRelative(BlockFace.DOWN).type.isSolid)) getSpawnLocation()
        else highestBlock.location
    }

    fun enableKit() {
        isKitEnabled = true
        isKitByRogueDisabled = false
        Bukkit.getPluginManager().callEvent(KitEnableEvent(bukkitPlayer ?: return, kit))
    }

    fun disableKit(isByRogue: Boolean = false) {
        isKitEnabled = false
        isKitByRogueDisabled = isByRogue
        Bukkit.getPluginManager().callEvent(KitDisableEvent(bukkitPlayer ?: return, kit))
    }
}

val Player.hgPlayer get() = PlayerList.getPlayer(this)
/*
val Player.staffPlayer
    get() = hgPlayer as? StaffPlayer*/
