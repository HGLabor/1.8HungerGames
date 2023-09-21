package de.hglabor.plugins.hungergames.game.mechanics.implementation.arena

import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.mechanics.Mechanic
import de.hglabor.plugins.hungergames.game.phase.phases.EndPhase
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

val ArenaMechanic by Mechanic("Arena") {
    description = "After dieing for the first time, fight for a revive!"
    displayMaterial = Material.IRON_SWORD

    onEnable {
        task(true, 20, 20) {
            if (GameManager.phase == EndPhase) {
                it.cancel()
                return@task
            }

            // Revive single player if arena is closed
            if (!Arena.isOpen && Arena.queuedPlayers.size == 1) {
                val revived = Arena.queuedPlayers.single()
                revived.makeGameReady()
                revived.bukkitPlayer?.inventory?.apply {
                    addItem(ItemStack(Material.STONE_SWORD))
                    for (i in 0..35) {
                        addItem(ItemStack(Material.MUSHROOM_SOUP))
                    }
                }
                broadcast("${Arena.Prefix}${ChatColor.WHITE}${revived.name} ${ChatColor.GRAY}was revived.")
            }

            if (Arena.currentMatch?.isEnded == false) {
                Arena.currentMatch?.tick()
            } else {
                Arena.startNewMatch()
            }
        }
    }
}