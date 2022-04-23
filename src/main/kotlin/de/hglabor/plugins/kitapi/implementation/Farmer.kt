package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import org.bukkit.Material
import org.bukkit.event.block.BlockBreakEvent


class FarmerProperties : KitProperties()

val Farmer = Kit("Farmer", ::FarmerProperties) {
    displayMaterial = Material.WHEAT

    kitPlayerEvent<BlockBreakEvent>({ it.player }) { it, _ ->
        for (i in it.block.drops.toList()) {
            it.block.drops.add(i)
        }
    }
}

