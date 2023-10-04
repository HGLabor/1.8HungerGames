package de.hglabor.plugins.hungergames.game.mechanics.implementation

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.PrimaryColor
import de.hglabor.plugins.hungergames.SecondaryColor
import de.hglabor.plugins.hungergames.event.KitPropertyChangeEvent
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.phase.IngamePhase
import de.hglabor.plugins.hungergames.game.phase.phases.LobbyPhase
import de.hglabor.plugins.hungergames.game.phase.phases.PvPPhase
import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.kitapi.implementation.None
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitManager
import de.hglabor.plugins.kitapi.player.PlayerKits.chooseKit
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.gui.GUIType
import net.axay.kspigot.gui.Slots
import net.axay.kspigot.gui.kSpigotGUI
import net.axay.kspigot.gui.openGUI
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

object KitSelector {
    val kitSelectorItem = itemStack(Material.CHEST) { meta { name = "${SecondaryColor}Kit Selector" } }
    val gui
        get() = kSpigotGUI(GUIType.FIVE_BY_NINE) {
            title = "${SecondaryColor}Kit Selector"
            page(1) {
                val compound = createRectCompound<Kit<*>>(Slots.RowOneSlotTwo, Slots.RowFiveSlotEight,
                    iconGenerator = { kit ->
                        kit.internal.displayItem.clone().apply {
                            meta {
                                name = "${SecondaryColor}${kit.properties.kitname}"
                                addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                                lore = kit.internal.description
                            }
                        }
                    },
                    onClick = { clickEvent, kit ->
                        clickEvent.bukkitEvent.isCancelled = true
                        if (RandomKits.internal.isEnabled) {
                            clickEvent.player.sendMessage("$Prefix${ChatColor.RED}You can't choose a kit whilst ${ChatColor.UNDERLINE}Random Kit${ChatColor.RED} is enabled")
                        } else {
                            clickEvent.player.chooseKit(kit)
                        }
                        clickEvent.player.closeInventory()
                    })
                compound.sortContentBy { kit -> kit.properties.kitname.lowercase() }
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
                compound.setContent(KitManager.kits.filter { it.properties.isEnabled })
            }
        }

    fun register() {
        listen<PlayerInteractEvent> {
            if (RandomKits.internal.isEnabled) return@listen
            if (it.item == kitSelectorItem) {
                if (GameManager.phase == PvPPhase) {
                    it.player.inventory.remove(kitSelectorItem)
                } else {
                    it.player.openGUI(gui)
                }
            }
        }

        listen<BlockPlaceEvent> {
            if (RandomKits.internal.isEnabled) return@listen
            if (it.player.itemInHand == kitSelectorItem) {
                it.isCancelled = true
            }
        }

        listen<KitPropertyChangeEvent> {
            val kit = it.kit
            if (it.property.kProperty.name != "isEnabled") return@listen
            val playersWithKit = PlayerList.allPlayers.filter { it.kit == kit }
            val newValue = kit.properties.isEnabled
            playersWithKit.forEach { hgPlayer ->
                if (!newValue && GameManager.phase == LobbyPhase) {
                    hgPlayer.kit = None
                    hgPlayer.changedKitBefore = false
                    hgPlayer.bukkitPlayer?.sendMessage("${Prefix}Your kit has been ${ChatColor.RED}disabled${ChatColor.GRAY}.")
                }
            }
        }
    }
}