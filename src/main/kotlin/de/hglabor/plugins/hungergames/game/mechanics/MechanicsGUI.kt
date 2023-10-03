package de.hglabor.plugins.hungergames.game.mechanics

import de.hglabor.plugins.hungergames.PrimaryColor
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.phase.phases.PvPPhase
import net.axay.kspigot.event.listen
import net.axay.kspigot.gui.*
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import net.axay.kspigot.items.toLoreList
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class MechanicsGUI(val mechanics: List<Mechanic>) {
    companion object {
        val mechanicsGuiItem = itemStack(Material.BEACON) { meta { name = "${PrimaryColor}Game Mechanics" } }
    }

    val gui = kSpigotGUI(GUIType.FIVE_BY_NINE) {
        title = "${PrimaryColor}Mechanics"

        page(1) {
            val compound = createRectCompound<Mechanic>(Slots.RowThreeSlotTwo, Slots.RowFiveSlotEight,
                iconGenerator = { mechanic ->
                    mechanic.internal.displayItem.clone().apply {
                        meta {
                            name = "${if (mechanic.internal.isEnabled) ChatColor.GREEN else ChatColor.RED}${mechanic.name}"
                            addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                            addItemFlags(ItemFlag.HIDE_ENCHANTS)
                            if (mechanic.internal.isEnabled) {
                                addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1)
                            }
                            mechanic.description?.let { description ->
                                lore = description.toLoreList()
                            }
                        }
                    }
                },
                onClick = { clickEvent, mechanic ->
                    clickEvent.bukkitEvent.isCancelled = true
                    mechanic.internal.isEnabled = !mechanic.internal.isEnabled
                    clickEvent.guiInstance.reloadCurrentPage()
                })
            compound.sortContentBy { mechanic -> mechanic.name.lowercase() }
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
            compound.setContent(mechanics.filter { !it.isEvent })

            placeholder(Slots.RowOneSlotOne, itemStack(Material.COMMAND) {
                meta {
                    name = "${PrimaryColor}Event Mechanics"
                }
            })
            val specialCompound = createRectCompound<Mechanic>(Slots.RowOneSlotTwo, Slots.RowOneSlotEight,
                iconGenerator = { mechanic ->
                    mechanic.internal.displayItem.clone().apply {
                        meta {
                            name = "${if (mechanic.internal.isEnabled) ChatColor.GREEN else ChatColor.RED}${mechanic.name}"
                            addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                            addItemFlags(ItemFlag.HIDE_ENCHANTS)
                            if (mechanic.internal.isEnabled) {
                                addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1)
                            }
                            mechanic.description?.let { description ->
                                lore = description.toLoreList()
                            }
                        }
                    }
                },
                onClick = { clickEvent, mechanic ->
                    clickEvent.bukkitEvent.isCancelled = true
                    mechanic.internal.isEnabled = !mechanic.internal.isEnabled
                    clickEvent.guiInstance.reloadCurrentPage()
                })
            specialCompound.sortContentBy { mechanic -> mechanic.name.lowercase() }
            specialCompound.setContent(mechanics.filter { it.isEvent })
        }
    }

    fun register() {
        listen<PlayerInteractEvent> {
            if (it.item == mechanicsGuiItem) {
                if (GameManager.phase == PvPPhase) {
                    it.player.inventory.remove(mechanicsGuiItem)
                } else {
                    it.player.openGUI(gui)
                }
            }
        }

        listen<BlockPlaceEvent> {
            if (it.player.itemInHand == mechanicsGuiItem) {
                it.isCancelled = true
            }
        }
    }
}