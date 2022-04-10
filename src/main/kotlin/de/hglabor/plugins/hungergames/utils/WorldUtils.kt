package de.hglabor.plugins.hungergames.utils

import de.hglabor.plugins.hungergames.utils.WorldUtils.setBlockInstantly
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.sync
import net.axay.kspigot.runnables.task
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block

object WorldUtils {
    private val defaultQueue = BlockQueue()

    fun setBlockInstantly(block: Block, material: Material, data: Byte = 0) {
        block.setTypeIdAndData(material.id, data, true)
    }

    fun setBlockInstantly(location: Location, material: Material, data: Byte = 0) {
        setBlockInstantly(location.block, material, data)
    }

    fun setBlock(block: Block, material: Material, data: Byte = 0, queue: BlockQueue = defaultQueue) {
        setBlock(block.location, material, data, queue)
    }

    fun setBlock(location: Location, material: Material, data: Byte, queue: BlockQueue = defaultQueue) {
        val queuedBlock = queue.queuedBlocks[location]
        val shouldntPlace = (queuedBlock != null && queuedBlock.first == material && queuedBlock.second == data) ||
                (queuedBlock?.first == Material.AIR && material == Material.AIR)

        if (!shouldntPlace) {
            queue.queuedBlocks[location] = material to data
        }
        queue.startPlacingBlocksInQueue()
    }

    fun makeCircle(loc: Location, r: Int, h: Int, hollow: Boolean, sphere: Boolean): HashSet<Location> {
        val surroundingBlocks = HashSet<Location>()
        val cx = loc.blockX
        val cy = loc.blockY
        val cz = loc.blockZ
        for (x in cx - r..cx + r) {
            for (z in cz - r..cz + r) {
                for (y in (if (sphere) cy - r else cy) until if (sphere) cy + r else cy + h) {
                    val dist =
                        ((cx - x) * (cx - x) + (cz - z) * (cz - z) + if (sphere) (cy - y) * (cy - y) else 0).toDouble()
                    if (dist < r * r && !(hollow && dist < (r - 1) * (r - 1))) {
                        surroundingBlocks.add(loc.world.getBlockAt(x, y, z).location)
                    }
                }
            }
        }
        return surroundingBlocks
    }
}

class BlockQueue {
    var queueTask: KSpigotRunnable? = null
    val queuedBlocks: MutableMap<Location, Pair<Material, Byte>> = mutableMapOf()

    fun startPlacingBlocksInQueue() {
        if (queueTask != null) return

        queueTask = task(false, 2, 2) {
            if (queuedBlocks.isEmpty()) {
                it.cancel()
                queueTask = null
                return@task
            }

            queuedBlocks.toList().take(75).forEach { (loc, pair) ->
                val (material, data) = pair
                sync {
                    val block = loc.block
                    if (block.type != material || block.data != data)
                        setBlockInstantly(loc, material, data)
                    queuedBlocks -= loc
                }
            }
        }
    }
}