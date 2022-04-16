package de.hglabor.plugins.hungergames.game.phase.phases

import com.mongodb.client.AggregateIterable
import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.agnikai.Agnikai
import de.hglabor.plugins.hungergames.game.mechanics.KitSelector
import de.hglabor.plugins.hungergames.game.mechanics.feast.Feast
import de.hglabor.plugins.hungergames.game.mechanics.recraft.RecraftInspector
import de.hglabor.plugins.hungergames.game.phase.IngamePhase
import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.hungergames.utils.LocationUtils
import de.hglabor.plugins.hungergames.utils.TimeConverter
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.extensions.onlinePlayers
import org.bukkit.ChatColor


object PvPPhase : IngamePhase(1800, EndPhase) {
    //private val recraftInspector: RecraftInspector = RecraftInspector()
    override val timeName = "Time"
    override fun getTimeString() = TimeConverter.stringify((GameManager.elapsedTime.get()).toInt())

    override fun onStart() {
        onlinePlayers.forEach { player ->
            player.inventory.remove(KitSelector.kitSelectorItem)
        }
    }

    override fun tick(tickCount: Int) {
        // recraft nerf
        // if (tickCount % 5 == 0) recraftInspector.tick()

        // Combat Timer
        PlayerList.alivePlayers.filter { it.isInCombat }.forEach { alive ->
            alive.combatTimer.decrementAndGet()
        }

        // Bordershrink
        if ((maxDuration - GameManager.elapsedTime.get()).toInt() == 10 * 60) {
            broadcast("${Prefix}${ChatColor.WHITE}${ChatColor.BOLD}The border starts shrinking now.")
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

        // Winner
        if (PlayerList.alivePlayers.size <= 1 && Agnikai.currentlyFighting.isEmpty() && Agnikai.queuedPlayers.size < 2) {
            GameManager.startNextPhase()
        }
    }
}
