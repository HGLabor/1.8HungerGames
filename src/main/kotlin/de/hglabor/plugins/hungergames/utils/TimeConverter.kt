package de.hglabor.plugins.hungergames.utils

object TimeConverter {
    fun stringify(totalSecs: Int): String {
        if (totalSecs > 3600) {
            return stringify(totalSecs, "%02d:%02d:%02d")
        }
        val minutes = totalSecs % 3600 / 60
        val seconds = totalSecs % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun stringify(totalSecs: Int, format: String?): String {
        val hours = totalSecs / 3600
        val minutes = totalSecs % 3600 / 60
        val seconds = totalSecs % 60
        return String.format(format!!, hours, minutes, seconds)
    }
}