package de.hglabor.plugins.hungergames.game.mechanics

import net.axay.kspigot.event.SingleListener
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class Mechanic(val name: String) {
    companion object {
        fun createRawMechanic(name: String) = Mechanic(name)

        inline operator fun invoke(
            key: Any,
            crossinline builder: MechanicBuilder.() -> Unit,
        ) = lazy {
            createRawMechanic(key.toString()).apply {
                MechanicBuilder(this).apply(builder)
            }
        }
    }

    inner class Internal internal constructor() {
        val mechanicEvents = HashSet<SingleListener<*>>()
        var displayItem: ItemStack = ItemStack(Material.BARRIER)
        var isEnabled = true
        var onEnable: (() -> Unit)? = null
        var onTick: ((second: Int) -> Unit)? = null
    }

    val internal = this.Internal()

    open var description: String? = null

    fun enable() {
        internal.onEnable?.invoke()
    }

    fun onTick(second: Int) {
        if (!internal.isEnabled) return
        internal.onTick?.invoke(second)
    }
}