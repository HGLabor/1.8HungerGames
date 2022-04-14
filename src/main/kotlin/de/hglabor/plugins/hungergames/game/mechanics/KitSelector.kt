package de.hglabor.plugins.hungergames.game.mechanics

import de.hglabor.plugins.hungergames.PrimaryColor
import de.hglabor.plugins.hungergames.SecondaryColor
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitManager
import de.hglabor.plugins.kitapi.player.PlayerKits.chooseKit
import net.axay.kspigot.event.listen
import net.axay.kspigot.gui.GUIType
import net.axay.kspigot.gui.Slots
import net.axay.kspigot.gui.kSpigotGUI
import net.axay.kspigot.gui.openGUI
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

object KitSelector {
    val kitSelectorItem = itemStack(Material.CHEST) { meta { name = "${SecondaryColor}Kit Selector" } }
    val gui = kSpigotGUI(GUIType.FIVE_BY_NINE) {
        title = "${SecondaryColor}Kitselector"
        page(1) {
            val compound = createRectCompound<Kit<*>>(Slots.RowOneSlotTwo, Slots.RowFiveSlotEight,
                iconGenerator = { kit ->
                    kit.internal.displayItem.clone().apply {
                        meta {
                            name = "${SecondaryColor}${kit.properties.kitname}"
                            addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                        }
                    }
                },
                onClick = { clickEvent, kit ->
                    clickEvent.bukkitEvent.isCancelled = true
                    clickEvent.player.chooseKit(kit)
                    clickEvent.player.closeInventory()
                })
            compound.sortContentBy { kit -> kit.properties.kitname }
            compoundScroll(
                Slots.RowThreeSlotNine,
                ItemStack(Material.STAINED_GLASS_PANE, 1, 5).apply {
                    meta {
                        name = "${PrimaryColor}Next"
                    }
                }, compound, 7 * 4, reverse = true
            )
            compoundScroll(
                Slots.RowThreeSlotOne,
                ItemStack(Material.STAINED_GLASS_PANE, 1, 14).apply {
                    meta {
                        name = "${PrimaryColor}Previous"
                    }
                }, compound, 7 * 4
            )
            compound.setContent(KitManager.kits)
        }
    }

    fun register() {
        listen<PlayerInteractEvent> {
            if (it.item == kitSelectorItem) {
                it.player.openGUI(gui)
            }
        }
    }
}