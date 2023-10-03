package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import de.hglabor.plugins.kitapi.player.PlayerKits.hasKit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import java.lang.Double.min

class StomperProperties : KitProperties() {
    val radius by double(3.0)
}

val Stomper by Kit("Stomper", ::StomperProperties) {
    displayMaterial = Material.DIAMOND_BOOTS
    description = "${ChatColor.GRAY}Your falldamage will be reflected to nearby players"

    kitPlayerEvent<EntityDamageEvent>({ it.entity as? Player }) { it, player ->
        if (it.cause != EntityDamageEvent.DamageCause.FALL) return@kitPlayerEvent
        val damage = it.damage
        val stomperDamage = min(4.0, damage)
        it.damage = stomperDamage
        val radius = kit.properties.radius
        player.getNearbyEntities(radius, radius, radius).filterIsInstance<Player>().forEach nearby@{ nearby ->
            if (nearby == player) return@nearby
            val nearbyKitPlayer = nearby.hgPlayer
            if (!nearbyKitPlayer.isAlive) return@nearby
            if (nearby.hasKit(Counter)) return@nearby
            nearby.damage(if (nearby.isSneaking) stomperDamage else damage, player)
        }
    }
}
