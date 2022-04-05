package de.hglabor.plugins.hungergames

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.inventory.ItemStack
import org.bukkit.Material
import org.bukkit.inventory.ShapelessRecipe

class CactusSoup : JavaPlugin() {
    override fun onEnable() {
        logger.info("Enabled!")
        setUp()
    }

    override fun onDisable() {
        logger.info("Disabled")
    }

    private fun setUp() {
        val mushroom_soup = ItemStack(Material.MUSHROOM_SOUP, 1)
        val MUSHROOM_SOUP = ShapelessRecipe(mushroom_soup)
        MUSHROOM_SOUP.addIngredient(Material.BOWL)
        MUSHROOM_SOUP.addIngredient(Material.CACTUS)
        server.addRecipe(MUSHROOM_SOUP)
    }
}