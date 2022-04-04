package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.Prefix
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
import java.util.concurrent.atomic.AtomicInteger

class PhantomProperties : CooldownProperties(30000) {
    val flightTime by int(5)
}

val Phantom = Kit("Phantom", ::PhantomProperties) {

    displayMaterial = Material.FEATHER

    clickableItem(itemStack(Material.FEATHER) { meta { name = "${ChatColor.LIGHT_PURPLE}Phantom" } }) {
        applyCooldown(it) {
            it.player.apply {
                allowFlight = true
                isFlying = true

                val timer = AtomicInteger(kit.properties.flightTime)
                task(false, 20, 20) { task ->
                    val t = timer.getAndDecrement()
                    if (t == 0) {
                        task.cancel()
                        allowFlight = false
                        isFlying = false
                        player.sendMessage("${Prefix}You are no longer able to fly.")
                        return@task
                    }
                    player.sendMessage("${Prefix}Your flight has ${ChatColor.LIGHT_PURPLE}$t ${ChatColor.GRAY}seconds remaining.")
                }
            }
        }
    }
}
