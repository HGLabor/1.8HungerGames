package de.hglabor.plugins.kitapi.kit

import net.axay.kspigot.chat.KColors
import net.axay.kspigot.items.meta
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

open class Kit<P : KitProperties> private constructor(val key: String, val properties: P) {

    companion object {
        /**
         * Do not use this function, it is only there to expose
         * the private constructor publicly for inlining from a safe
         * place.
         */
        fun <P : KitProperties> createRawKit(key: String, properties: P) =
            Kit(key, properties)

        /**
         * Creates a new lazy kit delegate.
         *
         * Usage:
         * ```kt
         * val MyKit = Kit("MyKit", ::MyKitProperties) { }
         * // or for instant access
         * val MyKit by Kit("MyKit", ::MyKitProperties) { }
         * ```
         *
         * @param key the unique key of this kit
         * @param properties the properties callback, for creating a properties instance
         * for this kit
         * @param builder the [KitBuilder]
         */
        inline operator fun <P : KitProperties> invoke(
            key: Any,
            crossinline properties: () -> P,
            crossinline builder: KitBuilder<P>.() -> Unit,
        ) = lazy {
            createRawKit(key.toString(), properties.invoke()).apply {
                KitBuilder(this).apply(builder)
            }
        }
    }

    inner class Internal internal constructor() {
        val items = HashMap<Int, KitItem>()
        val kitPlayerEvents = HashSet<Listener>()
        var displayItem: ItemStack = ItemStack(Material.BARRIER)

        fun givePlayer(player: Player) {
            for ((id: Int, item: KitItem) in items) {
                val kitItemStack = item.stack.apply {
                    meta {
                        displayName = "${ChatColor.DARK_PURPLE}${properties.kitname}"
                    }
                }
                if (!player.inventory.contains(kitItemStack))
                    player.inventory.addItem(kitItemStack)
            }
        }
    }

    val internal = this.Internal()

    init {
        properties.kitname = key
    }
}