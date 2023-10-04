package de.hglabor.plugins.hungergames.kitsettings.gui.properties.property

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.PrimaryColor
import de.hglabor.plugins.hungergames.kitsettings.gui.properties.KitPropertiesGUI
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import net.axay.kspigot.gui.GUIType
import net.axay.kspigot.gui.Slots
import net.axay.kspigot.gui.kSpigotGUI
import net.axay.kspigot.gui.openGUI
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object EnumPropertyGUI {
    fun openEnumPropertyGui(
        player: Player,
        property: KitProperties.KitProperty<Enum<*>>,
        kit: Kit<*>
    ) {
        player.openGUI(kSpigotGUI(GUIType.SIX_BY_NINE) {
            title = "${Prefix}${property.settings.propertyName}"
            page(1) {
                placeholder(Slots.All, ItemStack(Material.STAINED_GLASS_PANE, 1, 14).apply { meta { name = null } })
                placeholder(Slots.Border, ItemStack(Material.STAINED_GLASS_PANE, 1, 13).apply { meta { name = null } })

                placeholder(Slots.RowFiveSlotFive, property.settings.display.displayItem)


                val compound = createRectCompound<Enum<*>>(Slots.RowTwoSlotTwo, Slots.RowFiveSlotEight,
                    iconGenerator = { enum ->
                        var isActive = property.get() == enum
                        val colorData = if (isActive) 13 else 14
                        ItemStack(Material.STAINED_GLASS_PANE, 1, colorData.toShort()).apply {
                            meta {
                                name = "${if (isActive) ChatColor.GREEN else ChatColor.RED}${enum.name}"
                            }
                        }
                    }, onClick = { clickEvent, enum ->
                        clickEvent.bukkitEvent.isCancelled = true
                        property.set(enum, kit)
                        clickEvent.guiInstance.reloadCurrentPage()
                    })
                compound.sortContentBy { it.name }
                compound.setContent((property.get()::class.java.enumConstants).toList())

                compoundScroll(
                    Slots.RowSixSlotFive,
                    ItemStack(Material.STAINED_GLASS_PANE, 1, 5).apply {
                        meta {
                            name = "${PrimaryColor}Next"
                        }
                    }, compound, scrollLines = 4, reverse = true
                )
                compoundScroll(
                    Slots.RowOneSlotFive,
                    ItemStack(Material.STAINED_GLASS_PANE, 1, 14).apply {
                        meta {
                            name = "${PrimaryColor}Previous"
                        }
                    }, compound, scrollLines = 4
                )

                button(Slots.RowOneSlotNine, itemStack(Material.BARRIER) { meta { name = "${ChatColor.RED}Back" } }) {
                    it.bukkitEvent.isCancelled = true
                    KitPropertiesGUI.openKitProperties(player, kit)
                }
            }
        })
    }
}