package de.hglabor.plugins.hungergames.game.mechanics

import de.hglabor.plugins.hungergames.utils.ChanceUtils
import net.axay.kspigot.event.listen
import org.bukkit.event.player.PlayerItemDamageEvent

object MoreDurability {
    fun register() {
        listen<PlayerItemDamageEvent> {
            if (ChanceUtils.roll(35)) {
                it.isCancelled = true
            }
        }
    }
}