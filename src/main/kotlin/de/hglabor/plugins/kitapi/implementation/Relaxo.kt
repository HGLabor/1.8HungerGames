package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.heal
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.player.PlayerBedLeaveEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class RelaxoCooldownProperties : KitProperties()


val Relaxo = Kit("Relaxo", ::RelaxoCooldownProperties) {
    val tasks = mutableMapOf<UUID, KSpigotRunnable?>()
    val maxTime = mutableMapOf<UUID, AtomicInteger>()

    displayMaterial = Material.BED

    placeableItem(ItemStack(Material.BED, 5)) {}

    kitPlayerEvent<PlayerBedEnterEvent> ({ it.player}) { it, player ->
        if(player.location.world.time in 1..12299) {
            player.sendMessage("${Prefix}You mustn't use your kit during the day.")
            it.isCancelled = true
            return@kitPlayerEvent
        }
        maxTime[player.uniqueId] = AtomicInteger(120)
        tasks[player.uniqueId] = task(false, 20, 20) {
            val time = maxTime[player.uniqueId]
            if (time == null) {
                it.cancel()
                return@task
            }

            if (time.getAndDecrement() == 0) {
                player.damage(1.0)
                player.isHealthScaled = true
                player.healthScale = ((120-time.get()) / 2.0 + 1.0)+ 20.0
                player.heal()
                it.cancel()
            }
        }
    }

    kitPlayerEvent<PlayerBedLeaveEvent> ({ it.player}) { it, player ->
        val time = maxTime[player.uniqueId]?.get() ?: return@kitPlayerEvent
        tasks[player.uniqueId]?.cancel()
        tasks[player.uniqueId] = null
        player.isHealthScaled = true
        player.healthScale = ((120-time) / 2.0 + 1.0)+ 20.0
        player.health = ((120-time) / 2.0 + 1.0)+ 20.0
    }

    kitPlayerEvent<EntityDamageEvent> ({ it.entity as? Player }, EventPriority.HIGH) { it, player ->
        if (player.isSleeping) it.isCancelled = true
    }

    listen<PlayerInteractEvent> {
        if (it.player.hgPlayer.kit == kit) return@listen
        if (it.action != Action.LEFT_CLICK_BLOCK) return@listen
        if (!it.hasBlock() || it.clickedBlock == null || it.clickedBlock.type != Material.BED_BLOCK) return@listen
        it.player.addPotionEffect(PotionEffect(PotionEffectType.SLOW_DIGGING, 100, 2))
    }
}

