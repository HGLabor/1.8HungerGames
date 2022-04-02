package de.hglabor.plugins.hungergames.game

import de.hglabor.plugins.hardcoregames.player.HGPlayer
import org.bukkit.entity.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class Tracker : Listener {
    @EventHandler
    fun onUseTracker(event: PlayerInteractEvent) {
        val player: Player = event.getPlayer()
        val target = searchForCompassTarget(player)
        if (event.getMaterial() == Material.COMPASS) {
            if (target == null) {
                player.sendMessage(
                    Localization.INSTANCE.getMessage(
                        "hglabor.tracker.noTarget",
                        ChatUtils.getPlayerLocale(player)
                    )
                )
            } else {
                player.setCompassTarget(target.location)
                player.sendMessage(
                    Localization.INSTANCE.getMessage(
                        "hglabor.tracker.target",
                        ImmutableMap.of("targetName", target.name),
                        ChatUtils.getPlayerLocale(player)
                    )
                )
            }
        }
    }

    private fun searchForCompassTarget(tracker: Player): Entity? {
        for (hgPlayer in PlayerList.INSTANCE.getOnlinePlayers()) {
            val possibleTarget: Entity = Bukkit.getEntity(hgPlayer.getUUID()) ?: continue
            if (tracker === possibleTarget) continue
            if (possibleTarget.location.distanceSquared(tracker.getLocation()) > 30.0) {
                return possibleTarget
            }
        }
        return null
    }
}