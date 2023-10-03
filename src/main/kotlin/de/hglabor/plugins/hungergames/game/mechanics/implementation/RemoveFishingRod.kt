package de.hglabor.plugins.hungergames.game.mechanics.implementation

import de.hglabor.plugins.hungergames.game.mechanics.Mechanic
import net.axay.kspigot.event.listen
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.PlayerInteractEvent

val RemoveFishingRod by Mechanic("No Fishing Rod") {
    displayMaterial = Material.FISHING_ROD

    onGameStart {
        val iterator = Bukkit.getServer().recipeIterator()
        while (iterator.hasNext()) {
            val recipe = iterator.next()
            if (recipe != null && recipe.result.type == Material.FISHING_ROD) {
                iterator.remove()
            }
        }
    }

    mechanicPlayerEvent<PlayerInteractEvent> { it, player ->
        if (it.item?.type != Material.FISHING_ROD) return@mechanicPlayerEvent
        it.isCancelled = true
        player.inventory.removeAll { it?.type == Material.FISHING_ROD }
        player.updateInventory()
    }

    mechanicEvent<CraftItemEvent> {
        if (it.recipe.result.type == Material.FISHING_ROD) it.isCancelled = true
    }

    mechanicEvent<ItemSpawnEvent> {
        if (it.entity.itemStack.type == Material.FISHING_ROD) it.isCancelled = true
    }
}