package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.kitapi.cooldown.CooldownProperties
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.extensions.worlds
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import net.axay.kspigot.utils.OnlinePlayerMap
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.player.PlayerBedLeaveEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.max

class RelaxoCooldownProperties : KitProperties()


val Relaxo = Kit("Relaxo", ::RelaxoCooldownProperties) {
    displayMaterial = Material.LAPIS_ORE
    simpleItem(ItemStack(Material.BED, 1))
    val players = OnlinePlayerMap<KSpigotRunnable?>()

    var maxTime = 120

    kitPlayerEvent<PlayerBedEnterEvent> ({ it.player}, EventPriority.HIGH) { it, player ->
        if(player.location.world.time in 1..12299) {
            player.sendMessage("${Prefix}You mustn't use your kit during the day.")
            it.isCancelled = true
            return@kitPlayerEvent
        }
        players[player] = task(false, 20, 20) {
            broadcast("task")
            if (--maxTime == 0) {
                player.damage(1.0)
                player.isHealthScaled = true
                player.healthScale = ((120-maxTime) / 2.0 + 1.0)+ 20.0
                player.health = ((120-maxTime) / 2.0 + 1.0)+ 20.0
                it.cancel()
            }
        }
    }

    kitPlayerEvent<PlayerBedLeaveEvent> ({ it.player}, EventPriority.HIGH) { it, player ->
        players[player]?.cancel()
        player.isHealthScaled = true
        player.healthScale = ((120-maxTime) / 2.0 + 1.0)+ 20.0
        player.health = ((120-maxTime) / 2.0 + 1.0)+ 20.0
        broadcast("CANCELED")
    }

}

