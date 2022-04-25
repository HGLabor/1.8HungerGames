package de.hglabor.plugins.hungergames.game.mechanics.recraft

import net.axay.kspigot.event.listen
import org.bukkit.Material
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.inventory.ItemStack

object RecraftNerf {
    private const val RECRAFT_LIMIT = 128

    fun register() {
        listen<PlayerDropItemEvent> { e ->
            val recraftList = listOf(
                Recraft(Material.BROWN_MUSHROOM, Material.RED_MUSHROOM),
                Recraft(Material.COCOA),
                Recraft(Material.CACTUS)
            )

            val contents = e.player.inventory.contents
            contents.forEachIndexed { index, itemStack ->
                if (itemStack == null || !itemStack.isRecraftMaterial())
                    return@forEachIndexed

                val itemStackMaterial = if (itemStack.isCocoa()) Material.COCOA else itemStack.type
                for (recraft in recraftList) {
                    if (recraft.materials.contains(itemStackMaterial)) {
                        val recraftComponent = RecraftComponent(itemStackMaterial, itemStack.amount, index)
                        recraft.recraftComponents.add(recraftComponent)
                    }
                }
            }

            if (recraftList.sumOf { it.soups } <= RECRAFT_LIMIT)
                return@listen // no nerf needed

            val slotsNotToRemove = mutableListOf<Int>()

            var soupsSaved = 0
            while (soupsSaved < RECRAFT_LIMIT) {
                val eachRecraftsBiggestRecraftStacks = mutableListOf<List<RecraftComponent>>()
                for (recraft in recraftList) {
                    val biggestRecraftStacks = recraft.biggestRecraftStacks(slotsNotToRemove)
                    eachRecraftsBiggestRecraftStacks.add(biggestRecraftStacks)
                }

                val biggestRecraftStacks = eachRecraftsBiggestRecraftStacks.maxByOrNull { biggestRecraftStacks ->
                    if (biggestRecraftStacks.isEmpty()) 0
                    else biggestRecraftStacks.minOf { it.amount }
                } ?: continue

                val biggestRecraftStackSoups = biggestRecraftStacks.minOf { it.amount }
                if (soupsSaved + biggestRecraftStackSoups > RECRAFT_LIMIT) {
                    val soupsToDecrease = (soupsSaved + biggestRecraftStackSoups) - RECRAFT_LIMIT
                    val smallerBiggestRecraftStackSlot =
                        biggestRecraftStacks.minByOrNull { it.amount }?.slot ?: continue
                    val itemStack = contents[smallerBiggestRecraftStackSlot]

                    if (itemStack != null) {
                        itemStack.amount -= soupsToDecrease
                        e.player.inventory.setItem(smallerBiggestRecraftStackSlot, itemStack)
                    }

                    for (biggestRecraftStack in biggestRecraftStacks)
                        slotsNotToRemove.add(biggestRecraftStack.slot)
                    break
                }

                for (biggestRecraftStack in biggestRecraftStacks) {
                    slotsNotToRemove.add(biggestRecraftStack.slot)
                }

                soupsSaved += biggestRecraftStackSoups
            }

            val recraftSlots =
                recraftList.flatMap { recraft -> recraft.recraftComponents.map { it.slot } }.toMutableList()
            recraftSlots.removeAll(slotsNotToRemove)

            for (slot in recraftSlots) {
                val itemStack = e.player.inventory.contents[slot]
                itemStack.type = Material.AIR
                e.player.inventory.setItem(slot, itemStack)
            }
        }
    }

    data class RecraftComponent(val material: Material, val amount: Int, val slot: Int)

    class Recraft(vararg _material: Material) {
        val materials = _material.toList()
        val recraftComponents = mutableListOf<RecraftComponent>()
        val soups: Int
            get() {
                val componentAmounts = hashMapOf<Material, Int>()
                for (material in materials) {
                    val recraftComponentAmount = recraftComponentsByMaterial(material).sumOf { it.amount }
                    componentAmounts[material] = recraftComponentAmount
                }

                return componentAmounts.minOf { it.value }
            }

        fun biggestRecraftStacks(slotsToSkip: List<Int>): List<RecraftComponent> {
            val biggestRecraftComponents = mutableListOf<RecraftComponent>()
            for (material in materials) {
                val biggestRecraftComponent =
                    recraftComponentsByMaterial(material).filter { !slotsToSkip.contains(it.slot) }
                        .maxByOrNull { it.amount } ?: continue
                biggestRecraftComponents.add(biggestRecraftComponent)
            }
            return biggestRecraftComponents
        }

        private fun recraftComponentsByMaterial(material: Material) =
            recraftComponents.filter { it.material == material }
    }

    private val recraftMaterials = listOf(Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.CACTUS)
    private fun ItemStack.isCocoa() = this.type == Material.INK_SACK && this.data.data.toInt() == 3
    private fun ItemStack.isRecraftMaterial() = recraftMaterials.contains(this.type) || this.isCocoa()
}