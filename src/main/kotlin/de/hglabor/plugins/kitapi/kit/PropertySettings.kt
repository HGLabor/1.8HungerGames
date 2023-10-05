package de.hglabor.plugins.kitapi.kit

import de.hglabor.plugins.hungergames.PrimaryColor
import de.hglabor.plugins.hungergames.SecondaryColor
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import net.axay.kspigot.items.setLore
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.lang.StringBuilder

sealed class PropertySettings(val property: KitProperties.KitProperty<*>) {
    var propertyName: String = getPreferredName()
    var display = PropertyDisplay()

    open inner class PropertyDisplay {
        private fun getPreferredMaterial(): Material {
            val materialMap = hashMapOf(
                "enabled" to Material.COMMAND,
                "duration" to Material.REDSTONE,
                "amplifier" to Material.GLOWSTONE_DUST,
                "likelihood" to Material.DISPENSER,
                "soup" to Material.MUSHROOM_SOUP,
                "distance" to Material.COMPASS,
                "radius" to Material.ENDER_PEARL,
                "height" to Material.LADDER,
                "time" to Material.WATCH,
                "cooldown" to Material.WATCH,
                "uses" to Material.LEVER,
                "material" to Material.STONE,
                "fire" to Material.FLINT_AND_STEEL
            )
            return materialMap.firstNotNullOfOrNull {
                if (propertyName.contains(it.key, true)) it.value
                else null
            } ?: Material.BARRIER
        }

        /**
         * The item that will be shown in the property-gui
         * The name and lore of the item will be overwritten later
         */
        var displayItem = ItemStack(getPreferredMaterial())
            get() {
                return field.apply {
                    meta {
                        name = "$PrimaryColor$propertyName"
                        if (showValue) {
                            setLore {
                                +"${ChatColor.WHITE}Value ${ChatColor.DARK_GRAY}\u00BB $SecondaryColor${property.get()} $valueLoreExtension"
                                +"${ChatColor.WHITE}Default ${ChatColor.DARK_GRAY}\u00BB $SecondaryColor${property.defaultValue} $valueLoreExtension"

                                if (this@PropertySettings is NumberPropertySettings) {
                                    if (max != Int.MAX_VALUE || min != Int.MIN_VALUE) {
                                        +"${ChatColor.DARK_GRAY}${ChatColor.STRIKETHROUGH}                    "
                                        if (min != Int.MIN_VALUE)
                                            +"${ChatColor.GRAY}Min ${ChatColor.DARK_GRAY}\u00BB $SecondaryColor${min} $valueLoreExtension"
                                        if (max != Int.MAX_VALUE)
                                            +"${ChatColor.GRAY}Max ${ChatColor.DARK_GRAY}\u00BB $SecondaryColor${max} $valueLoreExtension"
                                    }
                                }
                            }
                        }
                    }
                }
            }

        /**
         * Sets the display-item of the setting.
         * @param itemStack the ItemStack that will be shown
         */
        fun displayItem(itemStack: ItemStack) {
            displayItem = itemStack
        }

        /**
         * Creates an ItemStack and sets it as the display-item of the setting.
         * @param material the material of the ItemStack that will be shown
         * @param material the amount of items
         */
        fun displayItem(material: Material, amount: Int = 1) {
            displayItem = ItemStack(material, amount)
        }

        /**
         * Sets if the value should be shown or not
         */
        var showValue: Boolean = true

        /**
         * The String that will be displayed after the value inside the itemlore
         * @sample "Prozent" -> Dropchance: $value Prozent
         */
        var valueLoreExtension: String = ""
    }

    inline fun display(callback: PropertyDisplay.() -> Unit) {
        display = PropertyDisplay().apply(callback)
    }

    private fun getPreferredName(): String {
        return StringBuilder().apply {
            var prevCharIsUpperCase = false
            property.kProperty.name.forEach {
                if (it.isUpperCase() && !prevCharIsUpperCase) {
                    append(' ')
                }
                append(it)
                prevCharIsUpperCase = it.isUpperCase()
            }
        }.toString().replaceFirstChar { it.uppercase() }
    }
}

class NumberPropertySettings(property: KitProperties.KitProperty<*>): PropertySettings(property) {
    var max = getPreferredMax()
    var min = 0

    private fun getPreferredMax(): Int {
        return hashMapOf(
            "duration" to 600,
            "amplifier" to 200,
            "likelihood" to 100,
            "soup" to 20,
            "distance" to 100,
            "radius" to 50,
            "height" to 50,
            "time" to 600,
            "cooldown" to 600,
            "uses" to 100,
            "ticks" to 600
        ).firstNotNullOfOrNull {
            if (propertyName.contains(it.key, true)) it.value
            else null
        } ?: 0
    }
}

class OtherPropertySettings(property: KitProperties.KitProperty<*>): PropertySettings(property)