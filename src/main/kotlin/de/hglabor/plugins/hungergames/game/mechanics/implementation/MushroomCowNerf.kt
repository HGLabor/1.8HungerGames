package de.hglabor.plugins.hungergames.game.mechanics.implementation

import de.hglabor.plugins.hungergames.SecondaryColor
import de.hglabor.plugins.hungergames.game.mechanics.Mechanic
import de.hglabor.plugins.hungergames.player.hgPlayer
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.spawnCleanEntity
import org.bukkit.ChatColor
import org.bukkit.Effect
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.MushroomCow
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import java.util.*

val MushroomCowNerf = Mechanic("Mushroom Cow Nerf") {
    description = "Mushroom Cows will turn into normal cows after milking it 16 times"
    displayMaterial = Material.RED_MUSHROOM

    val SOUPS_PER_COW = 16
    val ALLOW_IN_COMBAT = true
    val cows = mutableMapOf<UUID, Int>()

    mechanicEvent<EntitySpawnEvent> {
        if (it.entity.type != EntityType.MUSHROOM_COW) return@mechanicEvent
        it.entity.isCustomNameVisible = false
        it.entity.customName = "${SecondaryColor}${ChatColor.BOLD}$SOUPS_PER_COW"
    }

    mechanicPlayerEvent<PlayerInteractEntityEvent> { it, player ->
        val rightClicked = it.rightClicked as? MushroomCow ?: return@mechanicPlayerEvent
        if (player.itemInHand == null || player.itemInHand.type != Material.BOWL) return@mechanicPlayerEvent
        if (!ALLOW_IN_COMBAT && player.hgPlayer.isInCombat) {
            it.isCancelled = true
            return@mechanicPlayerEvent
        }
        val amountMilked = cows.getOrDefault(rightClicked.uniqueId, 0) + 1

        if (amountMilked == SOUPS_PER_COW) {
            cows.remove(rightClicked.uniqueId)
            rightClicked.remove()
            val loc = rightClicked.location.clone()
            loc.spawnCleanEntity(EntityType.COW)
            loc.world.playEffect(loc.add(0.0, 0.5, 0.0), Effect.EXPLOSION_LARGE, 1)
            loc.world.playSound(loc, Sound.SHEEP_SHEAR, 3f, 1f)
            return@mechanicPlayerEvent
        }
        rightClicked.customName = "${SecondaryColor}${ChatColor.BOLD}${SOUPS_PER_COW - amountMilked}"
        cows[rightClicked.uniqueId] = amountMilked

    }
}