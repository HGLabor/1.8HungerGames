package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import org.bukkit.Material
import org.bukkit.event.EventHandler
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class PerfectProperties : KitProperties() {
    val soupAmountForReward by int(5)
    val minUsedSoups by int(3)
}

val Perfect = Kit("Perfect", ::PerfectProperties) {
    displayMaterial = Material.MUSHROOM_SOUP
    val comboMap: HashMap<UUID, AtomicInteger> = hashMapOf()
    val shieldMap: HashMap<UUID, AtomicInteger> = hashMapOf()}

@EventHandler
