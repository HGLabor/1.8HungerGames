package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.SecondaryColor
import de.hglabor.plugins.hungergames.utils.hasMark
import de.hglabor.plugins.hungergames.utils.mark
import de.hglabor.plugins.hungergames.utils.unmark
import de.hglabor.plugins.kitapi.cooldown.MultipleUsesCooldownProperties
import de.hglabor.plugins.kitapi.cooldown.applyCooldown
import de.hglabor.plugins.kitapi.kit.Kit
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.spawnCleanEntity
import net.axay.kspigot.extensions.geometry.add
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import net.axay.kspigot.runnables.taskRunLater
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.math.absoluteValue


class EndermageProperties : MultipleUsesCooldownProperties(5, 15) {
    val searchRadius by double(4.0)
    val searchTime by int(5)
}

private val whoMaged = mutableMapOf<UUID, UUID>()

val Endermage by Kit("Endermage", ::EndermageProperties) {
    displayMaterial = Material.ENDER_PORTAL_FRAME
    description {
        +"${ChatColor.WHITE}Place ${ChatColor.GRAY}your kit-item to teleport other player to you"
        +"${ChatColor.GRAY}After teleporting you are invulnerable for 5 seconds"
    }

    val mageInstances = mutableMapOf<UUID, EndermageSearch>()

    clickableItem(ItemStack(Material.EYE_OF_ENDER)) {
        it.isCancelled = true
        val player = it.player
        applyCooldown(player) {
            if (mageInstances.containsKey(player.uniqueId)) {
                cancelCooldown()
                return@clickableItem
            }
            val clickedBlock = it.clickedBlock ?: return@clickableItem
            clickedBlock.type = Material.ENDER_PORTAL_FRAME
            mageInstances[player.uniqueId] = EndermageSearch(player, clickedBlock.location.add(0.5, 1.0, 0.5))
            taskRunLater(kit.properties.searchTime*20L) {
                clickedBlock.type = Material.ENDER_STONE
                mageInstances[player.uniqueId]?.cancelSearching()
                mageInstances.remove(player.uniqueId)
            }
        }
    }

    listen<EntityDamageEvent> {
        val player = it.entity as? Player ?: return@listen
        if (player.hasMark("wasMaged")) {
            it.isCancelled = true
        }
    }

    listen<EntityDamageByEntityEvent> {
        if (it.entity.hasMark("wasMaged") || it.damager.hasMark("wasMaged")) {
            it.isCancelled = true
        }
    }
}

class EndermageSearch(mage: Player, val location: Location) {
    private val mageUUID: UUID = mage.uniqueId
    var task: KSpigotRunnable? = null
    private var firstMage = true
    private var tempEntity: ArmorStand? =
    (location.clone().add(0, 100, 0).spawnCleanEntity(EntityType.ARMOR_STAND) as ArmorStand).apply {
        isVisible = false
        isSmall = true
    }

    init {
        startSearching()
    }

    private fun startSearching() {
        task = task(true, 0, 10) {
            val radius = Endermage.properties.searchRadius
            val tempEntity = tempEntity ?: return@task

            for (entity in tempEntity.getNearbyEntities(radius, 256.0, radius)) {
                val player = entity as? Player ?: continue
                if (!player.isMagable) continue
                if (player.hasMark("wasMaged") && whoMaged[player.uniqueId] == mageUUID) continue
                if (player.uniqueId == mageUUID) continue
                if ((player.location.y - location.y).absoluteValue < 4) continue
                if (firstMage) {
                    Bukkit.getPlayer(mageUUID)?.teleport(location)
                    firstMage = false
                }

                player.leaveVehicle()
                player.teleport(location)
                whoMaged[player.uniqueId] = mageUUID
                player.mark("wasMaged")
                player.sendMessage("${Prefix}You have been ${SecondaryColor}maged${ChatColor.GRAY}! You are now ${ChatColor.WHITE}invulnerable ${ChatColor.GRAY}for ${ChatColor.WHITE}5 seconds${ChatColor.GRAY}.")

                taskRunLater(5*20) {
                    if (whoMaged[entity.uniqueId] == mageUUID) {
                        player.unmark("wasMaged")
                        player.sendMessage("${Prefix}You are now vulnerable again.")
                    }
                }
            }
        }
    }

    fun cancelSearching() {
        task?.cancel()
        task = null
        tempEntity?.remove()
        tempEntity = null
    }

    private val Player.isMagable: Boolean
        get() = !(player.isInGladiator || player.isInUltimato)
}