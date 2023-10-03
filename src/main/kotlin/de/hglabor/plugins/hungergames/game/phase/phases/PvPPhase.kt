package de.hglabor.plugins.hungergames.game.phase.phases

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.SecondaryColor
import de.hglabor.plugins.hungergames.commands.BanSpecsCommand
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.mechanics.feast.Feast
import de.hglabor.plugins.hungergames.game.mechanics.implementation.KitSelector
import de.hglabor.plugins.hungergames.game.mechanics.implementation.arena.Arena
import de.hglabor.plugins.hungergames.game.phase.IngamePhase
import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.hungergames.player.PlayerStatus
import de.hglabor.plugins.hungergames.player.StaffPlayer
import de.hglabor.plugins.hungergames.utils.LocationUtils
import de.hglabor.plugins.hungergames.utils.TimeConverter
import de.hglabor.plugins.kitapi.implementation.None
import de.hglabor.plugins.kitapi.kit.KitManager
import de.hglabor.plugins.kitapi.player.PlayerKits.chooseKit
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.extensions.onlinePlayers
import net.axay.kspigot.runnables.async
import net.axay.kspigot.runnables.sync
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerPreLoginEvent

object PvPPhase : IngamePhase(3600, EndPhase) {
    override val timeName = "Time"
    override fun getTimeString() = TimeConverter.stringify((GameManager.elapsedTime.get()).toInt())

    override fun onStart() {
        PlayerList.alivePlayers.forEach { hgPlayer ->
            val player = hgPlayer.bukkitPlayer
            player?.inventory?.remove(KitSelector.kitSelectorItem)
            if (hgPlayer.kit == None && !hgPlayer.changedKitBefore) {
                val kit = KitManager.kits.random()
                player?.chooseKit(kit, false)
                player?.sendMessage("${Prefix}You have been given the kit $SecondaryColor${kit.properties.kitname}${ChatColor.GRAY}.")
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

        fun kickSpectatorsIfBanned() {
            if (tickCount % 5 != 0) return
            if (BanSpecsCommand.allowSpecs) return
            PlayerList.spectatingPlayers.mapNotNull {
                if (it is StaffPlayer) null
                else it.bukkitPlayer
            }.forEach {
                it.kickPlayer("Sorry, you can't spectate anymore.")
            }
        }

        handleCombatTimer()
        handleBorderShrink()
        handleFeast()
        checkForWinner()
        teleportAutisticSpectators()
        kickSpectatorsIfBanned()

        super.tick(tickCount)
    }

    @EventHandler
    fun onPlayerPreLogin(event: PlayerPreLoginEvent) {
        if (BanSpecsCommand.allowSpecs) return
        if (Bukkit.getOfflinePlayer(event.uniqueId).isOp) return
        val hgPlayer = PlayerList.getPlayer(event.uniqueId)
        if (hgPlayer is StaffPlayer) return
        if (hgPlayer == null || hgPlayer.status == PlayerStatus.ELIMINATED || hgPlayer.status == PlayerStatus.SPECTATOR) {
            event.disallow(PlayerPreLoginEvent.Result.KICK_WHITELIST, "Sorry, you can't spectate this game.")
        }
    }
}
