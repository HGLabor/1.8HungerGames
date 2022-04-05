package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.event.KitEnableEvent
import de.hglabor.plugins.kitapi.kit.EmptyProperties
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.player.PlayerKits.hasKit
import net.axay.kspigot.event.listen
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.ItemStack

val Fireman = Kit("Fireman", ::EmptyProperties)  {

    displayMaterial = Material.WATER_BUCKET

    listen<EntityDamageEvent> {
        if(it.entity is Player) {
            val player = it.entity as Player
            if (it.cause == EntityDamageEvent.DamageCause.FIRE || it.cause == EntityDamageEvent.DamageCause.LAVA || it.cause == EntityDamageEvent.DamageCause.FIRE_TICK) {
                if(player.hasKit(kit)) {
                    it.isCancelled = true
                    player.fireTicks = 0
                }
            }
        }
    }

    kitPlayerEvent<KitEnableEvent> {
        it.player.inventory.addItem(ItemStack(Material.WATER_BUCKET))
    }

}