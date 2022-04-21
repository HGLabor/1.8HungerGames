@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package de.hglabor.plugins.kitapi.cooldown

import de.hglabor.plugins.kitapi.kit.KitBuilder
import de.hglabor.plugins.kitapi.kit.KitProperties
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerEvent

abstract class CooldownProperties(default: Long) : KitProperties() {
    val cooldown by long(default)

    @Suppress("LeakingThis")
    val cooldownInstance = Cooldown(this::cooldown)
}

inline fun <P : CooldownProperties> KitBuilder<P>.applyCooldown(player: Player, block: CooldownScope.() -> Unit) =
    player.applyCooldown(kit.properties.cooldownInstance, block)

inline fun <P : CooldownProperties> KitBuilder<P>.applyCooldown(event: PlayerEvent, block: CooldownScope.() -> Unit) =
    event.applyCooldown(kit.properties.cooldownInstance, block)

fun <P : CooldownProperties> KitBuilder<P>.hasCooldown(player: Player) = CooldownManager.hasCooldown(kit.properties.cooldownInstance, player)
