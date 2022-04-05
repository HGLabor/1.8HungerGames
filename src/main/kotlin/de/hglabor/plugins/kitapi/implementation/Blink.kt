package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.kitapi.cooldown.MultipleUsesCooldownProperties
import de.hglabor.plugins.kitapi.cooldown.applyCooldown
import de.hglabor.plugins.kitapi.kit.Kit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack


class BlinkProperties : MultipleUsesCooldownProperties(5, 16000) {
    val distance by double(4.0)
}

val Blink = Kit("Blink", ::BlinkProperties) {
    displayMaterial = Material.NETHER_STAR

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
        applyCooldown(it) {
            val player = it.player
            val toLocation =
                player.location.add(player.location.direction.normalize().multiply(kit.properties.distance))
            if (toLocation.isSafe()) {
                player.teleport(toLocation)
            } else {
                player.sendMessage("${Prefix}This location is ${ChatColor.RED}not safe${ChatColor.GRAY}. You would have suffocated.")
                cancelCooldown()
            }
            player.playSound(player.location, Sound.FIREWORK_LAUNCH, 100f, 100f)
            //player.sendMessage("Blink! Uses: ${kit.properties.getUses(player)}")
        }
    }
}