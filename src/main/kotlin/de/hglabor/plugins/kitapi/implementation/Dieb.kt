package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.kitapi.cooldown.MultipleUsesCooldownProperties
import de.hglabor.plugins.kitapi.cooldown.applyCooldown
import de.hglabor.plugins.kitapi.kit.Kit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class DiebProperties : MultipleUsesCooldownProperties(3, 45000)

val Dieb = Kit("Dieb", ::DiebProperties) {
    displayMaterial = Material.COAL

    clickOnEntityItem(ItemStack(Material.COAL)) {
        val rightClicked = it.rightClicked as? Player ?: return@clickOnEntityItem
        applyCooldown(it) {
        }
        rightClicked.inventory.removeItem(ItemStack(Material.MUSHROOM_SOUP))
        it.player.inventory.addItem(ItemStack(Material.MUSHROOM_SOUP))
    }
}