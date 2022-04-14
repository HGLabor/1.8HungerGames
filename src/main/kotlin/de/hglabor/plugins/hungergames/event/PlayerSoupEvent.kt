package de.hglabor.plugins.hungergames.event

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

class PlayerSoupEvent(player: Player) : PlayerEvent(player) {

    companion object {
        @JvmStatic
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    val overhealed: Boolean = player.health + 7 > player.maxHealth
}