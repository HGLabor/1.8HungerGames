package de.hglabor.plugins.hungergames.game.mechanics

import org.bukkit.inventory.Recipe
import org.bukkit.Bukkit
import org.bukkit.Material

object RemoveFishingRod {

    var it = Bukkit.getServer().recipeIterator()
    var recipe: Recipe? = null

    fun register() {
        recipe = it.next()
        if (recipe != null && recipe!!.result.type == Material.FISHING_ROD) {
            it.remove()

        }
    }
}