package de.hglabor.plugins.kitapi.cooldown

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.player.hgPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.entity.Player
import java.util.*

object CooldownManager {
    private val cooldownCoroutineScope = CoroutineScope(Dispatchers.IO)

    private val cooldownMap = HashMap<Cooldown, MutableMap<UUID, Long>>()

    fun addCooldown(cooldown: Cooldown, player: Player) {
        val uuid = player.uniqueId
        val cooldownCollection = cooldownMap.getOrPut(cooldown) {
            Collections.synchronizedMap(HashMap())
        }
        cooldownCollection += uuid to System.currentTimeMillis() + cooldown.property.get() * 1000
        cooldownCoroutineScope.launch {
            delay(cooldown.property.get() * 1000)
            cooldownCollection -= uuid

            // Reset uses
            val properties = player.hgPlayer.kit.properties
            if (properties is MultipleUsesCooldownProperties) {
                properties.usesMap[player.uniqueId]?.set(properties.uses)
            }

            player.sendMessage("${Prefix}Your cooldown has expired.")
        }
    }

    fun hasCooldown(cooldown: Cooldown, player: Player) =
        cooldownMap[cooldown]?.contains(player.uniqueId) == true

    fun getRemainingCooldown(cooldown: Cooldown, player: Player): String {
        if (!hasCooldown(cooldown, player)) return "Ready"
        val restMillis = (cooldownMap[cooldown]?.get(player.uniqueId) ?: 0) - System.currentTimeMillis()
        if (restMillis <= 0) return "Ready"
        val seconds = (restMillis/1000).toInt()
        // can't get the scoreboard to update more frequent than once ~a second :<
        /*val miliseconds = (restMillis%1000).toInt() / 100
        return  String.format("%02d:%02d", seconds, miliseconds)*/
        return  String.format("%02ds", seconds)
    }
}
