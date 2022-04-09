package de.hglabor.plugins.hungergames.game.phase.phases

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.phase.GamePhase
import de.hglabor.plugins.hungergames.player.HGPlayer
import de.hglabor.plugins.hungergames.player.PlayerList
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.extensions.onlinePlayers
import org.bukkit.*
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.ItemStack


object EndPhase : GamePhase(25, null) {
    override fun getTimeString() = "Ended"
    override val timeName = "Game"
    var winner: HGPlayer? = null

    override fun onStart() {
        val platformLoc = createWinningPlatform()
        winner = PlayerList.alivePlayers.singleOrNull() ?: PlayerList.alivePlayers.minByOrNull { it.kills.get() }!!

        onlinePlayers.filter { it != winner?.bukkitPlayer }.forEach {
            it.gameMode = GameMode.SPECTATOR
            it.teleport(platformLoc)
        }

        winner?.bukkitPlayer?.apply {
            allowFlight = true
            isFlying = false
            teleport(platformLoc)
            inventory.clear()
            inventory.addItem(ItemStack(Material.WATER_BUCKET))
        }
    }

    override fun tick(tickCount: Int) {
        if (tickCount < 5) {
            broadcast(
                if (winner != null) "${Prefix}${ChatColor.LIGHT_PURPLE}${winner?.name} ${ChatColor.GRAY}won."
                else "${Prefix}${ChatColor.RED}Nobody ${ChatColor.GRAY}won."
            )
        }

        if (GameManager.elapsedTime.get() == 25L) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop")
        }
    }

    @EventHandler
    fun onEntityDamageEvent(event: EntityDamageEvent) {
        event.isCancelled = true
    }

    private fun createWinningPlatform(): Location {
        val loc = GameManager.world.getHighestBlockAt(0, 0).location.add(0.0, 50.0, 0.0)
        for (x in -2..2) {
            for (y in -2..-1) {
                for (z in -2..2) {
                    val material = if (y == -2) Material.GLASS else Material.CAKE_BLOCK
                    loc.clone().add(x.toDouble(), y.toDouble(), z.toDouble()).block.type = material
                }
            }
        }
        return loc.clone().add(0.0, 2.0, 0.0)
    }
}