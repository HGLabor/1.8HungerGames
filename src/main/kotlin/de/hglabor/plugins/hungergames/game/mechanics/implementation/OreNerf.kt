package de.hglabor.plugins.hungergames.game.mechanics.implementation

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.mechanics.Mechanic
import de.hglabor.plugins.hungergames.game.phase.phases.PvPPhase
import net.axay.kspigot.event.listen
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.block.BlockBreakEvent

val OreNerf by Mechanic("Ore Nerf") {
    description = "Diamonds are not breakable. Iron can only be mined 3 minutes after PvPPhase has started"
    displayMaterial = Material.IRON_ORE

    mechanicEvent<BlockBreakEvent> {
        if (it.block.type == Material.DIAMOND_ORE) {
            it.isCancelled = true
            it.block.type = Material.AIR
            it.player.sendMessage("${Prefix}${ChatColor.RED}You can't mine diamonds.")
        }

        if (it.block.type == Material.IRON_ORE) {
            if (GameManager.phase == PvPPhase && GameManager.elapsedTime.get() <= 180) {
                it.isCancelled = true
                it.player.sendMessage("${Prefix}${ChatColor.RED}You can't mine iron yet. You may start 3 minutes after the PvPPhase has began.")
            }
        }
    }
}