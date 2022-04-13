package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.hungergames.utils.ChanceUtils
import de.hglabor.plugins.kitapi.cooldown.MultipleUsesCooldownProperties
import de.hglabor.plugins.kitapi.cooldown.applyCooldown
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.isKitItem
import net.axay.kspigot.extensions.events.isRightClick
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.WitherSkull
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType


class ReaperProperties : MultipleUsesCooldownProperties(4, 25000) {
    val baseEffectDuration by int(3)
    val effectMultiplier by int(0)
    val blueWitherProbability by int(10)
}

val Reaper = Kit("Reaper", ::ReaperProperties) {
    displayMaterial = Material.IRON_HOE

    clickableItem(ItemStack(Material.IRON_HOE)) {
        if (!it.action.isRightClick) return@clickableItem
        applyCooldown(it) {
            val player = it.player
            val loc = player.eyeLocation.clone().add(player.location.direction.multiply(2.3))
            val witherSkull = player.world.spawnEntity(loc, EntityType.WITHER_SKULL) as WitherSkull
            if (ChanceUtils.roll(kit.properties.blueWitherProbability)) {
                witherSkull.isCharged = true
            }
            witherSkull.velocity = player.location.direction.multiply(1.75);
        }
    }

    kitPlayerEvent<EntityDamageByEntityEvent>({ it.damager as? Player }) { it, damager ->
        if (damager.itemInHand.isKitItem && damager.itemInHand.type == Material.IRON_HOE) {
            val target = it.entity as? LivingEntity ?: return@kitPlayerEvent
            target.addPotionEffect(
                PotionEffect(
                    PotionEffectType.WITHER,
                    (this.kit.properties.baseEffectDuration + damager.hgPlayer.kills.get()) * 20,
                    this.kit.properties.effectMultiplier
                )
            )
        }
    }
}
