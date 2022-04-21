package de.hglabor.plugins.hungergames.game.mechanics

import de.hglabor.plugins.hungergames.utils.ChanceUtils
import net.axay.kspigot.event.listen
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerItemDamageEvent

object HungerNerf {
    fun register() {
        listen<FoodLevelChangeEvent> {
            if (ChanceUtils.roll(40)) {
                it.isCancelled = true
            }
        }
    }
}