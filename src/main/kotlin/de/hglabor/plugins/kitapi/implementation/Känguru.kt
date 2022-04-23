package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.SecondaryColor
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.phase.phases.LobbyPhase
import de.hglabor.plugins.kitapi.cooldown.CooldownProperties
import de.hglabor.plugins.kitapi.cooldown.applyCooldown
import de.hglabor.plugins.kitapi.kit.ClickOnEntityKitItem
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import net.axay.kspigot.extensions.events.isRightClick
import net.axay.kspigot.extensions.geometry.times
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class KänguruProperties : KitProperties() {
    val Vector = 3
}

val Känguru = Kit("Kangaroo", ::KänguruProperties) {
    displayMaterial = Material.FIREWORK

    clickableItem(ItemStack(Material.FIREWORK)) {
        if (!it.action.isRightClick) return@clickableItem
        val player = it.player
        val velocity = player.velocity
        if (!player.isOnGround) return@clickableItem

        if (player.isSneaking) {
velocity.x *= 5
            velocity.y *= 2
        }
        else {
            velocity.y *= 5
        }
    }
}