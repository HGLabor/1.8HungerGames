package de.hglabor.plugins.hungergames.utils

import java.util.*

class RandomCollection<E> {
    private val map: NavigableMap<Double, E> = TreeMap()
    private val names: MutableMap<E, String> = HashMap()
    private val random = Random()
    private var total = 0.0
    fun add(weight: Double, result: E) {
        if (weight <= 0) return
        total += weight
        map[total] = result
    }

    fun add(name: String, weight: Double, result: E) {
        if (weight <= 0) return
        total += weight
        map[total] = result
        names[result] = name
    }

    fun getName(key: E): String {
        return names.getOrDefault(key, "")
    }

    fun getRandom(): E {
        val value = random.nextDouble() * total
        return map.higherEntry(value).value
    }
}