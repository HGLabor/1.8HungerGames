package de.hglabor.plugins.hungergames.game.phase.phases

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.mechanics.KitSelector
import de.hglabor.plugins.hungergames.game.phase.GamePhase
import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.hungergames.utils.TimeConverter
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.extensions.broadcast
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object LobbyPhase : GamePhase(240, InvincibilityPhase) {
    override val timeName = "Starting${ChatColor.DARK_GRAY}"
    override fun getTimeString() = TimeConverter.stringify(remainingTime.toInt())

    override fun incrementElapsedTime() {
        if (PlayerList.allPlayers.size >= 2) GameManager.elapsedTime.getAndIncrement()
    }

    override fun tick(tickCount: Int) {
        when (remainingTime.toInt()) {
            60, 30, 20, 10, 3, 2, 1 -> broadcast("${Prefix}The tournament starts in ${KColors.WHITE}${getTimeString()}${ChatColor.GRAY}.")
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        PlayerList.getPlayer(event.player)
        event.player.apply {
            teleport(GameManager.world.spawnLocation)
            inventory.clear()
            inventory.addItem(KitSelector.kitSelectorItem)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        PlayerList.remove(event.player.uniqueId)
    }

    @EventHandler
    fun onFoodLevelChange(event: FoodLevelChangeEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onDropItem(event: PlayerDropItemEvent) {
        event.isCancelled = true
    }
}