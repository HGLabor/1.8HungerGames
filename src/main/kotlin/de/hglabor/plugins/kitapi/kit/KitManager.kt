package de.hglabor.plugins.kitapi.kit

import de.hglabor.plugins.kitapi.implementation.*

object KitManager {
    val kits = setOf(
        Anchor,
        Automatic,
        Blink,
        //Counter,
        Digger,
        Magma,
        Ninja,
        None,
        Phantom,
        Redstoner,
        Revive,
        Snail,
        Spider,
        Sponge,
        Squid,
        Stomper,
        TobleroneKit,
        ZickZack,
    ).map { it.value }
}
