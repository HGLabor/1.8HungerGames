package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.HungerGames
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.phase.phases.LobbyPhase
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import net.axay.kspigot.utils.OnlinePlayerMap
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent


class NewVersionProperties : KitProperties() {
    val attackSpeed by int(60)
    val additivePercentalDamage by int(50)
}

private val plugin: HungerGames? = null
val coolddown = buildMap<Player, Boolean> {}.toMutableMap()





val NewVersion = Kit("NewVersion", ::NewVersionProperties) {
    val lastDamagedTask = OnlinePlayerMap<KSpigotRunnable?>()
    val lastHit = buildMap<Player, Int> {}.toMutableMap()
    //val scheduler = Bukkit.getServer().scheduler
    //scheduler.scheduleSyncDelayedTask(plugin, Runnable {
    // code execute
    //} as BukkitRunnable?, this.kit.properties.attackSpeed.toLong()*20)
    displayMaterial = Material.DIAMOND_HOE


    kitPlayerEvent<EntityDamageByEntityEvent>({ it.damager as? Player }, EventPriority.HIGH) {it, player ->
        if (GameManager.phase == LobbyPhase) return@kitPlayerEvent
        if (!it.isCancelled) {
            lastDamagedTask[player]?.cancel()

            var timer = 1

            if(lastHit[player] == 0) lastHit[player] == 1
            lastDamagedTask[player] = task(false, 20, 20) {
                if(lastHit[player] == 1) {
                    it.cancel()
                    lastHit[player] == 0
                }

                if (--timer == 0) {
                    lastDamagedTask[player] = null
                    it.cancel()
                }
            }
            val T = 20/1
            val t = (1-timer)/20
            val damageMultiplier = 0.2 + (((t+0.5) / T) * ((t+0.5) / T)) * 0.8
            // add damageMultiplier to damage of enemy
        }


    }


}
