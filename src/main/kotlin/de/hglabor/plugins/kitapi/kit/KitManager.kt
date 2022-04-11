package de.hglabor.plugins.kitapi.kit

import de.hglabor.plugins.kitapi.implementation.*

object KitManager {
    val kits = setOf(
        Anchor,
        Automatic,
        Blink,
        //Counter,
        Digger,
        Domino,
        Magma,
        Ninja,
        None,
        //Perfekt,
        Phantom,
        Redstoner,
        Revive,
        Snail,
        Spider,
        Sponge,
        Squid,
        Stomper,
        Viking,
        ZickZack,
    ).map { it.value }
}