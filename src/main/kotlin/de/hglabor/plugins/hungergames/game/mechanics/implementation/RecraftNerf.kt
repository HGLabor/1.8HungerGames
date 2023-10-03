package de.hglabor.plugins.hungergames.game.mechanics.implementation

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.mechanics.Mechanic
import de.hglabor.plugins.hungergames.game.phase.phases.PvPPhase
import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.kitapi.kit.isKitItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

val RecraftNerf by Mechanic("Recraft Nerf") {
    displayMaterial = Material.MUSHROOM_SOUP

    val MAX_RECRAFT = 64
    val SECONDS_BETWEEN_CHECK = 10
    val emptyStack = ItemStack(Material.AIR)

    fun getRecraftItems(player: Player): List<Pair<Int, ItemStack>> {
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

    fun removeMushrooms(player: Player, mushroomToRemove: Material, amountToKeep: Int, sortedStacks: List<Pair<Int, ItemStack>>) {
        val (oppositeSlot, oppositeStack) = sortedStacks.first { (_, it) -> it.type == mushroomToRemove }
        if (oppositeStack.amount <= amountToKeep) return
        if (amountToKeep == 0) player.inventory.setItem(oppositeSlot, emptyStack)
        oppositeStack.amount = amountToKeep
    }

    fun removeExcessItems(player: Player, recraftStacks: List<Pair<Int, ItemStack>>) {
        val mergedStacks = mergeRecraftItems(player, recraftStacks).sortedBy { it.second.amount }
        val totalRecraft = countRecraftItems(mergedStacks.map { it.second })
        var remainingToRemove = totalRecraft - MAX_RECRAFT
        if (remainingToRemove <= 0) return
        player.sendMessage("${Prefix}Sorry, you can't carry more than 64 recraft at the time.")
//        player.sendMessage("Recraft nerf:")
//        player.sendMessage(" - You had a total of: $totalRecraft")
//        mergedStacks.onEach {
//            player.sendMessage("   - ${it.second.amount}x ${it.second.type.name}")
//        }
//        player.sendMessage(" - Amount to remove: $remainingToRemove")


        var mushroomsWereRemoved = false
        for ((slot, stack) in mergedStacks) {
            val isMushroom = stack.type == Material.RED_MUSHROOM || stack.type == Material.BROWN_MUSHROOM
            if (mushroomsWereRemoved && isMushroom) continue
            val amountToRemove = stack.amount.coerceAtMost(remainingToRemove)
            //player.sendMessage("Removed: ${amountToRemove}x ${stack.type.name}")
            if (amountToRemove >= stack.amount) {
                stack.amount = 0
                player.inventory.setItem(slot, emptyStack)
            } else {
                stack.amount -= amountToRemove
            }
            remainingToRemove -= amountToRemove

            if (isMushroom) {
                val oppositeMushroom = if (stack.type == Material.BROWN_MUSHROOM) Material.RED_MUSHROOM else Material.BROWN_MUSHROOM
                val remainingOfRemovedMushroom = stack.amount
                removeMushrooms(player, oppositeMushroom, remainingOfRemovedMushroom, mergedStacks)
                mushroomsWereRemoved = true
            }

            if (remainingToRemove <= 0) break
        }
    }

    onTick { second ->
        if (GameManager.phase !is PvPPhase || second % SECONDS_BETWEEN_CHECK != 0) return@onTick
        CoroutineScope(Dispatchers.IO).launch {
            val windows = PlayerList.alivePlayers.windowed(5, 5, true)
            windows.forEach window@{ players ->
                CoroutineScope(Dispatchers.IO).launch {
                    players.forEach player@{ hgPlayer ->
                        val player = hgPlayer.bukkitPlayer ?: return@player
                        val recraftStacks = getRecraftItems(player)
                        val totalRecraftAmount = countRecraftItems(recraftStacks.map { it.second })
                        if (totalRecraftAmount <= MAX_RECRAFT) return@player
                        removeExcessItems(player, recraftStacks)
                    }
                }
                delay((SECONDS_BETWEEN_CHECK - 1 * 1000L) / windows.size)
            }
        }
    }
}

private val recraftMaterials = listOf(Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.CACTUS)
private fun ItemStack.isCocoa() = type == Material.INK_SACK && data.data.toInt() == 3
private fun ItemStack.isRecraftMaterial(): Boolean {
    return isCocoa() || type in recraftMaterials
}