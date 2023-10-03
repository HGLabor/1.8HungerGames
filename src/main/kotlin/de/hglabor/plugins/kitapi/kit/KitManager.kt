package de.hglabor.plugins.kitapi.kit

import de.hglabor.plugins.kitapi.implementation.*

object KitManager {
    val kits = setOf(
        Anchor,
        Automatic,
        Blink,
        Cannibal,
        //Counter,
        Digger,
        Domino,
        Endermage,
        Gladiator,
        Lumberjack,
        Magma,
        Nightshade,
        Ninja,
        None,
        Perfect,
        Phantom,
        Reaper,
        Redstoner,
        Revive,
        Rogue,
        Smoothyy,
        Snail,
        Spider,
        Sponge,
        Squid,
        Stomper,
        Toblerone,
        //Ultimato,
        Viking,
        ZickZack,
    ).map { it.value }
}
