package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.kitapi.cooldown.MultipleUsesCooldownProperties
import de.hglabor.plugins.kitapi.cooldown.applyCooldown
import de.hglabor.plugins.kitapi.kit.Kit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class BlinkProperties : MultipleUsesCooldownProperties(5, 16000) {
    val distance by double(4.0)
}

val Blink = Kit("Blink", ::BlinkProperties) {
    displayMaterial = Material.NETHER_STAR

    clickableItem(ItemStack(Material.NETHER_STAR)) {
        applyCooldown(it) {
            val player = it.player
            player.teleport(player.location.clone().add(0.0, kit.properties.distance, 0.0))
            player.sendMessage("Blink! Uses: ${kit.properties.getUses(player)}")
        }
    }
}
