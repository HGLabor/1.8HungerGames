package de.hglabor.plugins.kitapi.kit

import de.hglabor.plugins.hungergames.event.KitPropertyChangeEvent
import org.bukkit.Bukkit
import org.bukkit.Material
import kotlin.reflect.KProperty

abstract class KitProperties {
    val properties = mutableListOf<KitProperty<*>>()
    lateinit var kitname: String
        internal set

    val isEnabled by boolean(true)

    abstract class KitProperty<T> {
        abstract val defaultValue: T
        protected var value: T? = null
        lateinit var kProperty: KProperty<*>
        lateinit var settings: PropertySettings

        operator fun getValue(thisRef: Any, property: KProperty<*>): T {
            return value ?: defaultValue
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) {
            value = newValue
        }

        operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): KitProperty<T> {
            kProperty = property
            settings = (if (defaultValue is Number) NumberPropertySettings(this as KitProperty<Number>)
            else OtherPropertySettings(this as KitProperty<*>))
            return this
        }

        fun set(any: Any?, kit: Kit<*>) {
            value = any as T
            Bukkit.getPluginManager().callEvent(KitPropertyChangeEvent(kit, this))
        }

        abstract fun get(): T
    }

    private inline fun <reified T : Any> any(default: T) = object : KitProperty<T>() {
        override val defaultValue = default
        override fun get(): T {
            return value ?: defaultValue
        }

        init {
            properties += this
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