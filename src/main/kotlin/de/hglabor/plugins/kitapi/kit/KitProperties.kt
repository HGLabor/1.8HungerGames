package de.hglabor.plugins.kitapi.kit

import org.bukkit.Material
import kotlin.reflect.KProperty

abstract class KitProperties {
    val propertyList = mutableListOf<KitProperty<*>>()
    lateinit var kitname: String
        internal set

    abstract class KitProperty<T> {
        abstract val defaultValue: T
        lateinit var kProperty: KProperty<*>

        protected var value: T? = null

        operator fun getValue(thisRef: Any, property: KProperty<*>): T {
            return value ?: defaultValue
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) {
            value = newValue
        }

        operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): KitProperty<T> {
            kProperty = property
            return this
        }
    }

    private inline fun <reified T : Any> any(default: T) = object : KitProperty<T>() {
        override val defaultValue = default

        init {
            propertyList += this
        }
    }

    fun boolean(default: Boolean) = any(default)
    fun int(default: Int) = any(default)
    fun long(default: Long) = any(default)
    fun float(default: Float) = any(default)
    fun double(default: Double) = any(default)
    fun string(default: String) = any(default)
    fun material(default: Material) = any(default)
}