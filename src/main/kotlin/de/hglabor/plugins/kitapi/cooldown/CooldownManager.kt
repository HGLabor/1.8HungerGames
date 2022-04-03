package de.hglabor.plugins.kitapi.cooldown

import de.hglabor.plugins.hungergames.Prefix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.entity.Player
import java.util.*

object CooldownManager {
    private val cooldownCoroutineScope = CoroutineScope(Dispatchers.IO)

    private val cooldownMap = HashMap<Cooldown, MutableSet<UUID>>()

    fun addCooldown(cooldown: Cooldown, player: Player) {
        val uuid = player.uniqueId
        val cooldownCollection = cooldownMap.getOrPut(cooldown) {
            Collections.synchronizedSet(HashSet())
        }
        cooldownCollection += uuid
        cooldownCoroutineScope.launch {
            delay(cooldown.property.get())
            cooldownCollection -= uuid
            player.sendMessage("${Prefix}Your cooldown has expired.")
        }
    }

    fun hasCooldown(cooldown: Cooldown, player: Player) =
        cooldownMap[cooldown]?.contains(player.uniqueId) == true
}
