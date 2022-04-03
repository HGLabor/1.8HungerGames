package de.hglabor.plugins.hungergames.game.mechanics.recraft

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class Recraft {
    private val recraftMaterials = listOf(
        RecraftMaterial(1, Material.RED_MUSHROOM, Material.BROWN_MUSHROOM),
        RecraftMaterial(1, Material.COCOA),
        RecraftMaterial(1, Material.CACTUS)
    )

    fun calcRecraft(items: Array<ItemStack?>) {
        recraftMaterials.forEach { recraftMaterial -> recraftMaterial.reset() }
        for (item in items) {
            if (item == null) continue
            for (recraftMaterial in recraftMaterials) {
                val type = item.type
                if (recraftMaterial.materials.contains(type)) {
                    recraftMaterial.put(type, recraftMaterial.getOrDefault(type, 0) + item.amount)
                }
            }
        }
    }

    fun decrease(player: Player, amount: Int) {
        val lowestMaterials: MutableList<Material> = ArrayList()
        for (recraftMaterial in recraftMaterials) {
            if (recraftMaterial.getLowestMaterial() != null) {
                lowestMaterials.add(recraftMaterial.getLowestMaterial()!!)
            }
        }
        var highestMaterial: Material? = null
        var i = 0f
        for (lowestMaterial in lowestMaterials) {
            val recraftMaterial = byMaterial(lowestMaterial)
            if (recraftMaterial!![lowestMaterial] * recraftMaterial.materialValue > i) {
                i = recraftMaterial[lowestMaterial] * recraftMaterial.materialValue
                highestMaterial = lowestMaterial
            }
        }
        val recraftMaterial = byMaterial(highestMaterial!!)
        recraftMaterial!!.decrease(highestMaterial, amount)
        for (item in player.inventory.contents) {
            if (item == null) {
                continue
            }
            if (item.type == highestMaterial) {
                item.amount = item.amount - amount
                break
            }
        }
    }

    fun byMaterial(material: Material): RecraftMaterial? =
        recraftMaterials.firstOrNull { recraftMaterial -> recraftMaterial.materials.contains(material) }


    val recraftPoints: Float
        get() {
            var points = 0f
            for (recraftMaterial in recraftMaterials) {
                points += recraftMaterial.points
            }
            return points
        }
}