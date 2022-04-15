package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.kitapi.kit.Kit
import de.hglabor.plugins.kitapi.kit.KitProperties
import net.axay.kspigot.chat.KColors
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.litote.kmongo.addEachToSet

class TobleroneProperties : KitProperties() {
    val effectDuration by int(60)
    val min by int(0)
    val max by int(10)
}

fun rand(min: Int, max: Int): Int {
    if(min>=max) System.out.println("${KColors.RED}THE MIN=$min IS BIGGER THAN THE MAX=$max")
    return (Math.random() * (max - min + 1)).toInt() + min
}

val Toblerone = Kit("Toblerone", ::TobleroneProperties) {
    displayMaterial = (Material.BANNER)
    val uses = buildMap<Player, Int> {  }.toMutableMap()

    kitPlayerEvent<PlayerDeathEvent> ({it.entity.killer as Player}) {it, player ->
        val killer = it.entity.killer as? LivingEntity ?: return@kitPlayerEvent
        if (killer !is Player) return@kitPlayerEvent
        uses[killer]?.plus(1)
    }

    placeableItem(ItemStack(Material.BANNER)) {
        it.isCancelled = true
        if(uses.get(it.player)!! <= 0) { return@placeableItem  it.player.sendMessage("${Prefix}You have ${uses.get(it.player)} uses left.")}
        else {
            uses[it.player]?.minus(1)
            it.player.sendMessage("${Prefix}You have ${uses.get(it.player)} uses left.")
        }
        it.player.playSound(it.player.location, Sound.EAT, 2f, 1f)
        it.player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, this.kit.properties.effectDuration * 20, rand(this.kit.properties.min, this.kit.properties.max)))
        it.player.addPotionEffect(PotionEffect(PotionEffectType.JUMP, this.kit.properties.effectDuration * 20, rand(this.kit.properties.min, this.kit.properties.max)))
    }
}

