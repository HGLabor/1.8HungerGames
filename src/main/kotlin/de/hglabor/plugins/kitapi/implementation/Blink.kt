package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.kitapi.cooldown.MultipleUsesCooldownProperties
import de.hglabor.plugins.kitapi.cooldown.applyCooldown
import de.hglabor.plugins.kitapi.kit.Kit
import net.axay.kspigot.extensions.events.isRightClick
import net.axay.kspigot.extensions.geometry.add
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack


class BlinkProperties : MultipleUsesCooldownProperties(4, 16000) {
    val distance by double(4.0)
}

val Blink = Kit("Blink", ::BlinkProperties) {
    displayMaterial = Material.NETHER_STAR
    description = "${ChatColor.WHITE}Right click ${ChatColor.GRAY}your kit-item to teleport in the direction you are looking"

    fun Location.isSafe(): Boolean {
        val feet: Block = block
        if (!feet.type.isTransparent && !feet.location.add(0.0, 1.0, 0.0).block.type.isTransparent) {
            return false // not transparent (will suffocate)
        }
        val head: Block = feet.getRelative(BlockFace.UP)
        if (!head.type.isTransparent) {
            return false // not transparent (will suffocate)
        }
        return true
    }

    clickableItem(ItemStack(Material.NETHER_STAR)) {
        if (!it.action.isRightClick) return@clickableItem
        applyCooldown(it) {
            val player = it.player
            val toLocation = player.location.add(player.location.direction.normalize().multiply(kit.properties.distance)).add(0.0, 0.5, 0.0)
            if (toLocation.isSafe()) {
                player.teleport(toLocation)
                player.playSound(player.location, Sound.FIREWORK_LAUNCH, 100f, 100f)
                player.location.subtract(0.0, 1.0, 0.0).block.setType(Material.LEAVES, false)
            } else {
                player.sendMessage("${Prefix}This location is ${ChatColor.RED}not safe${ChatColor.GRAY}. You would have suffocated.")
                cancelCooldown()
            }
        }
    }
}