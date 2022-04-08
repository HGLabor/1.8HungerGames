package de.hglabor.plugins.hungergames.event

import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PlayerKilledEntityEvent(val killer: Player, val dead: LivingEntity) : Event() {
    val deathLocation: Location = dead.location

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        val handlerList = HandlerList()
    }
}