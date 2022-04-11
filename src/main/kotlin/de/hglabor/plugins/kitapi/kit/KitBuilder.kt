package de.hglabor.plugins.kitapi.kit

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.event.PlayerSoupEvent
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

class KitBuilder<P : KitProperties>(val kit: Kit<P>) {
    private var currentItemId = 0

    /**
     * This just gives the [stack] to the player if
     * he has the kit.
     */

    var displayItem: ItemStack
        get() = kit.internal.displayItem
        set(value) { kit.internal.displayItem = value }

    var displayMaterial: Material
        get() = kit.internal.displayItem.type
        set(value) { kit.internal.displayItem.type = value }

    fun simpleItem(stack: ItemStack) {
        kit.internal.items[currentItemId++] = SimpleKitItem(stack)
    }

    /**
     * Gives the [stack] to the player if he has the kit
     * and executed the [onClick] callback when the player
     * interacts using the item.
     */
    fun clickableItem(stack: ItemStack, onClick: (PlayerInteractEvent) -> Unit) {
        kit.internal.items[currentItemId++] = ClickableKitItem(stack, onClick)
    }

    /**
     * Gives the [stack] to the player if he has the kit
     * and executed the [onClick] callback when the player
     * interacts with another entity using the item.
     */
    fun clickOnEntityItem(stack: ItemStack, onClick: (PlayerInteractAtEntityEvent) -> Unit) {
        kit.internal.items[currentItemId++] = ClickOnEntityKitItem(stack, onClick)
    }

    /**
     * Gives the [stack] to the player if he has the kit
     * and executed the [onClick] callback when the player
     * places the item.
     */
    fun placeableItem(stack: ItemStack, onBuild: (BlockPlaceEvent) -> Unit) {
        kit.internal.items[currentItemId++] = PlaceableKitItem(stack, onBuild)
    }

    /**
     * Executes the given [callback] if the player of the
     * [PlayerEvent] has this kit.
     */
    inline fun <reified T : PlayerEvent> kitPlayerEvent(crossinline callback: (event: T) -> Unit) {
        kit.internal.kitPlayerEvents += listen<T> {
            if (!it.player.hgPlayer.isAlive) return@listen
            if (it.player.hasKit(kit))
                callback.invoke(it)
        }
    }

    /**
     * Executes the given [callback] if the player of the
     * [playerGetter] has this kit.
     */
    inline fun <reified T : Event> kitPlayerEvent(
        crossinline playerGetter: (T) -> Player?,
        priority: EventPriority = EventPriority.NORMAL,
        ignoreCancelled: Boolean = false,
        crossinline callback: (event: T, player: Player) -> Unit,
    ) {
        kit.internal.kitPlayerEvents += listen<T>(priority = priority, ignoreCancelled = ignoreCancelled) {
            val player = playerGetter(it) ?: return@listen
            if (!player.hgPlayer.isAlive) return@listen
            if (player.hasKit(kit))
                callback(it, player)
        }
    }

    /**
     * Executes the given [block] if the player does not currently
     * have a cooldown for this action.
     *
     * Inside if the [CooldownScope], you can use [CooldownScope.cancelCooldown]
     * if you wish to not apply the cooldown regardless of the [block]
     * being executed.
     */
    inline fun Player.applyCooldown(cooldown: Cooldown, block: CooldownScope.() -> Unit) {
        if (!CooldownManager.hasCooldown(cooldown, this)) {
            val properties = kit.properties

            if (properties is MultipleUsesCooldownProperties) {
                if (properties.hasUses(this)) {
                    if (CooldownScope().apply(block).shouldApply) {
                        properties.decrementUses(this)
                    }
                } else {
                    CooldownManager.addCooldown(cooldown, this)
                }

            } else {
                if (CooldownScope().apply(block).shouldApply) {
                    CooldownManager.addCooldown(cooldown, this)
                }
            }
        } else {
            sendMessage("${Prefix}You are still on cooldown.")
        }
    }

    /**
     * @see [Player.applyCooldown]
     */
    inline fun PlayerEvent.applyCooldown(cooldown: Cooldown, block: CooldownScope.() -> Unit) =
        player.applyCooldown(cooldown, block)
}