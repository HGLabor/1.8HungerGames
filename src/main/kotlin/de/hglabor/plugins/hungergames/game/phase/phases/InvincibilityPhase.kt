package de.hglabor.plugins.hungergames.game.phase.phases

import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.phase.IngamePhase
import de.hglabor.plugins.hungergames.player.HGPlayer
import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.hungergames.utils.TimeConverter
import de.hglabor.plugins.kitapi.player.PlayerKits
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent

object InvincibilityPhase: IngamePhase(120, PvPPhase) {
    override val timeName = "Grace${ChatColor.DARK_GRAY}"
    override fun getTimeString() = TimeConverter.stringify((maxDuration - GameManager.elapsedTime.get()).toInt())

    override fun onStart() {
        PlayerList.allPlayers.forEach(HGPlayer::makeGameReady)
        PlayerKits.register()
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        event.isCancelled = true
    }
}