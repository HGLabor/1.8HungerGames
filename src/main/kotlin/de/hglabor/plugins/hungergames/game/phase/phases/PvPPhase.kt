package de.hglabor.plugins.hungergames.game.phase.phases

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.mechanics.feast.Feast
import de.hglabor.plugins.hungergames.game.mechanics.recraft.RecraftInspector
import de.hglabor.plugins.hungergames.game.phase.IngamePhase
import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.hungergames.utils.LocationUtils
import de.hglabor.plugins.hungergames.utils.TimeConverter
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.extensions.broadcast
import org.bukkit.ChatColor


object PvPPhase : IngamePhase(1800, EndPhase) {
    private val recraftInspector: RecraftInspector = RecraftInspector()
    override val timeName = "Zeit"
    override fun getTimeString() = TimeConverter.stringify((GameManager.elapsedTime.get()).toInt())

    override fun tick(tickCount: Int) {
        // recraft nerf
        if (tickCount % 5 == 0) recraftInspector.tick()

        // Bordershrink
        if ((maxDuration - GameManager.elapsedTime.get()).toInt() == 10 * 60) {
            broadcast("Border starts shrinking")
            GameManager.world.worldBorder.setSize(25.0 * 2, 10 * 60)
        }

        // Feast
        if (tickCount == 600) {
            val world = GameManager.world

            GameManager.feast = Feast(world).apply {
                feastCenter = LocationUtils.getHighestBlock(world, (world.worldBorder.size / 4).toInt(), 0)
            }
            GameManager.feast?.spawn()
        }

        when (remainingTime.toInt()) {
            60, 30, 20, 10, 3, 2, 1 -> broadcast("${Prefix}Der Spieler mit den meisten Eliminierungen gewinnt in ${KColors.WHITE}${LobbyPhase.getTimeString()}${ChatColor.GRAY}.")
        }
        // Winner
        if (PlayerList.alivePlayers.size == 1) {
            GameManager.startNextPhase()
        }
    }
}