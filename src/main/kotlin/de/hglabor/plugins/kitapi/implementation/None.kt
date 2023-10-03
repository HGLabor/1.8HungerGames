package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import org.bukkit.Material

class NoneProperties : KitProperties()

val None by Kit("None", ::NoneProperties) {
    displayMaterial = Material.BARRIER
    description = "Nothing?"
}
