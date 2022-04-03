package de.hglabor.plugins.hungergames.game.mechanics

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.phase.phases.PvPPhase
import net.axay.kspigot.event.listen
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.block.BlockBreakEvent

object IronNerf {
    fun register() {
        listen<BlockBreakEvent> {
            if (it.block.type != Material.IRON_ORE) return@listen
            if (GameManager.phase != PvPPhase || (GameManager.phase == PvPPhase && GameManager.elapsedTime.get() <= 180)) {
                it.isCancelled = true
                it.player.sendMessage("${Prefix}${ChatColor.RED}You can't mine iron yet. You may start 3 minutes after the pvpphase has began.")
            }
        }
    }
}