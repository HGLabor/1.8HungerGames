package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.kitapi.cooldown.CooldownProperties
import de.hglabor.plugins.kitapi.cooldown.applyCooldown
import de.hglabor.plugins.kitapi.kit.Kit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.axay.kspigot.extensions.events.isRightClick
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class RogueProperties : CooldownProperties(16000) {
    val radius by double(25.0)
    val duration by long(10000)
}

val Rogue = Kit("Rogue", ::RogueProperties) {
    val scope = CoroutineScope(Dispatchers.IO)

    displayMaterial = Material.STICK

    clickableItem(ItemStack(Material.STICK)) {
        if (!it.action.isRightClick) return@clickableItem
        val player = it.player
        val radius = kit.properties.radius
        val radiusSquared: Double = radius * radius

        val entities: List<Entity> = player.getNearbyEntities(radius, radius, radius) // All entities withing a box

        applyCooldown(it) {
            for (entity in entities) {
                if (entity.location.distanceSquared(player.location) > radiusSquared) continue  // All entities within a sphere

                if (entity is Player) {
                    val hgPlayer = entity.hgPlayer
                    if (!hgPlayer.isAlive) continue

                    scope.launch {
                        hgPlayer.disableKit()
                        hgPlayer.bukkitPlayer?.sendMessage("${Prefix}Your kit has been ${ChatColor.RED}disabled${ChatColor.GRAY}.")

                        delay(kit.properties.duration)

                        hgPlayer.enableKit()
                        hgPlayer.bukkitPlayer?.sendMessage("${Prefix}Your kit has been ${ChatColor.GREEN}enabled${ChatColor.GRAY}.")
                    }
                }
            }
        }
    }
}