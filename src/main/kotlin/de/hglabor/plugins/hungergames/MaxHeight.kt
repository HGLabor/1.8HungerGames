package de.hglabor.plugins.hungergames

import jdk.jfr.Event
import org.bukkit.event.EventHandler

object MaxHeight {
    @EventHandler
    fun placeLimit(event: PlayerBlockPlaceEvent) {
        if (!Event.getPlayer().getLocation().getY() >= 120) return
        event.setCancelled(true)
    }
}