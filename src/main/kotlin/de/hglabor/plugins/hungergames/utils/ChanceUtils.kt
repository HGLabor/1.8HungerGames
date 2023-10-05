package de.hglabor.plugins.hungergames.utils

object ChanceUtils {
    fun roll(likelihood: Int) = (0..100).random() <= likelihood
}