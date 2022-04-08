package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class RedstonerProperties : KitProperties()

val Redstoner = Kit("Redstoner", ::RedstonerProperties) {
    displayMaterial = Material.REDSTONE

    simpleItem(ItemStack(Material.PISTON_BASE, 32))
    simpleItem(ItemStack(Material.PISTON_STICKY_BASE, 16))
    simpleItem(ItemStack(Material.DISPENSER, 2))
    simpleItem(ItemStack(Material.DROPPER, 2))
    simpleItem(ItemStack(Material.DIODE, 4))
    simpleItem(ItemStack(Material.REDSTONE_COMPARATOR, 4))
    simpleItem(ItemStack(Material.TRIPWIRE_HOOK, 4))
    simpleItem(ItemStack(Material.STRING, 8))
    simpleItem(ItemStack(Material.REDSTONE, 64))
    simpleItem(ItemStack(Material.REDSTONE_TORCH_OFF, 12))
    simpleItem(ItemStack(Material.TNT, 12))
    simpleItem(ItemStack(Material.SLIME_BLOCK, 4))
}
