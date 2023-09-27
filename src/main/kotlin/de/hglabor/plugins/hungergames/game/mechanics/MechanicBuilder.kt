package de.hglabor.plugins.hungergames.game.mechanics

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.kitapi.cooldown.Cooldown
import de.hglabor.plugins.kitapi.cooldown.CooldownManager
import de.hglabor.plugins.kitapi.cooldown.CooldownScope
import de.hglabor.plugins.kitapi.cooldown.MultipleUsesCooldownProperties
import de.hglabor.plugins.kitapi.player.PlayerKits.hasKit
import net.axay.kspigot.event.listen
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class MechanicBuilder(val mechanic: Mechanic) {
    var displayItem: ItemStack
        get() = mechanic.internal.displayItem
        set(value) { mechanic.internal.displayItem = value }

    var displayMaterial: Material
        get() = mechanic.internal.displayItem.type
        set(value) { mechanic.internal.displayItem.type = value }

    var description: String?
        get() = mechanic.description
        set(value) { mechanic.description = value }

    /**
     * Executes the given [callback] if the Mechanic is enabled
     */
    inline fun <reified T : Event> mechanicEvent(priority: EventPriority = EventPriority.NORMAL, crossinline callback: (event: T) -> Unit) {
        mechanic.internal.mechanicEvents += listen<T>(priority = priority, ignoreCancelled = false) {
            if (!mechanic.internal.isEnabled) return@listen
            callback.invoke(it)
        }
    }

    /**
     * Executes the given [callback] if the Mechanic is enabled
     */
    inline fun <reified T : PlayerEvent> mechanicPlayerEvent(priority: EventPriority = EventPriority.NORMAL, crossinline callback: (event: T, player: Player) -> Unit) {
        mechanic.internal.mechanicEvents += listen<T>(priority = priority, ignoreCancelled = false) {
            if (!mechanic.internal.isEnabled) return@listen
            if (!it.player.hgPlayer.isAlive) return@listen
            callback.invoke(it, it.player)
        }
    }

    /**
     * Executes the given [callback] if the Mechanic is enabled
     */
    inline fun <reified T : Event> mechanicPlayerEvent(
        crossinline playerGetter: (T) -> Player?,
        priority: EventPriority = EventPriority.NORMAL,
        ignoreCancelled: Boolean = false,
        crossinline callback: (event: T, player: Player) -> Unit,
    ) {
        mechanic.internal.mechanicEvents += listen<T>(priority = priority, ignoreCancelled = ignoreCancelled) {
            if (!mechanic.internal.isEnabled) return@listen
            val player = playerGetter(it) ?: return@listen
            if (!player.hgPlayer.isAlive) return@listen
            callback(it, player)
        }
    }

    /**
    * [callback] will be executed when the game starts, if the Mechanic is enabled
    */
    internal fun onEnable(callback: () -> Unit) {
        mechanic.internal.onEnable = callback
    }
}