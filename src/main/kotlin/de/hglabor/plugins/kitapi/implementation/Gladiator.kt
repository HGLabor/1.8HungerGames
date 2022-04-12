package de.hglabor.plugins.kitapi.implementation

import de.hglabor.plugins.hungergames.Manager
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.player.PlayerList
import de.hglabor.plugins.hungergames.player.hgPlayer
import de.hglabor.plugins.hungergames.utils.BlockQueue
import de.hglabor.plugins.hungergames.utils.LocationUtils.setDirectionTo
import de.hglabor.plugins.hungergames.utils.WorldUtils
import de.hglabor.plugins.hungergames.utils.mark
import de.hglabor.plugins.hungergames.utils.unmark
import de.hglabor.plugins.kitapi.cooldown.CooldownManager
import de.hglabor.plugins.kitapi.cooldown.CooldownProperties
import de.hglabor.plugins.kitapi.kit.Kit
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.extensions.geometry.add
import net.axay.kspigot.extensions.geometry.subtract
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import net.axay.kspigot.runnables.taskRunLater
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import kotlin.random.Random

class GladiatorProperties : CooldownProperties(15000) {
    val radius by int(12)
    val height by int(10)
    val duration by int(240)
    val material by material(Material.GLASS)
    val materialData by int(0)
}

class GladiatorInstance(val gladiator: Player, playerTwo: Player) {
    val players = arrayOf(gladiator, playerTwo)
    var isFinished = false
    val properties = Gladiator.value.properties
    var oldLocations: Array<Location> = Array(2) { players[it].location.clone() }
    lateinit var spawnLocations: Array<Location>
    lateinit var allLocations: HashSet<Location>
    var centerLocation: Location = getGladiatorLocation(players.first().location.clone().apply { y = 95.0 }, properties.radius, properties.height)
    var lowestY = 0
    var task: KSpigotRunnable? = null
    val blockQueue = BlockQueue()

    init {
        createArena()
        makePlayersReady()
        startTimer()
    }

    private fun startTimer() {
        taskRunLater(Gladiator.value.properties.duration.toLong() * 20) {
            if (isFinished) return@taskRunLater
            players.forEach {
                it.addPotionEffect(PotionEffect(PotionEffectType.WITHER, Int.MAX_VALUE, 1))
            }
        }
        task = task(true, 20, 10) {
            if (players.any { p ->
                    p.location.blockY < lowestY || p.location.blockY > centerLocation.blockY + 1
            }) {
                endFight()
                return@task
            }
            
            // damage people who are not supposed to be in the gladiator box
            PlayerList.alivePlayers.forEach { intruder ->
                val intruderPlayer = intruder.bukkitPlayer ?: return@forEach
                if (intruderPlayer in players) return@forEach
                if (intruderPlayer.gameMode != GameMode.SURVIVAL) return@forEach
                if (allLocations.any { it.distanceSquared(intruderPlayer.location) < 0.5 })
                    intruderPlayer.damage(2.5)
            }
        }
    }

    private fun createArena() {
        val radius = properties.radius
        val height = properties.height
        val material = properties.material
        val materialData = properties.materialData

        fun setSpawnLocations() {
            spawnLocations = arrayOf(
                centerLocation.clone().add(3, 1, 0),
                centerLocation.clone().add(radius - 3, 1, 0)
            )

            spawnLocations.forEach {
                it.clone().subtract(0, 1, 0).block.type = material
            }
        }

        setSpawnLocations()
        WorldUtils.makeCircle(centerLocation, radius, 1, false, false).forEach {
            WorldUtils.setBlock(it, material, materialData.toByte(), blockQueue)
            it.block.setMetadata("gladiBlock", FixedMetadataValue(Manager, 0))
        }
        WorldUtils.makeCircle(centerLocation, radius, height, true, false).forEach {
            WorldUtils.setBlock(it, material, materialData.toByte(), blockQueue)
            it.block.setMetadata("gladiBlock", FixedMetadataValue(Manager, 0))
        }
        WorldUtils.makeCircle(centerLocation.add(0, height, 0), radius, 1, false, false).forEach {
            WorldUtils.setBlock(it, material, materialData.toByte(), blockQueue)
            it.block.setMetadata("gladiBlock", FixedMetadataValue(Manager, 0))
        }
    }

