package de.hglabor.plugins.kitapi.cooldown

import org.bukkit.entity.Player
import org.litote.kmongo.util.idValue
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

abstract class MultipleUsesCooldownProperties(usesDefault: Int, cooldownDefault: Long) : CooldownProperties(cooldownDefault) {
    val uses by int(usesDefault)
    val usesMap: MutableMap<UUID, AtomicInteger> = mutableMapOf()

    fun getUses(player: Player) = usesMap[player.uniqueId]?.get() ?: -1
    fun hasUses(player: Player) = getUses(player) > 0
    fun decrementUses(player: Player) = usesMap[player.uniqueId]?.getAndDecrement()
}