package de.hglabor.plugins.hungergames.game.phase.phases

import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.mechanics.feast.Feast
import de.hglabor.plugins.hungergames.game.phase.IngamePhase
import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.hungergames.utils.LocationUtils
import net.axay.kspigot.extensions.broadcast
import org.bukkit.Material

object PvPPhase : IngamePhase(1800, EndPhase) {
    override fun onStart() {
        broadcast("PvPPhase")
    }

    override fun tick(tickCount: Int) {
        if (GameManager.elapsedTime.get() == 30.toLong()) {
            val world = GameManager.world

            GameManager.feast = Feast(world)
                .center(LocationUtils.getHighestBlock(world, (world.worldBorder.size / 4).toInt(), 0))
                .radius(20)
                .timer(30)
                .material(Material.GRASS)
            GameManager.feast?.spawn()
        }

        if (PlayerList.alivePlayers.size == 1) {
            GameManager.startNextPhase()
        }
    }

    override fun getTimeString(): String {
        //return "Ingame: $elapsedTime"
        return "PvP: ${maxDuration - GameManager.elapsedTime.get()}"
    }
}