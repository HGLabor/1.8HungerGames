package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.kitapi.cooldown.Cooldown
import de.hglabor.plugins.kitapi.cooldown.CooldownProperties
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.utils.OnlinePlayerMap
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent


class ReviveProperties : CooldownProperties(50000)

val Revive = Kit("Revive", ::CounterProperties) {
    displayMaterial = Material.GOLDEN_APPLE

    val lastDamaged = OnlinePlayerMap<Player?>()
    val lastDamagedTask = OnlinePlayerMap<KSpigotRunnable?>()

    kitPlayerEvent<EntityDamageEvent> {

        if (it.entity.getHealth() - it.finalDamage <= 0
            {
                it.isCancelled;
            }
        );
    }
}

private operator fun Int.invoke(value: Any) {

}





