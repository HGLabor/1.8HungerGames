package de.hglabor.plugins.hungergames.kitsettings.gui.properties

import de.hglabor.plugins.hungergames.PrimaryColor
import de.hglabor.plugins.hungergames.kitsettings.gui.KitSettingsGUI
import de.hglabor.plugins.hungergames.kitsettings.gui.properties.property.EnumPropertyGUI
import de.hglabor.plugins.hungergames.kitsettings.gui.properties.property.IntPropertyGUI
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import de.hglabor.plugins.kitapi.kit.NumberPropertySettings
import net.axay.kspigot.gui.GUIType
import net.axay.kspigot.gui.Slots
import net.axay.kspigot.gui.kSpigotGUI
import net.axay.kspigot.gui.openGUI
import net.axay.kspigot.items.*
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player

object KitPropertiesGUI {
    fun openKitProperties(player: Player, kit: Kit<*>) {
        player.openGUI(kSpigotGUI(GUIType.THREE_BY_NINE) {
            title = "${PrimaryColor}${kit.properties.kitname}"

            page(1) {
                placeholder(Slots.Border, itemStack(Material.STAINED_GLASS_PANE) { meta { name = null } })
                val compound = createRectCompound<KitProperties.KitProperty<*>>(
                    Slots.RowTwoSlotTwo,
                    Slots.RowTwoSlotEight,
                    iconGenerator = { property ->
                        val display = property.settings.display
                        display.displayItem
                    },
                    onClick = { clickEvent, property ->
                        clickEvent.bukkitEvent.isCancelled = true
                        val value = property.get()!!
                        when (value::class) {
                            Int::class -> IntPropertyGUI.openIntPropertyGui(
                                player,
                                property as KitProperties.KitProperty<Int>,
                                kit
                            )
                            Boolean::class -> property.set(!(property.get() as Boolean), kit)
                            //String::class -> StringPropertyGUI.openStringPropertyGui(player, property as KitProperties.KitProperty<String>, kit)
                            else -> {
                                if (value::class.java.isEnum) {
                                    EnumPropertyGUI.openEnumPropertyGui(
                                        player,
                                        property as KitProperties.KitProperty<Enum<*>>,
                                        kit
                                    )
                                }
                            }
                        }
                        clickEvent.guiInstance.reloadCurrentPage()
                    })
                compound.setContent(kit.properties.properties)
                button(Slots.RowOneSlotNine, itemStack(Material.BARRIER) { meta { name = "${ChatColor.RED}Close" } }) {
                    it.bukkitEvent.isCancelled = true
                    KitSettingsGUI.open(player)
                }
            }
        })
    }
}