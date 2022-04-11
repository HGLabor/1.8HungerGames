package de.hglabor.plugins.hungergames.game.phase.phases

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.game.phase.IngamePhase
import de.hglabor.plugins.hungergames.player.HGPlayer
import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.hungergames.utils.TimeConverter
import de.hglabor.plugins.kitapi.player.PlayerKits
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.extensions.broadcast
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent

object InvincibilityPhase: IngamePhase(120, PvPPhase) {
    override val timeName = "Schutz"
    override fun getTimeString() = TimeConverter.stringify(remainingTime.toInt())

    override fun onStart() {
        PlayerList.allPlayers.forEach(HGPlayer::makeGameReady)
        PlayerKits.register()
    }

    override fun tick(tickCount: Int) {
        when (remainingTime.toInt()) {
            60, 30, 20, 10, 3, 2, 1 -> broadcast("${Prefix}Die Schutzzeit  ${KColors.WHITE}${getTimeString()}${ChatColor.GRAY}.")
        }
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        event.isCancelled = true
    }
}