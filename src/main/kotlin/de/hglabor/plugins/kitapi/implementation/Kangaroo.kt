package de.hglabor.plugins.kitapi.implementation

import com.google.common.primitives.Doubles.max
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import net.axay.kspigot.extensions.events.isRightClick
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class KangarooProperties : KitProperties() {
    val sneakingMultiplier = 5 to 2
    val defaultMultiplier = 1 to 5
}

val Kangaroo = Kit("Kangaroo", ::KangarooProperties) {
    displayMaterial = Material.FIREWORK

    clickableItem(ItemStack(Material.FIREWORK)) {
        if (!it.action.isRightClick) return@clickableItem
        val player = it.player
        if (!player.isOnGround) return@clickableItem

        player.velocity.y = max(player.velocity.y, 1.0) // always needed

        if (player.isSneaking) {
            player.velocity.x = max(player.velocity.x, 1.0) // only needed for sneaking velocity

            player.velocity.x *= kit.properties.sneakingMultiplier.first
            player.velocity.y *= kit.properties.sneakingMultiplier.second
        } else {
            player.velocity.x *= kit.properties.defaultMultiplier.first
            player.velocity.y *= kit.properties.defaultMultiplier.second
        }
    }
}