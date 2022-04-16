package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import java.util.concurrent.atomic.AtomicInteger


class LumberjackProperties : KitProperties() {
    val maxBlocks by int(300)
}

val Lumberjack = Kit("Lumberjack", ::LumberjackProperties) {
    displayMaterial = Material.LOG

    simpleItem(ItemStack(Material.WOOD_AXE))

    fun isWood(block: Block) =
        block.type == Material.LOG || block.type == Material.LOG_2 || block.type == Material.HUGE_MUSHROOM_1 || block.type == Material.HUGE_MUSHROOM_2


    fun breakSurroundingWood(block: Block, atomicInteger: AtomicInteger) {
        if (isWood(block)) {
            block.breakNaturally()
            if (atomicInteger.getAndIncrement() > kit.properties.maxBlocks) return
            val faces = arrayOf(BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)
            for (face in faces) {
                breakSurroundingWood(block.getRelative(face), atomicInteger)
            }
        }
    }

    kitPlayerEvent<BlockBreakEvent>({ it.player }) { it, player ->
        if (isWood(it.block)) {
            breakSurroundingWood(it.block, AtomicInteger(0))
        }
    }
}
