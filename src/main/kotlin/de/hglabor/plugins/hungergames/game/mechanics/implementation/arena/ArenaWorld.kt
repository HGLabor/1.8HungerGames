package de.hglabor.plugins.hungergames.game.mechanics.implementation.arena

import net.axay.kspigot.event.listen
import org.bukkit.Bukkit
import org.bukkit.Difficulty
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.LivingEntity
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.player.PlayerDropItemEvent

object ArenaWorld {
    val world: World = Bukkit.getWorld("arena")
    val queueLocation = Location(world, 247.5, 76.5, 281.5, 90f, 0F)
    val spawn1Location = Location(world, 221.5, 75.5, 261.5, 0f, 0F)
    val spawn2Location = Location(world, 221.5, 75.5, 301.5, -180f, 0F)

    init {
        world.difficulty = Difficulty.NORMAL

        listen<BlockBreakEvent> {
            if (it.block.world != Bukkit.getWorld("arena")) return@listen
            it.isCancelled = true
        }

        listen<BlockPlaceEvent> {
            if (it.block.world != Bukkit.getWorld("arena")) return@listen
            it.isCancelled = true
        }

        listen<EntitySpawnEvent> {
            if (it.entity !is LivingEntity) return@listen
            if (it.entity.world == world) {
                it.isCancelled = true
            }
        }

        listen<PlayerDropItemEvent> {
            if (it.player.world == world) {
                it.isCancelled = true
            }
        }

        listen<ItemSpawnEvent> {
            if (it.entity.world == world) {
                it.isCancelled = true
            }
        }

        listen<FoodLevelChangeEvent> {
            if (it.entity !is LivingEntity) return@listen
            if (it.entity.world == world) {
                it.isCancelled = true
            }
        }
    }
}