package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.hungergames.utils.ChanceUtils
import de.hglabor.plugins.kitapi.cooldown.MultipleUsesCooldownProperties
import de.hglabor.plugins.kitapi.cooldown.applyCooldown
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.isKitItem
import net.axay.kspigot.extensions.events.isRightClick
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.WitherSkull
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType


class ReaperProperties : MultipleUsesCooldownProperties(4, 25) {
    val witherEffectDuration by int(3)
    val witherAmplifier by int(0)
    val blueWitherLikelihood by int(10)
}

val Reaper by Kit("Reaper", ::ReaperProperties) {
    displayMaterial = Material.IRON_HOE
    description {
        +"${ChatColor.WHITE}Right-click ${ChatColor.GRAY}to shoot ${ChatColor.WHITE}wither skulls"
        +"${ChatColor.WHITE}Hit ${ChatColor.GRAY}an enemy with your kit-item to give them ${ChatColor.WHITE}wither effect"
    }

    clickableItem(ItemStack(Material.IRON_HOE)) {
        if (!it.action.isRightClick) return@clickableItem
        applyCooldown(it) {
            val player = it.player
            val loc = player.eyeLocation.clone().add(player.location.direction.multiply(2.3))
            val witherSkull = player.world.spawnEntity(loc, EntityType.WITHER_SKULL) as WitherSkull
            if (ChanceUtils.roll(kit.properties.blueWitherLikelihood)) {
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
                    (this.kit.properties.witherEffectDuration + damager.hgPlayer.kills.get()) * 20,
                    this.kit.properties.witherAmplifier
                )
            )
        }
    }
}