    private fun makePlayersReady() {
        players.forEachIndexed { index, player ->
            player.teleport(spawnLocations[index].setDirectionTo(spawnLocations[if (index == 1) 0 else 1]))
            player.mark("inGladi")
            player.addPotionEffect(PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 10))
            player.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 10, 128))
            player.addPotionEffect(PotionEffect(PotionEffectType.JUMP, 10, 128))
        }
    }

    fun endFight() {
        isFinished = true
        blockQueue.cancelAndFlush()
        task?.cancel()
        task = null

        if (gladiator.hgPlayer.isAlive) {
            CooldownManager.addCooldown(Gladiator.value.properties.cooldownInstance, gladiator)
        }
        players.forEachIndexed { index, player ->
            player.fallDistance = 0f
            if (!player.hgPlayer.isAlive)
                player.teleport(oldLocations[index])
            player.unmark("inGladi")
            player.addPotionEffect(PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 10))
            player.removePotionEffect(PotionEffectType.WITHER)
        }

        allLocations.forEach {
            if (it.block.type != Material.AIR)
                WorldUtils.setBlock(it, Material.AIR, 0, blockQueue)
            it.block.removeMetadata("gladiBlock", Manager)
        }
    }

    // Used for getting a free space to build the arena
    private fun getGladiatorLocation(location: Location, radius: Int, height: Int): Location {
        val cylinder = WorldUtils.makeCircle(location, radius, height + 1, false, false)
        return if (hasEnoughSpace(cylinder)) {
            allLocations = cylinder
            lowestY = location.blockY-height-1
            location
        } else {
            getGladiatorLocation(
                location.add(
                    if (Random.nextBoolean()) -10 else 10.toDouble(),
                    5.0,
                    if (Random.nextBoolean()) -10 else 10.toDouble()
                ), radius, height
            )
        }
    }

    // Used for getting a free space to build the arena
    private fun hasEnoughSpace(locations: HashSet<Location>): Boolean {
        val world = GameManager.world

        for (loc in locations) {
            if (!(loc.blockX < world.worldBorder.size + 1 && loc.blockX > -world.worldBorder.size - 1)) {
                broadcast("x not in border")
                return false
            }
            if (!(loc.blockY < world.worldBorder.size + 1 && loc.blockY > -world.worldBorder.size - 1)) {
                broadcast("y not in border")
                return false
            }

            if (loc.block.type != Material.AIR) {
                broadcast("not air :I")
                return false
            }
        }
        return true
    }
}

val Gladiator = Kit("Gladiator", ::GladiatorProperties) {
    displayMaterial = Material.IRON_FENCE
    val gladiatorInstances: HashMap<UUID, GladiatorInstance> = hashMapOf()

    fun stopGladi(gladiatorInstance: GladiatorInstance) {
        gladiatorInstance.players.forEach {
            gladiatorInstances -= it.uniqueId
        }

        gladiatorInstance.endFight()
    }

    clickOnEntityItem(ItemStack(Material.IRON_FENCE)) {
        val rightClicked = it.rightClicked as? Player ?: return@clickOnEntityItem
        if (!rightClicked.hgPlayer.isAlive) return@clickOnEntityItem

        GladiatorInstance(it.player, rightClicked)
    }

    /*kitPlayerEvent<PlayerToggleSneakEvent> {
        if (it.player.isSneaking)
            GladiatorInstance(it.player, it.player)
    }*/

    listen<BlockBreakEvent> {
        val block = it.block
        if (!block.hasMetadata("gladiBlock")) return@listen
        it.isCancelled = true
        val type = block.type
        val id = block.data

        if (type == kit.properties.material) {
            block.setTypeIdAndData(Material.STAINED_GLASS.id, 14, false)
            return@listen
        }

        when (id) {
            14.toByte() -> block.setTypeIdAndData(Material.STAINED_GLASS.id, 4, false)
            4.toByte() -> block.setTypeIdAndData(Material.STAINED_GLASS.id, 5, false)
            5.toByte() -> {
                block.setType(Material.AIR, false)
                block.removeMetadata("gladiBlock", Manager)
            }
        }
    }

    listen<PlayerDeathEvent> {
        val gladi = gladiatorInstances[it.entity.uniqueId] ?: return@listen
        stopGladi(gladi)
    }

    listen<PlayerQuitEvent> {
        val gladi = gladiatorInstances[it.player.uniqueId] ?: return@listen
        stopGladi(gladi)
    }
}