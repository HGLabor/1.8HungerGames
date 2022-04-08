package de.hglabor.plugins.hungergames.game.phase

import de.hglabor.plugins.hungergames.game.GameManager
import org.bukkit.event.Listener

abstract class GamePhase(val maxDuration: Long, val nextPhase: GamePhase?) : Listener {
    var tickCount: Int = 0
    val remainingTime get() = maxDuration - GameManager.elapsedTime.get()

    open fun onStart() {}

    open fun tick(tickCount: Int) {}

    abstract val timeName: String
    abstract fun getTimeString(): String

    fun start() {
        onStart()
    }

    open fun incrementElapsedTime() {
        GameManager.elapsedTime.incrementAndGet()
    }
}