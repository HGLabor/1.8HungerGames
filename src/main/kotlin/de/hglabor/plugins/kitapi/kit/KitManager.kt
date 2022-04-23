package de.hglabor.plugins.kitapi.kit

import de.hglabor.plugins.kitapi.implementation.*

object KitManager {
    val kits = setOf(
        Anchor,
        Automatic,
        Blink,
        //Claw,
        //Counter,
        Dieb,
        Digger,
        Domino,
        Endermage,
        Farmer,
        Gladiator,
        Känguru,
        Lumberjack,
        Magma,
        Nightshade,
        Ninja,
        None,
        Perfect,
        Phantom,
        Reaper,
        Redstoner,
        //Relaxo,
        Revive,
        Shabby,
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
