package de.hglabor.plugins.hungergames.game.mechanics.recraft

import net.axay.kspigot.event.listen
import org.bukkit.Material
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.inventory.ItemStack

object RecraftNerf {
    private const val RECRAFT_LIMIT = 64

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
                    if (recraft.materials.contains(itemStackMaterial))
                        recraft.recraftComponents.add(RecraftComponent(itemStackMaterial, itemStack.amount, index))
                }
            }

            if (recraftList.sumOf { it.soups } <= RECRAFT_LIMIT)
                return@listen

            val slotsToKeep = mutableListOf<Int>()

            var soupsSaved = 0
            while (soupsSaved < RECRAFT_LIMIT) {
                val biggestRecraftStacks = recraftList.map { it.biggestRecraftStack(slotsToKeep) }
                val biggestRecraftStack = biggestRecraftStacks.maxByOrNull { recraftStack ->
                    if (recraftStack.isEmpty()) 0
                    else recraftStack.minOf { it.amount }
                } ?: break

                val biggestRecraftStackSoups = biggestRecraftStack.minOf { it.amount }

                if (soupsSaved + biggestRecraftStackSoups > RECRAFT_LIMIT) {
                    val soupsToDecrease = (soupsSaved + biggestRecraftStackSoups) - RECRAFT_LIMIT
                    val smallerRecraftComponent = biggestRecraftStack.minByOrNull { it.amount } ?: break
                    val biggerRecraftComponent = biggestRecraftStack.maxByOrNull { it.amount } ?: break

                    val smallerItemStack = contents[smallerRecraftComponent.slot]
                    val biggerItemStack = contents[biggerRecraftComponent.slot]

                    // der größere stack ist quasi mit (sich - kleineren) soups durchgekommen

                    if (smallerItemStack != null && biggerItemStack != null) {
                        smallerItemStack.amount -= soupsToDecrease
                        e.player.inventory.setItem(smallerRecraftComponent.slot, smallerItemStack)
                        biggerItemStack.amount = smallerItemStack.amount
                        e.player.inventory.setItem(biggerRecraftComponent.slot, biggerItemStack)
                    }

                    for (recraftStack in biggestRecraftStack)
                        slotsToKeep.add(recraftStack.slot)
                    break
                }

                for (recraftStack in biggestRecraftStack)
                    slotsToKeep.add(recraftStack.slot)
                soupsSaved += biggestRecraftStackSoups
            }

            val recraftSlots =
                recraftList.flatMap { recraft -> recraft.recraftComponents.map { it.slot } }
                    .filterNot { slotsToKeep.contains(it) }

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
            get() = materials.map { material -> recraftComponentsByMaterial(material).sumOf { it.amount } }.minOf { it }

        fun biggestRecraftStack(slotsToSkip: List<Int>): List<RecraftComponent> {
            val biggestRecraftComponents = mutableListOf<RecraftComponent>()
            for (material in materials) {
                val biggestRecraftComponent =
                    recraftComponentsByMaterial(material)
                        .filterNot { slotsToSkip.contains(it.slot) }
                        .maxByOrNull { it.amount } ?: break
                biggestRecraftComponents.add(biggestRecraftComponent)
            }
            return biggestRecraftComponents
        }

        private fun recraftComponentsByMaterial(material: Material) =
            recraftComponents.filter { it.material == material }
    }

    private val recraftMaterials =
        listOf(Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.CACTUS, Material.COCOA)

    private fun ItemStack.isCocoa() = this.type == Material.INK_SACK && this.data.data.toInt() == 3
    private fun ItemStack.isRecraftMaterial() = recraftMaterials.contains(this.type) || this.isCocoa()
}