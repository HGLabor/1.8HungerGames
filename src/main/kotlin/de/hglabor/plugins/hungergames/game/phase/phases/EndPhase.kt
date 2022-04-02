package de.hglabor.plugins.hungergames.game.phase.phases

import com.google.common.collect.ImmutableMap
import de.hglabor.plugins.hungergames.game.phase.GamePhase
import de.hglabor.plugins.hungergames.player.HGPlayer
import net.axay.kspigot.extensions.broadcast
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent


object EndPhase: GamePhase(25, null) {
    override fun getTimeString() = "Ended"

    override fun onStart() {
        broadcast("EndPhase")
    }

    override fun tick() {
        if (elapsedTime == 25L) {
            Bukkit.shutdown()
        }
    }
}

class EndPhase(winner: Optional<HGPlayer?>, participants: Int) :
    GamePhase(HGConfig.getInteger(ConfigKeys.END_RESTART_AFTER)) {
    val maxParticipants: Int
    private val winner: Optional<HGPlayer>
    val rawTime: Int

    init {
        rawTime = GameStateManager.INSTANCE.getTimer()
        this.winner = winner
        maxParticipants = participants
    }

    protected fun init() {
        winner.ifPresent { hgPlayer ->
            val player: Player = Bukkit.getPlayer(hgPlayer.getUUID())
            if (player != null) {
                player.allowFlight = true
                player.isFlying = true
            }
        }
        GameStateManager.INSTANCE.resetTimer()
    }

    protected fun tick(timer: Int) {
        if (timer <= maxPhaseTime) {
            winner.ifPresentOrElse({ hgPlayer ->
                ChatUtils.broadcastMessage(
                    "endPhase.winAnnouncementPlayer",
                    ImmutableMap.of("player", hgPlayer.getName())
                )
            }) {
                ChatUtils.broadcastMessage(
                    "endPhase.winAnnouncementNobody"
                )
            }
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart")
        }
    }

    val type: PhaseType
        get() = PhaseType.END

    protected fun getTimeString(timer: Int): String {
        return TimeConverter.stringify(rawTime)
    }

    val currentParticipants: Int
        get() = playerList.getAlivePlayers().size()
    protected override val nextPhase: GamePhase?
        protected get() = null

    @EventHandler
    fun onEntityDamageEvent(event: EntityDamageEvent) {
        event.isCancelled = true
    }
}