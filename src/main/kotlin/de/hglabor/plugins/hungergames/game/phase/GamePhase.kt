package de.hglabor.plugins.hungergames.game.phase

import de.hglabor.plugins.hungergames.game.GameManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.axay.kspigot.runnables.sync
import net.axay.kspigot.runnables.task
import org.bukkit.event.Listener

abstract class GamePhase(val maxDuration: Long, val nextPhase: GamePhase?): Listener {
    var elapsedTime: Long = 0

    open fun onStart() {}

    open fun tick() {}

    abstract fun getTimeString(): String

    fun start() {
        onStart()

        elapsedTime = 0
        runBlocking {
            launch {
                startTimer()
            }
        }
    }

    private suspend fun startTimer() {
        task(false, 20, 20, maxDuration, true, endCallback = {
            sync {
                GameManager.startNextPhase()
            }
        }) {
            if (elapsedTime == maxDuration) {
                it.cancel()
                return@task
            }
            incrementElapsedTime()
        }
    }

    open fun incrementElapsedTime() {
        elapsedTime++
    }
}