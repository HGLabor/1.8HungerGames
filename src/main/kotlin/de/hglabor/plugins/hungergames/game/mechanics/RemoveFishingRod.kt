package de.hglabor.plugins.hungergames.game.mechanics

import net.axay.kspigot.event.listen
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.inventory.CraftItemEvent

object RemoveFishingRod {
    fun register() {
        val iterator = Bukkit.getServer().recipeIterator()
        while (iterator.hasNext()) {
            val recipe = iterator.next()
            if (recipe != null && recipe.result.type == Material.FISHING_ROD) {
                iterator.remove()
            }
        }

        listen<CraftItemEvent> {
            if (it.recipe.result.type == Material.FISHING_ROD)
                it.isCancelled = true
        }

        listen<ItemSpawnEvent> {
            if (it.entity.itemStack.type == Material.FISHING_ROD)
                it.isCancelled = true
        }
    }
}