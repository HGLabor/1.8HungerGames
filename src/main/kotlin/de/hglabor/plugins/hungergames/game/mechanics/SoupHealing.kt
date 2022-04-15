package de.hglabor.plugins.hungergames.game.mechanics

import de.hglabor.plugins.hungergames.event.PlayerSoupEvent
import net.axay.kspigot.event.listen
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.math.min

object SoupHealing {
    fun register() {
        listen<PlayerInteractEvent> {
            it.player.apply {
                if (it.action == Action.LEFT_CLICK_AIR) return@listen
                if (itemInHand.type != Material.MUSHROOM_SOUP) return@listen
                if (health >= maxHealth - 0.4 && foodLevel == 20) return@listen
                Bukkit.getPluginManager().callEvent(PlayerSoupEvent(this, itemInHand))
                health = min(maxHealth, health + 7)
                foodLevel = min(30, foodLevel + 6)
                itemInHand.type = Material.BOWL
            }
        }
    }
}