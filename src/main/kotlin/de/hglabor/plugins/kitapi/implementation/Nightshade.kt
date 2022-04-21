package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.event.PlayerSoupEvent
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.utils.hasMark
import de.hglabor.plugins.hungergames.utils.mark
import de.hglabor.plugins.hungergames.utils.unmark
import de.hglabor.plugins.kitapi.cooldown.CooldownProperties
import de.hglabor.plugins.kitapi.cooldown.applyCooldown
import de.hglabor.plugins.kitapi.kit.Kit
import net.axay.kspigot.event.listen
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import net.axay.kspigot.runnables.taskRunLater
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class NightshadeProperties  : CooldownProperties(20000) {
    val duration by long(12*20)
}

val Nightshade = Kit("Nightshade", ::NightshadeProperties) {
    displayMaterial = Material.NETHER_BRICK_ITEM

    clickOnEntityItem(ItemStack(Material.NETHER_BRICK_ITEM)) {
        val rightClicked = it.rightClicked as? Player ?: return@clickOnEntityItem
        applyCooldown(it) {
            if (rightClicked.hasMark("nightshadeHealth")) {
                it.player.sendMessage("${Prefix}This player is already affected by nightshade")
                cancelCooldown()
                return@clickOnEntityItem
            }

            rightClicked.healthScale -= 4.0
            rightClicked.mark("nightshadeHealth")

            repeat(if (GameManager.feast?.isFinished == true) 2 else 1) {
                val slot = (0..9)
                    .filter { s ->
                        val item = rightClicked.inventory.getItem(s)
                        item != null && item.type == Material.MUSHROOM_SOUP && item.itemMeta != null && item.itemMeta.name != "Nightshade"
                    }.randomOrNull()

                if (slot != null) {
                    rightClicked.inventory.getItem(slot).apply {
                        meta {
                            name = "Nightshade"
                        }
                    }
                }
            }

            taskRunLater(kit.properties.duration) {
                rightClicked.healthScale += 4.0
                rightClicked.unmark("nightshadeHealth")
            }
        }
    }

    fun Player.giveNightshadeWither() {
        player.mark("nightshadeEffect")
        player.addPotionEffect(PotionEffect(PotionEffectType.WITHER, 80, 3))
        taskRunLater(80) {
            player.unmark("nightshadeEffect")
        }
    }

    listen<PlayerSoupEvent> {
        val player = it.player

        if (player.itemInHand.itemMeta.name == "Nightshade") {
            player.giveNightshadeWither()
            return@listen
        }

        if (!player.hasMark("nightshadeHealth")) return@listen
        if (it.overhealed) {
            if (!player.hasMark("nightshadeEffect")) {
                player.giveNightshadeWither()
            }
        }
    }
}
