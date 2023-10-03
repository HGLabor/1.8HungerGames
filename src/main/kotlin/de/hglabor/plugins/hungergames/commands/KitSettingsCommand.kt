package de.hglabor.plugins.hungergames.commands

import de.hglabor.plugins.kitapi.kit.KitManager
import de.hglabor.plugins.kitapi.kit.KitProperties
import net.axay.kspigot.extensions.broadcast
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.typeOf

object KitSettingsCommand : CommandExecutor, TabCompleter {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        val player = sender as? Player ?: return false
        if (args.size != 3) {
            player.sendMessage("use /kitsettings <kit> <property> <value>")
            return false
        }
        val (kitName, propertyName, newValue) = args

        // getting the kit
        val kit = KitManager.kits.firstOrNull { kit -> kit.properties.kitname.startsWith(kitName, true) }
        if (kit == null) {
            player.sendMessage("kit not found")
            return false
        }

        // getting the property
        val property = kit.properties.propertyList.firstOrNull { property ->
            property.kProperty.name.startsWith(
                propertyName,
                true
            )
        }
        if (property == null) {
            player.sendMessage("property not found")
            return false
        }

        // setting the new value
        if (setPropertyValue(kit.properties, property, newValue)) {
            player.sendMessage("set '$propertyName' of '$kitName' to '$newValue'")
        } else {
            player.sendMessage("falsch?")
        }
        return true
    }

    private fun <T> setPropertyValue(
        kitProperties: KitProperties,
        property: KitProperties.KitProperty<T>,
        valueAsString: String
    ): Boolean {
        val defaultValue = property.defaultValue ?: return false
        val propertyType = defaultValue::class.createType()

        val newValue = parseValue(valueAsString, propertyType) ?: return false
        property.setValue(kitProperties, property.kProperty, newValue as T)
        return true
    }

    private fun parseValue(valueAsString: String, kType: KType): Any? {
        return when (kType) {
            typeOf<String>() -> valueAsString
            typeOf<Material>() -> Material.values()
                .firstOrNull { material -> material.name.startsWith(valueAsString, true) }

            typeOf<Boolean>() -> valueAsString.lowercase().toBooleanStrictOrNull()
            typeOf<Int>() -> valueAsString.toInt()
            typeOf<Long>() -> valueAsString.toLong()
            typeOf<Float>() -> valueAsString.toFloat()
            typeOf<Double>() -> valueAsString.toDouble()

            else -> {
                val stringsClass = Class.forName("kotlin.text.StringsKt")
                val methodName = "to${kType.jvmErasure.simpleName}OrNull"

                val method = try {
                    stringsClass.getMethod(methodName, String::class.java)
                } catch (e: NoSuchMethodException) {
                    throw IllegalArgumentException("Method $methodName is not valid")
                }.apply { isAccessible = true }

                method.invoke(null, valueAsString)
                    ?: throw IllegalArgumentException("Couldn't parse '$valueAsString' to type $kType")
            }
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        cmd: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String>? {

        when (args.size) {
            1 -> {
                val (kitName) = args
                val allKits = KitManager.kits.map { it.properties.kitname }
                val kits = if (kitName.isBlank()) allKits
                else allKits.filter { it.startsWith(kitName, true) }

                return kits.takeIf { it.isNotEmpty() }?.toMutableList() ?: allKits.toMutableList()
            }

            2 -> {
                val (kitName, propertyName) = args
                val kit = KitManager.kits.firstOrNull { kit -> kit.properties.kitname.startsWith(kitName, true) }
                    ?: return mutableListOf("Kit not found")

                val allProperties = kit.properties.propertyList.map { it.kProperty.name }
                val properties = if (propertyName.isBlank()) allProperties
                else allProperties.filter { it.startsWith(propertyName, true) }

                return properties.takeIf { it.isNotEmpty() }?.toMutableList() ?: properties.toMutableList()
            }

            3 -> {
                val (kitName, propertyName) = args
                val kit = KitManager.kits.firstOrNull { kit -> kit.properties.kitname.startsWith(kitName, true) }
                    ?: return mutableListOf("Kit not found")
                val property = kit.properties.propertyList.firstOrNull { it.kProperty.name.startsWith(propertyName, true) }
                    ?: return mutableListOf("Property not found")

                val defaultValue = property.defaultValue ?: return mutableListOf("Property not found")
                val dataType = defaultValue::class.createType().jvmErasure.simpleName

                return mutableListOf("$dataType value")
            }
        }
        return null
    }
}