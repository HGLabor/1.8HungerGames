package de.hglabor.plugins.hungergames.game.phase.phases

import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.mechanics.KitSelector
import de.hglabor.plugins.hungergames.game.phase.GamePhase
import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.hungergames.utils.TimeConverter
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object LobbyPhase : GamePhase(240, InvincibilityPhase) {
    override val timeName = "Starting"
    override fun getTimeString() = TimeConverter.stringify(remainingTime.toInt())

    override fun incrementElapsedTime() {
        if (PlayerList.allPlayers.size >= 2) GameManager.elapsedTime.getAndIncrement()
        else GameManager.elapsedTime.set(0)
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