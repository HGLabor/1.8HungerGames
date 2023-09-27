package de.hglabor.plugins.hungergames.game.mechanics.implementation

import de.hglabor.plugins.hungergames.game.mechanics.Mechanic
import de.hglabor.plugins.hungergames.utils.ChanceUtils
import net.axay.kspigot.event.listen
import org.bukkit.Material
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerItemDamageEvent

val HungerNerf by Mechanic("Hunger Nerf") {
    description = "Hunger will be nerfed by 40%"
    displayMaterial = Material.BAKED_POTATO

    mechanicEvent<FoodLevelChangeEvent> {
        if (ChanceUtils.roll(40)) {
            it.isCancelled = true
        }
    }
}