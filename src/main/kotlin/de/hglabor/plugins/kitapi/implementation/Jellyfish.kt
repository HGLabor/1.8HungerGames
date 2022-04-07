package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.HungerGames
import de.hglabor.plugins.kitapi.cooldown.CooldownProperties
import de.hglabor.plugins.kitapi.cooldown.MultipleUsesCooldownProperties
import de.hglabor.plugins.kitapi.cooldown.applyCooldown
import de.hglabor.plugins.kitapi.kit.Kit
import net.axay.kspigot.event.listen
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.player.PlayerInteractEvent


class JellyfishProperties : MultipleUsesCooldownProperties(15,30000) {

    val waterRemoveDelay by int(50)

}

val Jellyfish = Kit("Jellyfish", ::JellyfishProperties) {

    displayMaterial = Material.RAW_FISH
    val waterBlocks = ArrayList<Block>()

    listen<BlockFromToEvent> {
        if(it.block.type == Material.WATER || it.block.type == Material.STATIONARY_WATER) {
            if (waterBlocks.contains(it.block)) {
                it.isCancelled = true
            }
        }
    }

    kitPlayerEvent<PlayerInteractEvent> {
        if(it.action != Action.RIGHT_CLICK_BLOCK) return@kitPlayerEvent
        applyCooldown(it) {
            it.clickedBlock.getRelative(BlockFace.UP).type = Material.STATIONARY_WATER
            Bukkit.getScheduler().runTaskLater(HungerGames.INSTANCE, {
                it.clickedBlock.getRelative(BlockFace.UP).type = Material.AIR
                waterBlocks.remove(it.clickedBlock.getRelative(BlockFace.UP))
            }, kit.properties.waterRemoveDelay.toLong())
            waterBlocks.add(it.clickedBlock.getRelative(BlockFace.UP))
        }
    }

}