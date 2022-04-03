package de.hglabor.plugins.hungergames.game.phase.phases

import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.mechanics.feast.Feast
import de.hglabor.plugins.hungergames.game.mechanics.recraft.RecraftInspector
import de.hglabor.plugins.hungergames.game.phase.IngamePhase
import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.hungergames.utils.LocationUtils
import de.hglabor.plugins.hungergames.utils.TimeConverter
import net.axay.kspigot.extensions.broadcast
import org.bukkit.ChatColor
import org.bukkit.Material


object PvPPhase : IngamePhase(1800, EndPhase) {
    private val recraftInspector: RecraftInspector = RecraftInspector()
    override val timeName = "Ingame${ChatColor.DARK_GRAY}"
    override fun getTimeString() = TimeConverter.stringify((GameManager.elapsedTime.get()).toInt())

    override fun tick(tickCount: Int) {
        // recraft nerf
        if (tickCount % 5 == 0) recraftInspector.tick()

        // Bordershrink
        if ((maxDuration - GameManager.elapsedTime.get()).toInt() == 10*60) {
            broadcast("Border starts shrinking")
            GameManager.world.worldBorder.setSize(25.0*2, 10*60)
        }

        // Feast
        if (GameManager.elapsedTime.get() == 600.toLong()) {
            val world = GameManager.world

            GameManager.feast = Feast(world)
                .center(LocationUtils.getHighestBlock(world, (world.worldBorder.size / 4).toInt(), 0))
                .radius(20)
                .timer(300)
                .material(Material.GRASS)
            GameManager.feast?.spawn()
        }

        // Winner
        if (PlayerList.alivePlayers.size == 1) {
            GameManager.startNextPhase()
        }
    }
}