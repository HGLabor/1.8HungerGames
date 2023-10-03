package de.hglabor.plugins.hungergames.game.mechanics.implementation

import net.axay.kspigot.extensions.server
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.material.MaterialData

object RecraftRecipes {
    fun register() {
        server.addRecipe(recipe(MaterialData(Material.CACTUS)))
        server.addRecipe(recipe(MaterialData(Material.INK_SACK, 3)))
    }

    fun recipe(materialData: MaterialData): ShapelessRecipe =
        ShapelessRecipe(ItemStack(Material.MUSHROOM_SOUP)).apply {
            addIngredient(Material.BOWL)
            addIngredient(materialData)
        }
}



