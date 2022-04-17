package de.hglabor.plugins.hungergames.game.mechanics.recraft

import org.bukkit.Material
import java.util.*
import java.util.function.ToIntFunction
import java.util.function.BiFunction
import java.util.function.Function

class RecraftMaterial(getMaxSoupAmount: Int, vararg materials: Material) {
    val materials: MutableMap<Material?, Int>
    private val maxSoupAmount: Int

    init {
        this.materials = HashMap()
        maxSoupAmount = getMaxSoupAmount
        Arrays.stream(materials).forEach { material: Material? -> this.materials[material] = 0 }
    }

    val points: Float
        get() = materials.getOrDefault(getLowestMaterial(), 0).toFloat()

    fun decrease(material: Material?, amount: Int) {
        materials[material] = materials[material]!! - amount
    }

    fun getMaterials(): Set<Material?> {
        return materials.keys
    }

    operator fun get(material: Material?): Int {
        return getOrDefault(material, 0)
    }

    fun getOrDefault(material: Material?, fallback: Int): Int {
        return materials.getOrDefault(material, fallback)
    }

    fun put(material: Material?, zahl: Int) {
        materials[material] = zahl
    }

    fun getLowestMaterial(): Material? {
        if (materials.size > 1) {
            if (materials.values.stream().anyMatch { integer: Int -> integer == 0 }) {
                return null
            }
            val materialIntegerEntry: Optional<MutableMap.MutableEntry<Material?, Int>> = materials.entries.stream().min(
                Comparator.comparingInt<Map.Entry<Material?, Int>> { (_, value) -> value }
            )
            return materialIntegerEntry.map { (key, _) -> key }.orElse(null)
        } else {
            return materials.keys.stream().findFirst().orElse(null)
        }
    }

    val materialValue: Float
        get() = maxSoupAmount.toFloat() / materials.size

    fun reset() {
        materials.replaceAll { _, _ -> 0 }
    }
}