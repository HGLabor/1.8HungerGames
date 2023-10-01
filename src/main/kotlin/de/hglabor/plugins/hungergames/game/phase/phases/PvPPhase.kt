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
import net.axay.kspigot.runnables.async
import net.axay.kspigot.runnables.sync
import org.bukkit.ChatColor
import org.bukkit.GameMode

object PvPPhase : IngamePhase(3600, EndPhase) {
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
        fun handleCombatTimer() {
            async {
                PlayerList.alivePlayers.filter { it.isInCombat }.forEach { alive ->
                    alive.combatTimer.decrementAndGet()
                }
            }
        }

        fun handleBorderShrink() {
            // Bordershrink - 20 min vor ende
            if (remainingTime.toInt() == 20 * 60) {
                broadcast("${Prefix}${ChatColor.WHITE}${ChatColor.BOLD}The border starts shrinking now.")
                GameManager.world.worldBorder.setSize(25.0 * 2, 10 * 60)
            }
        }

        fun handleFeast() {
            // Feast - nach 10 minuten announcen | 5 min später spawnt es
            if (tickCount == 600) {
                val world = GameManager.world

                GameManager.feast = Feast(world).apply {
                    feastCenter = LocationUtils.getHighestBlock(world, (world.worldBorder.size / 4).toInt(), 0)
                    spawn()
                }
            }
        }

        fun checkForWinner() {
            // Winner
            if (PlayerList.alivePlayers.size <= 1 && Arena.currentMatch == null && Arena.queuedPlayers.size < 2) {
                GameManager.startNextPhase()
            }
        }

        fun teleportAutisticSpectators() {
            async {
                if (tickCount % 2 != 0) return@async
                val worldBorder = GameManager.world.worldBorder
                val borderRadius = worldBorder.size / 2.0

                onlinePlayers.filter { it.gameMode == GameMode.SPECTATOR }.forEach { player ->
                    val playerLoc = player.location
                    if (playerLoc.x > borderRadius || playerLoc.x < -borderRadius ||
                        playerLoc.z > borderRadius || playerLoc.z < -borderRadius
                    ) {
                        sync {
                            player.teleport(GameManager.world.spawnLocation)
                        }
                    }
                }
            }
        }

        handleCombatTimer()
        handleBorderShrink()
        handleFeast()
        checkForWinner()
        teleportAutisticSpectators()

        super.tick(tickCount)
    }
}
