package de.hglabor.plugins.hungergames.utils

object ChanceUtils {
    fun roll(probability: Int) = (0..100).random() <= probability
}