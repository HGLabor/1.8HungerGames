package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.player.HGPlayer
import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.kitapi.cooldown.*
import de.hglabor.plugins.kitapi.kit.Kit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.axay.kspigot.extensions.events.isRightClick
import org.bukkit.*
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class RogueProperties : CooldownProperties(16000) {
    val distance by double(4.0)
}

val Rogue = Kit("Rogue", ::RogueProperties) {
    val radius = 25.0
    val duration = 1000L * 10
    val scope = CoroutineScope(Dispatchers.IO)

    displayMaterial = Material.STICK

    clickableItem(ItemStack(Material.STICK)) {
        if (!it.action.isRightClick) return@clickableItem
        val playersInRadius = mutableListOf<HGPlayer>()
        val player = it.player

        val radiusSquared: Double = radius * radius

        val entities: List<Entity> = player.getNearbyEntities(radius, radius, radius) // All entities withing a box

        for (entity in entities) {
            if (entity.location.distanceSquared(player.location) > radiusSquared) continue  // All entities within a sphere

            if (entity is Player) {
                val hgPlayer = PlayerList.getPlayer(entity.uniqueId)

                // hoffe das hier geht
                scope.launch {
                    hgPlayer?.disableKit()

                    delay(duration)

                    hgPlayer?.enableKit()
                }.invokeOnCompletion {
                    applyCooldown(player) {  }
                }
            }
        }
    }
}