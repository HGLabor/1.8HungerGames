package de.hglabor.plugins.hungergames.game.phase.phases

import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.phase.IngamePhase
import de.hglabor.plugins.hungergames.player.HGPlayer
import de.hglabor.plugins.hungergames.player.PlayerList
import net.axay.kspigot.extensions.broadcast
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent

object InvincibilityPhase: IngamePhase(120, PvPPhase) {
    override fun onStart() {
        broadcast("InvincibilityPhase")
        PlayerList.allPlayers.forEach(HGPlayer::makeGameReady)
    }

    override fun getTimeString(): String = "Invinc: ${maxDuration - GameManager.elapsedTime.get()}"

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        event.isCancelled = true
    }
}