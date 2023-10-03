package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.SecondaryColor
import de.hglabor.plugins.hungergames.utils.cancelFalldamage
import de.hglabor.plugins.kitapi.cooldown.CooldownProperties
import de.hglabor.plugins.kitapi.cooldown.applyCooldown
import de.hglabor.plugins.kitapi.kit.Kit
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import net.axay.kspigot.runnables.task
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.util.Vector
import java.util.concurrent.atomic.AtomicInteger

class PhantomProperties : CooldownProperties(30000) {
    val flightTime by int(5)
}

val Phantom by Kit("Phantom", ::PhantomProperties) {
    displayMaterial = Material.FEATHER
    description = "${ChatColor.WHITE}Right-click ${ChatColor.GRAY}your kit-item to fly for ${kit.properties.flightTime} seconds"

    clickableItem(itemStack(Material.FEATHER) { meta { name = "${SecondaryColor}Phantom" } }) {
        applyCooldown(it) {
            it.player.apply {
                allowFlight = true
                isFlying = true
                player.sendMessage("${Prefix}You are now able to fly.")
                player.velocity = Vector(0.0, 0.3, 0.0)
                broadcast("${ChatColor.BOLD}A ${SecondaryColor}${ChatColor.BOLD}Phantom ${ChatColor.WHITE}${ChatColor.BOLD}has risen!")

                val timer = AtomicInteger(kit.properties.flightTime)
                task(true, 20, 20) { task ->
                    val timeRemaining = timer.getAndDecrement()
                    if (timeRemaining == 0) {
                        task.cancel()
                        cancelFalldamage(100, true)
                        allowFlight = false
                        isFlying = false
                        player.sendMessage("${Prefix}You are no longer able to fly.")
                        return@task
                    }
                    player.sendMessage("${Prefix}Your flight has ${SecondaryColor}$timeRemaining ${ChatColor.GRAY}seconds remaining.")
                }
            }
        }
    }
}
