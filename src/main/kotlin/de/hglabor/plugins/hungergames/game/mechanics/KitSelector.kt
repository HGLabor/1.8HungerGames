package de.hglabor.plugins.hungergames.game.mechanics

import de.hglabor.plugins.hungergames.player.hgPlayer
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
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent

object KitSelector {
    val kitSelectorItem = itemStack(Material.CHEST) { meta { name = "${ChatColor.LIGHT_PURPLE}Kit Selector" } }

    fun register() {
        listen<PlayerInteractEvent> {
            if (it.item == kitSelectorItem) {
                it.player.openGUI(kSpigotGUI(GUIType.FIVE_BY_NINE) {
                    page(1) {
                        val compound = createRectCompound<Kit<*>>(Slots.RowTwoSlotTwo, Slots.RowFourSlotEight,
                            iconGenerator = { kit ->
                                kit.internal.displayItem.apply {
                                    meta {
                                        name = "${ChatColor.LIGHT_PURPLE}${kit.properties.kitname}"
                                    }
                                }
                            },
                            onClick = { clickEvent, kit ->
                                clickEvent.bukkitEvent.isCancelled = true
                                clickEvent.player.chooseKit(kit)
                                clickEvent.player.closeInventory()
                            })
                        compound.sortContentBy { kit -> kit.properties.kitname }
                        compound.setContent(KitManager.kits)
                    }
                })
            }
        }
    }
}