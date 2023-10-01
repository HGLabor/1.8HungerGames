package de.hglabor.plugins.hungergames.game.mechanics.implementation.recraftnerf

import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.mechanics.Mechanic
import de.hglabor.plugins.hungergames.game.phase.phases.PvPPhase
import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.kitapi.kit.isKitItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.axay.kspigot.extensions.broadcast
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

val RecraftNerf by Mechanic("Recraft Nerf") {
    val MAX_RECRAFT = 64
    val emptyStack = ItemStack(Material.AIR)

    fun getRecraftStacks(player: Player): List<Pair<Int, ItemStack>> {
        return player.inventory.contents.mapIndexedNotNull { index, item ->
            if (item != null && item.type != null && item.isRecraftMaterial() && !item.isKitItem) index to item
            else null
        }
    }

    fun countRecraftItems(recraftStacks: List<ItemStack>): Int {
        val redMushroomCount = recraftStacks.filter { it.type == Material.RED_MUSHROOM }.sumOf { it.amount }
        val brownMushroomCount = recraftStacks.filter { it.type == Material.BROWN_MUSHROOM }.sumOf { it.amount }
        val cactusCount = recraftStacks.filter { it.type == Material.CACTUS }.sumOf { it.amount }
        val cocoaCount = recraftStacks.filter { it.isCocoa() }.sumOf { it.amount }
        return cactusCount + cocoaCount + redMushroomCount.coerceAtMost(brownMushroomCount)
    }

    fun removeMushrooms(player: Player, mushroomToRemove: Material, amountToKeep: Int, sortedStacks: List<Pair<Int, ItemStack>>) {
        val oppositeMushroomStacks = sortedStacks.filter { (_, it) -> it.type == mushroomToRemove }.toMutableList()
        if (oppositeMushroomStacks.sumOf { (_, it) -> it.amount } <= amountToKeep) return
        val biggestOppositeMushroomStack = oppositeMushroomStacks.removeLast()
        oppositeMushroomStacks.forEach { (slot, _) -> player.inventory.setItem(slot, emptyStack) }
        biggestOppositeMushroomStack.second.amount = amountToKeep
    }

    fun mergeRecraftItems(player: Player, recraftStacks: List<Pair<Int, ItemStack>>): List<Pair<Int, ItemStack>> {
        val remainingStacks = mutableListOf<Pair<Int, ItemStack>>()
        val groupedStacks = recraftStacks.sortedBy { (_, item) -> item.amount }.groupBy { it.second.type }

        groupedStacks.forEach { type, _stacks ->
            val stacks = _stacks.toMutableList()
            val totalOfType = stacks.sumOf { it.second.amount }
            val biggestStack = stacks.removeLast()
            stacks.forEach { (slot, _) ->
                player.inventory.setItem(slot, emptyStack)
            }
            biggestStack.second.amount = totalOfType.coerceAtMost(MAX_RECRAFT)
            remainingStacks.add(biggestStack)
        }
        return remainingStacks
    }

    fun removeExcessItems(player: Player, totalRecraft: Int, recraftStacks: List<Pair<Int, ItemStack>>) {
        if (totalRecraft <= MAX_RECRAFT) return
        var remainingToRemove = totalRecraft - MAX_RECRAFT
        broadcast("removing ${remainingToRemove}x recraft for ${player.name}")

        val mergedStacks = mergeRecraftItems(player, recraftStacks)

        for ((slot, stack) in mergedStacks) {
            val amountToRemove = stack.amount.coerceAtMost(remainingToRemove)
            if (amountToRemove >= stack.amount) {
                stack.amount = 0
                player.inventory.setItem(slot, emptyStack)
            } else {
                stack.amount -= amountToRemove
            }
            remainingToRemove -= amountToRemove

            if (stack.type == Material.RED_MUSHROOM || stack.type == Material.BROWN_MUSHROOM) {
                val oppositeMushroom = if (stack.type == Material.BROWN_MUSHROOM) Material.RED_MUSHROOM else Material.BROWN_MUSHROOM
                val remainingOfRemovedMushroom = mergedStacks.filter { (_, it) -> it.type == stack.type }.sumOf { it.second.amount }
                removeMushrooms(player, oppositeMushroom, remainingOfRemovedMushroom, mergedStacks)
            }

            if (remainingToRemove <= 0) break
        }
    }

    onTick { second ->
        if (GameManager.phase !is PvPPhase || second % 5 != 0) return@onTick
        broadcast("Recraft nerf tick:")
        CoroutineScope(Dispatchers.IO).launch {
            val windows = PlayerList.alivePlayers.windowed(5, 5, true)
            windows.forEach window@{ players ->
                CoroutineScope(Dispatchers.IO).launch {
                    players.forEach player@{ hgPlayer ->
                        val player = hgPlayer.bukkitPlayer ?: return@player
                        val recraftStacks = getRecraftStacks(player)
                        val totalRecraftAmount = countRecraftItems(recraftStacks.map { it.second })
                        broadcast("${player.name} has $totalRecraftAmount recraft")
                        if (totalRecraftAmount < 64) return@player
                        removeExcessItems(player, totalRecraftAmount, recraftStacks)
                    }
                }
                delay(4000L / windows.size)
            }
        }
    }
}

private val recraftMaterials = listOf(Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.CACTUS)
private fun ItemStack.isCocoa() = type == Material.INK_SACK && data.data.toInt() == 3
private fun ItemStack.isRecraftMaterial(): Boolean {
    return isCocoa() || type in recraftMaterials
}

