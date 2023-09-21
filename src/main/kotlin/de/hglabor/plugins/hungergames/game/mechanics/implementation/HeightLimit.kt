package de.hglabor.plugins.hungergames.game.mechanics.implementation

import de.hglabor.plugins.hungergames.game.mechanics.Mechanic
import net.axay.kspigot.event.listen
import org.bukkit.Material
import org.bukkit.event.block.BlockPlaceEvent

val BuildHeightLimit by Mechanic("Height Limit") {
    description = "Players won't be able to place blocks above Y-Level 120"
    displayMaterial = Material.GLASS

    mechanicEvent<BlockPlaceEvent> {
        if (it.block.location.y >= 120) it.isCancelled = true
    }
}