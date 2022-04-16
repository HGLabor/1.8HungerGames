package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.event.KitEnableEvent
import de.hglabor.plugins.hungergames.event.PlayerKilledEntityEvent
import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.hungergames.utils.cancelFalldamage
import de.hglabor.plugins.kitapi.cooldown.MultipleUsesCooldownProperties
import de.hglabor.plugins.kitapi.cooldown.applyCooldown
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.player.PlayerKits.hasKit
import net.axay.kspigot.event.listen
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import net.axay.kspigot.runnables.taskRunLater
import net.axay.kspigot.utils.OnlinePlayerMap
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector


class SpongeProperties : MultipleUsesCooldownProperties(15, 30000) {
    val waterRemoveDelay by long(50)
    val poisonDuration by int(60)
    val poisonAmplifier by int(60)
    val spongesOnKill by int(2)
    val spongesOnStart by int(12)
    val spongeBoost by double(1.0)
}

val Sponge = Kit("Sponge", ::SpongeProperties) {
    displayMaterial = Material.SPONGE

    val waterBlocks = ArrayList<Block>()
    val waterTasks = OnlinePlayerMap<KSpigotRunnable?>()

    listen<BlockFromToEvent> {
        if (it.block.type == Material.WATER || it.block.type == Material.STATIONARY_WATER) {
            if (waterBlocks.contains(it.block)) {
                it.isCancelled = true
            }
        }
    }

    fun Player.isInSpongeWater(): Boolean {
        if (player.hasKit(kit)) return false
        val feetBlock = player.location.block
        val headBlock = player.location.block.getRelative(BlockFace.UP)
        if (feetBlock in waterBlocks) return true
        if (headBlock in waterBlocks) return true
        return false
    }

    fun Player.getSpongeBoost(): Double {
        val loc = location.clone()
        var boost = 0.0
        for (i in 0..4) {
            if (loc.clone().subtract(0.0, i.toDouble(), 0.0).block.type == Material.SPONGE) {
                boost += kit.properties.spongeBoost
            }
        }
        return boost
    }

    listen<PlayerMoveEvent> {
        val player = it.player
        if (!player.hgPlayer.isAlive) return@listen
        // poison players in spongewater
        if (player.isInSpongeWater()) {
            val properties = kit.properties
            waterTasks[player] = task(true, 0, properties.poisonDuration - 5L) { task ->
                if (!player.isInSpongeWater()) {
                    task.cancel()
                    waterTasks[player] = null
                    return@task
                }
                player.addPotionEffect(
                    PotionEffect(
                        PotionEffectType.POISON,
                        properties.poisonDuration,
                        properties.poisonAmplifier
                    )
                )
            }
        }

        // Sponge-block boost (launcher)
        if (player.location.block.getRelative(BlockFace.DOWN).type == Material.SPONGE) {
            player.velocity = Vector(0.0, player.getSpongeBoost(), 0.0)
            player.cancelFalldamage(100, true)
        }
    }

    kitPlayerEvent<PlayerInteractEvent> {
        if (it.action != Action.RIGHT_CLICK_BLOCK) return@kitPlayerEvent
        if (it.player.itemInHand != null && it.player.itemInHand.type != Material.AIR) return@kitPlayerEvent

        applyCooldown(it) {
            val blockToReplace = it.clickedBlock.getRelative(BlockFace.UP)
            if (!blockToReplace.type.isSolid) {
                blockToReplace.setType(Material.STATIONARY_WATER, false)
                waterBlocks.add(blockToReplace)

                taskRunLater(kit.properties.waterRemoveDelay) {
                    blockToReplace.setType(Material.AIR, false)
                    waterBlocks.remove(blockToReplace)
                }
            }
        }
    }

    kitPlayerEvent<KitEnableEvent> {
        if (it.player.inventory.contains(Material.SPONGE)) return@kitPlayerEvent
        it.player.inventory.addItem(ItemStack(Material.SPONGE, kit.properties.spongesOnStart))
    }

    kitPlayerEvent<PlayerKilledEntityEvent>({ it.killer }) { event, player ->
        player.inventory.addItem(ItemStack(Material.SPONGE, kit.properties.spongesOnKill))
    }
}