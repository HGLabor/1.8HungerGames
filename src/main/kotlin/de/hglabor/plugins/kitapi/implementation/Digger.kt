package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.mechanics.feast.Feast
import de.hglabor.plugins.hungergames.game.phase.phases.InvincibilityPhase
import de.hglabor.plugins.kitapi.cooldown.CooldownProperties
import de.hglabor.plugins.kitapi.cooldown.applyCooldown
import de.hglabor.plugins.kitapi.kit.Kit
import net.axay.kspigot.extensions.geometry.add
import net.axay.kspigot.runnables.taskRunLater
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import java.awt.Container

class DiggerProperties : CooldownProperties(12000) {
    val radius by int(5)
}

val Digger = Kit("Digger", ::DiggerProperties) {
    displayMaterial = Material.DRAGON_EGG

    fun Block.isReplaceable(): Boolean =
        when {
            hasMetadata(Feast.BLOCK_KEY) -> false
            this is Container -> {
                this.breakNaturally()
                false
            }

            else -> {
                when (type) {
                    Material.BEDROCK, Material.AIR -> false
                    else -> true
                }
            }
        }

    placeableItem(ItemStack(Material.DRAGON_EGG)) {
        if (GameManager.phase == InvincibilityPhase) {
            it.player.sendMessage("${Prefix}You can't use this kit while during the grace period.")
            return@placeableItem
        }
        it.isCancelled = true
        applyCooldown(it.player) {
            val radius = kit.properties.radius
            val eggLocation = it.block.location
            taskRunLater(15, true) {
                it.player.world.playSound(eggLocation, Sound.DIG_STONE, 1f, 1f)
                for (x in -radius..radius) {
                    for (y in -1 downTo -radius) {
                        for (z in -radius..radius) {
                            val block = eggLocation.clone().add(x, y, z).block
                            if (!block.isReplaceable()) continue
                            block.setType(Material.AIR, false)
                        }
                    }
                }
            }
        }
    }
}
