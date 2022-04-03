package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import org.bukkit.Effect
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class ZickZackProperties : KitProperties() {
    val likelihood by int(33)
    val minCombo by int(3)
}

val ZickZack = Kit("ZickZack", ::ZickZackProperties) {
    displayMaterial = Material.DIAMOND_BLOCK
    val comboMap: HashMap<UUID, AtomicInteger> = hashMapOf()
    val shieldMap: HashMap<UUID, AtomicInteger> = hashMapOf()

    kitPlayerEvent<EntityDamageByEntityEvent>({ it.damager as? Player }) { it, damager ->
        if (!(it.entity as? Player ?: return@kitPlayerEvent).hgPlayer.isAlive) return@kitPlayerEvent
        comboMap.computeIfAbsent(damager.uniqueId) { AtomicInteger(0) }
        val combo = comboMap[damager.uniqueId]?.incrementAndGet() ?: 0
        if (combo < kit.properties.minCombo) return@kitPlayerEvent
        if ((0..100).random() <= kit.properties.likelihood) {
            shieldMap.computeIfAbsent(damager.uniqueId) { AtomicInteger(0) }.incrementAndGet()
        }
    }

    kitPlayerEvent<EntityDamageEvent>({ it.entity as? Player }) { it, player ->
        comboMap.computeIfAbsent(player.uniqueId) { AtomicInteger(0) }.set(0)
        val shield = shieldMap[player.uniqueId] ?: return@kitPlayerEvent
        if (shield.get() > 0) {
            it.isCancelled = true
            player.world.playEffect(player.eyeLocation.add(0.0, 0.5, 0.0), Effect.HEART, shield.decrementAndGet())
            return@kitPlayerEvent
        }
    }
}
