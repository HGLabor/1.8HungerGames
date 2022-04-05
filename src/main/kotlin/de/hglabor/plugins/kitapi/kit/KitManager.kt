package de.hglabor.plugins.kitapi.kit

import de.hglabor.plugins.kitapi.implementation.*

object KitManager {
    val kits = setOf(
        Anchor,
        Automatic,
        Blink,
        Counter,
        Digger,
        Magma,
        Ninja,
        None,
        Phantom,
        Redstoner,
        Stomper,
        ZickZack,
    ).map { it.value }
}