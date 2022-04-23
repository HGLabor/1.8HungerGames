package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.kitapi.cooldown.CooldownProperties
import de.hglabor.plugins.kitapi.cooldown.applyCooldown
import de.hglabor.plugins.kitapi.kit.Kit
import org.bukkit.Material
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*


class ClawProperties : CooldownProperties(16000)

val Claw = Kit("Claw", ::ClawProperties) {
    displayMaterial = Material.SHEARS

    clickOnEntityItem(ItemStack(Material.ARROW)) {
        val rightClicked = it.rightClicked as? Player ?: return@clickOnEntityItem
        applyCooldown(it) {
            val arrow = Material.ARROW
            val playerDirection: Vector = it.player.location.direction
            it.launchProjectile(arrow, playerDirection())
        }
    }
}

