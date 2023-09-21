package de.hglabor.plugins.hungergames.game.mechanics

import de.hglabor.plugins.hungergames.game.mechanics.implementation.*

object MechanicsManager {
    val mechanics = listOf(
        BuildHeightLimit,
        DamageNerf,
        OreNerf,
        LapisInEnchanter,
        BlocksToInv,
        RemoveFishingRod,
        NoInvDropOnClose,
        MoreDurability,
        MushroomCowNerf,
        HungerNerf
    )
}