/*
package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.kitapi.cooldown.CooldownProperties
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import org.bukkit.Material
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class EndermageProperties : CooldownProperties() {
    val radius by double(3.0)
}

val Endermage = Kit("Endermage", ::EndermageProperties) {
    displayMaterial = Material.ENDER_PORTAL_FRAME

    kitPlayerEvent<PlayerInteractEvent>({ it.player }) { it, player ->
        if (it.action != Action.RIGHT_CLICK_BLOCK) return@kitPlayerEvent
        // TODO add logic
    }
}
*/
