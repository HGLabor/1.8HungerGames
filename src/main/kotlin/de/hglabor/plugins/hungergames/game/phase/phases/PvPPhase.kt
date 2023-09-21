package de.hglabor.plugins.hungergames.game.phase.phases

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.SecondaryColor
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.mechanics.implementation.arena.Arena
import de.hglabor.plugins.hungergames.game.mechanics.implementation.KitSelector
import de.hglabor.plugins.hungergames.game.mechanics.feast.Feast
import de.hglabor.plugins.hungergames.game.phase.IngamePhase
import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.hungergames.utils.LocationUtils
import de.hglabor.plugins.hungergames.utils.TimeConverter
import de.hglabor.plugins.kitapi.implementation.None
import de.hglabor.plugins.kitapi.kit.KitManager
import de.hglabor.plugins.kitapi.player.PlayerKits.chooseKit
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.extensions.onlinePlayers
import org.bukkit.ChatColor

object PvPPhase : IngamePhase(1800, EndPhase) {
    override val timeName = "Time"
    override fun getTimeString() = TimeConverter.stringify((GameManager.elapsedTime.get()).toInt())

    override fun onStart() {
        onlinePlayers.forEach { player ->
            player.inventory.remove(KitSelector.kitSelectorItem)
            if (player.hgPlayer.kit == None.value && !player.hgPlayer.changedKitBefore) {
                player.chooseKit(KitManager.kits.random(), false)
                player.sendMessage("${Prefix}You have been given the kit $SecondaryColor${player.hgPlayer.kit.properties.kitname}${ChatColor.GRAY}.")
            }
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
        if (PlayerList.alivePlayers.size <= 1 && Arena.currentMatch == null && Arena.queuedPlayers.size < 2) {
            GameManager.startNextPhase()
        }
    }
}
