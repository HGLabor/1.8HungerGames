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
        Gladiator,
        Magma,
        Ninja,
        None,
        //Perfect,
        Phantom,
        Redstoner,
        Revive,
        Snail,
        Spider,
        Sponge,
        Squid,
        Stomper,
        Ultimato,
        Viking,
        ZickZack,
    ).map { it.value }
}