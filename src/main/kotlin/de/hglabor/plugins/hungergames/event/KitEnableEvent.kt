package de.hglabor.plugins.hungergames.event

import de.hglabor.plugins.kitapi.kit.Kit
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

class KitEnableEvent(player: Player, val kit: Kit<*>) : PlayerEvent(player) {

    private val handlers = HandlerList()

    override fun getHandlers(): HandlerList {
        return handlers
    }

}