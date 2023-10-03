package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import org.bukkit.Material

class CounterProperties: KitProperties()

val Counter by Kit("Counter", ::CounterProperties) {
    displayMaterial = Material.STAINED_GLASS_PANE
    description = "Counter kits of your enemies"
}
