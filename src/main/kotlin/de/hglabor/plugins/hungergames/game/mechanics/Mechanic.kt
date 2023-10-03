package de.hglabor.plugins.hungergames.game.mechanics

import net.axay.kspigot.event.SingleListener
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class Mechanic(val name: String, val isEvent: Boolean) {
    companion object {
        fun createRawMechanic(name: String, isEvent: Boolean) = Mechanic(name, isEvent)

        inline operator fun invoke(
            key: Any,
            isEvent: Boolean = false,
            crossinline builder: MechanicBuilder.() -> Unit,
        ) = lazy {
            createRawMechanic(key.toString(), isEvent).apply {
                MechanicBuilder(this).apply(builder)
            }
        }
    }

    inner class Internal internal constructor() {
        val mechanicEvents = HashSet<SingleListener<*>>()
        var displayItem: ItemStack = ItemStack(Material.BARRIER)
        var isEnabled = !isEvent
            set(value) {
                field = value
                if (value) onEnable?.invoke()
                else onDisable?.invoke()
            }
        var onEnable: (() -> Unit)? = null
        var onDisable: (() -> Unit)? = null
        var onGameStart: (() -> Unit)? = null
        var onTick: ((second: Int) -> Unit)? = null
    }

    val internal = this.Internal()

    open var description: String? = null

    fun onEnable() {
        internal.onEnable?.invoke()
    }

    fun onGameStart() {
        internal.onGameStart?.invoke()
    }

    fun onTick(second: Int) {
        if (!internal.isEnabled) return
        internal.onTick?.invoke(second)
    }
}