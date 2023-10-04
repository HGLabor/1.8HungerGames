package de.hglabor.plugins.hungergames.kitsettings.gui.properties.property

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.PrimaryColor
import de.hglabor.plugins.hungergames.SecondaryColor
import de.hglabor.plugins.hungergames.kitsettings.gui.properties.KitPropertiesGUI
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import de.hglabor.plugins.kitapi.kit.NumberPropertySettings
import net.axay.kspigot.gui.*
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import net.axay.kspigot.items.setLore
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object IntPropertyGUI {
    fun openIntPropertyGui(
        player: Player,
        property: KitProperties.KitProperty<Int>,
        kit: Kit<*>
    ) {
        val settings = property.settings as NumberPropertySettings
        player.openGUI(kSpigotGUI(GUIType.SIX_BY_NINE) {
            title = "$Prefix${property.settings.propertyName}"
            page(1) {
                placeholder(Slots.All, itemStack(Material.STAINED_GLASS_PANE) { meta { name = null } })

                placeholder(
                    Slots.RowFiveSlotFive,
                    property.settings.display.displayItem
                )

                intIncrementButton(
                    this,
                    kit,
                    property,
                    Slots.RowThreeSlotTwo,
                    -100,
                )
                intIncrementButton(
                    this,
                    kit,
                    property,
                    Slots.RowThreeSlotThree,
                    -10,
                )
                intIncrementButton(
                    this,
                    kit,
                    property,
                    Slots.RowThreeSlotFour,
                    -1,
                )
                intIncrementButton(
                    this,
                    kit,
                    property,
                    Slots.RowThreeSlotSix,
                    1,
                )
                intIncrementButton(
                    this,
                    kit,
                    property,
                    Slots.RowThreeSlotSeven,
                    10,
                )
                intIncrementButton(
                    this,
                    kit,
                    property,
                    Slots.RowThreeSlotEight,
                    100,
                )

                val compound = createRectCompound<IntSlider>(Slots.RowTwoSlotTwo, Slots.RowTwoSlotEight,
                    iconGenerator = { slider ->
                        val (min, max) = listOf(settings.min, settings.max)
                        val percentage = slider.percentage
                        val colorData =
                            if ((percentage == 0 && property.get() > 0) || (max / 100 * percentage <= property.get()))
                                13 else 14
                        ItemStack(Material.STAINED_GLASS_PANE, 1, colorData.toShort()).apply {
                            meta {
                                name = null
                                setLore {
                                    +"${ChatColor.GRAY}Click to set value to ${ChatColor.WHITE}$percentage%"
                                    +"${ChatColor.GRAY}which is equal to ${ChatColor.WHITE}${
                                        (max / 100 * percentage).coerceAtMost(
                                            max
                                        ).coerceAtLeast(min)
                                    }"
                                    +""
                                    +"${ChatColor.DARK_GRAY}this might not be 100% correct"
                                    +"${ChatColor.DARK_GRAY}if the min value is not 0"
                                    +"${ChatColor.DARK_GRAY}but mathe geil"
                                }
                            }
                        }
                    }, onClick = { clickEvent, slider ->
                        clickEvent.bukkitEvent.isCancelled = true
                        val (min, max) = listOf(settings.min, settings.max)
                        val percentage = slider.percentage
                        property.set((max / 100 * percentage).coerceAtMost(max).coerceAtLeast(min), kit)
                        clickEvent.reload(property)
                    })
                compound.sortContentBy { it.percentage }
                compound.setContent(
                    listOf(
                        IntSlider(0),
                        IntSlider(20),
                        IntSlider(40),
                        IntSlider(50),
                        IntSlider(60),
                        IntSlider(80),
                        IntSlider(100),
                    )
                )

                button(Slots.RowOneSlotNine, itemStack(Material.BARRIER) { meta { name = "${ChatColor.RED}Back" } }) {
                    it.bukkitEvent.isCancelled = true
                    KitPropertiesGUI.openKitProperties(player, kit)
                }
            }
        })
    }

    private fun intIncrementButton(
        builder: GUIPageBuilder<ForInventorySixByNine>,
        kit: Kit<*>,
        property: KitProperties.KitProperty<Int>,
        slot: InventorySlotCompound<ForInventorySixByNine>,
        toIncrement: Int,
    ) {
        val settings = property.settings as NumberPropertySettings
        val min = settings.min
        val max = settings.max
        val isNegative = toIncrement < 0

        builder.apply {
            button(
                slot,
                ItemStack(Material.STAINED_GLASS_PANE, toIncrement, if (isNegative) 14 else 13).apply {
                    meta {
                        name = "${PrimaryColor}$toIncrement"
                    }
                }
            ) {
                property.set((property.get() + toIncrement).coerceAtMost(max).coerceAtLeast(min), kit)
                it.reload(property)
            }
        }
    }

    private fun GUIClickEvent<ForInventorySixByNine>.reload(property: KitProperties.KitProperty<Int>) {
        guiInstance[Slots.RowFiveSlotFive] = property.settings.display.displayItem
        guiInstance.reloadCurrentPage()
    }

    private class IntSlider(val percentage: Int)
}