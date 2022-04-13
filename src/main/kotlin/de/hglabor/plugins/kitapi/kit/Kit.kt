package de.hglabor.plugins.kitapi.kit

import de.hglabor.plugins.hungergames.event.KitEnableEvent
import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.kitapi.cooldown.CooldownManager
import de.hglabor.plugins.kitapi.cooldown.CooldownProperties
import de.hglabor.plugins.kitapi.cooldown.MultipleUsesCooldownProperties
import de.hglabor.plugins.kitapi.player.PlayerKits.hasKit
import net.axay.kspigot.event.listen
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.setLore
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import java.util.concurrent.atomic.AtomicInteger

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
            player.hgPlayer.enableKit()
            for ((_: Int, item: KitItem) in items) {
                val kitItemStack = item.stack.apply {
                    meta {
                        displayName = "${ChatColor.DARK_PURPLE}${properties.kitname}"
                        setLore {
                            + "${ChatColor.DARK_PURPLE}Kititem"
                        }
                    }
                }
                if (!player.inventory.contains(kitItemStack))
                    player.inventory.addItem(kitItemStack)
            }
            val board = player.hgPlayer.board ?: return
            if (properties is CooldownProperties) {
                board.apply {
                    addLineBelow { "${ChatColor.LIGHT_PURPLE}${ChatColor.BOLD}Cooldown: ${ChatColor.WHITE}${CooldownManager.getRemainingCooldown(properties.cooldownInstance, player)}" }
                    if (properties is MultipleUsesCooldownProperties) {
                        addLineBelow { "${ChatColor.GRAY}${ChatColor.BOLD}Uses: ${ChatColor.WHITE}${properties.usesMap[player.uniqueId]}/${properties.uses}" }
                    }
                    addLineBelow(" ")
                }
            }
        }
    }

    val internal = this.Internal()

    init {
        properties.kitname = key

        if (properties is MultipleUsesCooldownProperties) {
            internal.kitPlayerEvents += listen<KitEnableEvent> {
                if (!it.player.hgPlayer.isAlive) return@listen
                if (!it.player.hasKit(this)) return@listen
                if (!properties.usesMap.contains(it.player.uniqueId)) {
                    properties.usesMap[it.player.uniqueId] = AtomicInteger(properties.uses)
                }
            }
        }
    }
}