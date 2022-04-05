package de.hglabor.plugins.kitapi.kit

import de.hglabor.plugins.kitapi.implementation.*

object KitManager {
    val kits = setOf(
        Anchor,
        Automatic,
        Blink,
        Counter,
        Magma,
        Ninja,
        None,
        Phantom,
        Redstoner,
        Stomper,
        ZickZack,
        Fireman
    ).map { it.value }
}