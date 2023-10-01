package de.hglabor.plugins.hungergames.game.phase

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.event.PlayerKilledEntityEvent
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.mechanics.MechanicsManager
import de.hglabor.plugins.hungergames.game.mechanics.implementation.arena.Arena
import de.hglabor.plugins.hungergames.game.mechanics.implementation.DeathMessages
import de.hglabor.plugins.hungergames.game.mechanics.implementation.OfflineTimer
import de.hglabor.plugins.hungergames.game.mechanics.implementation.arena.ArenaMechanic
import de.hglabor.plugins.hungergames.game.phase.phases.InvincibilityPhase
import de.hglabor.plugins.hungergames.game.phase.phases.PvPPhase
import de.hglabor.plugins.hungergames.player.PlayerStatus
import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.kitapi.kit.isKitItem
import net.axay.kspigot.runnables.taskRunLater
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

open class IngamePhase(maxDuration: Long, nextPhase: GamePhase) : GamePhase(maxDuration, nextPhase) {
    override fun getTimeString(): String = ""
    override val timeName: String = ""

    override fun tick(tickCount: Int) {
        MechanicsManager.mechanics.filter { it.internal.isEnabled }.onEach {
            it.onTick(tickCount)
        }
        super.tick(tickCount)
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity
        val isArenaAllowed = ArenaMechanic.internal.isEnabled && Arena.isOpen
        val isEligibleForArena = player.hgPlayer.status == PlayerStatus.INGAME && !player.hgPlayer.wasInArena

        if (isArenaAllowed && isEligibleForArena) {
            DeathMessages.announce(event, true)
            taskRunLater(1) {
                Arena.queuePlayer(player)
            }
        } else {
            taskRunLater(1) { player.spigot().respawn() }
            player.gameMode = GameMode.SPECTATOR
            if (player.hgPlayer.status != PlayerStatus.GULAG) {
                DeathMessages.announce(event, false)
            }
            player.hgPlayer.status = PlayerStatus.ELIMINATED
        }
        if (event.entity.killer != null) {
            val killer = event.entity.killer ?: return
            killer.hgPlayer.kills.incrementAndGet()
            Bukkit.getPluginManager().callEvent(PlayerKilledEntityEvent(killer, player))
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val hgPlayer = player.hgPlayer

        if (hgPlayer.status == PlayerStatus.ELIMINATED) {
            hgPlayer.status = PlayerStatus.SPECTATOR
            player.gameMode = GameMode.SPECTATOR
        }

        if (GameManager.phase == InvincibilityPhase) {
            if (hgPlayer.status == PlayerStatus.LOBBY) {
                hgPlayer.login()
                hgPlayer.makeGameReady()
                player.sendMessage("${Prefix}Hurry up! The game just started.")
            }
        } else if (GameManager.phase == PvPPhase) {
            if (hgPlayer.status == PlayerStatus.LOBBY) {
                hgPlayer.status = PlayerStatus.SPECTATOR
                player.sendMessage("${Prefix}You are too late, the game has already started.")
                player.gameMode = GameMode.SPECTATOR
            }
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        OfflineTimer.putAndStartTimer(event.player.hgPlayer)
    }


    @EventHandler
    fun onPlayerCraftItem(event: CraftItemEvent) {
        event.isCancelled = event.inventory.contents.any { it.isKitItem }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.isCancelled) return
        val damager = event.damager as? Player ?: return
        val entity = event.entity as? Player ?: return
        damager.hgPlayer.combatTimer.set(12)
        entity.hgPlayer.combatTimer.set(12)
    }
}

