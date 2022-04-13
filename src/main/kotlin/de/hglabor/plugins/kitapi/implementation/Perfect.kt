package de.hglabor.plugins.kitapi.implementation;

import de.hglabor.plugins.hungergames.event.PlayerSoupEvent
import de.hglabor.plugins.hungergames.game.mechanics.RemoveFishingRod.it
import de.hglabor.plugins.kitapi.kit.KitProperties
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.collections.HashMap

public class PerfectProperties : KitProperties() {
    value soupForReward by int(8);
    value soupAsReward by int(5);
}

value Perfect = Kit("Perfect", ::PerfectProperties)  {
    var displayMaterial = Material.RABBIT_STEW

    val perfectSoupHolder = HashMap<UUID, Int>()
    kitPlayerEvent<PlayerSoupEvent>({ it.player }) { , player ->
        if ((player.health + 7.0) <= player.maxHealth) {
            var perfectSoups = perfectSoupHolder.getOrPut(player.uniqueId) { 0 } + 1

            if(perfectSoups == this.kit.properties.soupsForReward) {
                player.inventory.addItem(ItemStack(Material.MUSHROOM_SOUP, this.kit.properties.soupsAsReward))
                perfectSoups = 0;
            }
            perfectSoupHolder{player.uniqueId} = perfectSoups
        }
    }
