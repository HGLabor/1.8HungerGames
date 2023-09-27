package de.hglabor.plugins.hungergames.game.mechanics.implementation

import de.hglabor.plugins.hungergames.game.mechanics.Mechanic
import de.hglabor.plugins.hungergames.utils.ChanceUtils
import net.axay.kspigot.event.listen
import org.bukkit.Material
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.material.MaterialData

val MoreDurability by Mechanic("More Durability") {
    description = "Items (excluding armor) will have more durability"
    displayMaterial = Material.ANVIL

    fun ItemStack.isArmor(): Boolean {
        return type.name.endsWith("_HELMET") || type.name.endsWith("_CHESTPLATE") ||
            type.name.endsWith("_LEGGINGS") || type.name.endsWith("_BOOTS")
    }

    mechanicEvent<PlayerItemDamageEvent> {
        if (it.item.isArmor()) return@mechanicEvent
        if (ChanceUtils.roll(35)) {
            it.isCancelled = true
        }
    }
}