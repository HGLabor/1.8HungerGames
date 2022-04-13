package de.hglabor.plugins.kitapi.implementation


import de.hglabor.plugins.hungergames.event.PlayerSoupEvent
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.*

class PerfectProperties : KitProperties() {
    val soupsForReward by int(8);
    val soupsAsReward by int(5);
}

val Perfect = Kit("Perfect", ::PerfectProperties)  {
    displayMaterial = Material.RABBIT_STEW

    val perfectSoupHolder = HashMap<UUID, Int>()
    kitPlayerEvent<PlayerSoupEvent>({ it.player }) { it, player ->
        if (!it.overhealed) {
            var perfectSoups = perfectSoupHolder.getOrPut(player.uniqueId) { 0 } + 1

            if (perfectSoups == this.kit.properties.soupsForReward) {
                player.inventory.addItem(ItemStack(Material.MUSHROOM_SOUP, this.kit.properties.soupsAsReward))
                perfectSoups = 0;
            }
            perfectSoupHolder[player.uniqueId] = perfectSoups
        } else {
            perfectSoupHolder[player.uniqueId] = 0
        }
    }
}