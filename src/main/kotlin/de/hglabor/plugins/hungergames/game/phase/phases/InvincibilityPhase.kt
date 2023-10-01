package de.hglabor.plugins.hungergames.game.phase.phases

import de.hglabor.plugins.hungergames.game.mechanics.MechanicsManager
import de.hglabor.plugins.hungergames.game.phase.IngamePhase
import de.hglabor.plugins.hungergames.player.HGPlayer
import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.hungergames.utils.TimeConverter
import de.hglabor.plugins.kitapi.player.PlayerKits
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent

object InvincibilityPhase: IngamePhase(120, PvPPhase) {
    override val timeName = "Grace"
    override fun getTimeString() = TimeConverter.stringify(remainingTime.toInt())

    override fun onStart() {
        PlayerList.allPlayers.forEach(HGPlayer::makeGameReady)
        PlayerKits.register()
        MechanicsManager.mechanics.filter { it.internal.isEnabled }.onEach { it.onGameStart() }
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        if (event.entity !is Player) return
        event.isCancelled = true
    }
}