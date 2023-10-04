package de.hglabor.plugins.hungergames.game.mechanics

import de.hglabor.plugins.hungergames.PrimaryColor
import de.hglabor.plugins.hungergames.game.mechanics.implementation.KitSelector
import de.hglabor.plugins.hungergames.kitsettings.gui.KitSettingsGUI
import net.axay.kspigot.event.listen
import net.axay.kspigot.gui.GUIType
import net.axay.kspigot.gui.Slots
import net.axay.kspigot.gui.kSpigotGUI
import net.axay.kspigot.gui.openGUI
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent

object SettingsGUI {
    val item = itemStack(Material.COMMAND) {
        meta {
            name = "${PrimaryColor}Settings"
        }
    }

    private val gui = kSpigotGUI(GUIType.THREE_BY_NINE) {
        title = "${PrimaryColor}Settings"
        page(1) {
            placeholder(Slots.All, itemStack(Material.STAINED_GLASS_PANE) {
                meta { name = null }
            })

            button(Slots.RowTwoSlotThree, itemStack(Material.COMMAND) {
                meta {
                    name = "${PrimaryColor}Mechanics"
                }
            }) {
                it.bukkitEvent.isCancelled = true
                MechanicsGUI.open(it.player)
            }

            button(Slots.RowTwoSlotSeven, itemStack(Material.CHEST) {
                meta {
                    name = "${PrimaryColor}Kit Settings"
                }
            }) {
                it.bukkitEvent.isCancelled = true
                KitSettingsGUI.open(it.player)
            }
        }
    }

    fun register() {
        listen<PlayerInteractEvent> {
            if (!it.player.isOp) {
                it.player.inventory.remove(item)
                return@listen
            }
            if (it.player.inventory.itemInHand.equals(item)) {
                open(it.player)
            }
        }

        listen<BlockPlaceEvent> {
            if (!it.player.isOp) {
                it.player.inventory.remove(item)
                return@listen
            }
            if (it.player.itemInHand == KitSelector.kitSelectorItem) {
                it.isCancelled = true
            }
        }
    }

    fun open(player: Player) {
        player.openGUI(gui)
    }
}