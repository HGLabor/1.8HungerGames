package de.hglabor.plugins.hungergames.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class FeastBeginEvent: Event() {
    companion object {
        @JvmStatic
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }
}