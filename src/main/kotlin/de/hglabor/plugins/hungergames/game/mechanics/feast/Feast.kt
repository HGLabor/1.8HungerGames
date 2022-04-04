package de.hglabor.plugins.hungergames.game.mechanics.feast

import de.hglabor.plugins.hungergames.Manager
import de.hglabor.plugins.hungergames.Prefix
import de.hglabor.plugins.hungergames.utils.RandomCollection
import de.hglabor.plugins.hungergames.utils.TimeConverter
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.runnables.sync
import net.axay.kspigot.runnables.task
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random


class Feast(world: World) : Listener {
    private val feastBlocks: MutableSet<Block>
    private val world: World
    private var feastCenter: Location? = null
    private var platformMaterial: Material? = null
    private var radius = 0
    private var timer: AtomicInteger? = null
    private var totalTime = 0
    private var airHeight = 0
    private var maxItemsInChest: Int
    private var inPreparation = false
    private var isFinished = false
    private var shouldDamageItems = false

    init {
        this.world = world
        feastBlocks = HashSet()
        maxItemsInChest = 6
    }

    fun center(feastCenter: Location?): Feast {
        this.feastCenter = feastCenter
        return this
    }

    fun material(platformMaterial: Material?): Feast {
        this.platformMaterial = platformMaterial
        return this
    }

    fun radius(radius: Int): Feast {
        this.radius = radius
        return this
    }

    fun timer(timer: Int): Feast {
        this.timer = AtomicInteger(timer)
        totalTime = timer
        return this
    }

    fun maxItemsInChest(maxItemsInChest: Int): Feast {
        this.maxItemsInChest = maxItemsInChest
        return this
    }

    fun air(height: Int): Feast {
        airHeight = height
        return this
    }

    fun damageItems(shouldDamage: Boolean): Feast {
        shouldDamageItems = shouldDamage
        return this
    }

    fun spawn() {
        announceFeast()
        inPreparation = true
        createCylinder()
        startCountDown()
    }

    /*private fun setBlock(loc: Location, material: Material, data: Byte) {
        val world = loc.world
        val x = loc.blockX
        val y = loc.blockY
        val z = loc.blockZ
        val nmsWorld = (world as CraftWorld).handle
        val nmsChunk = nmsWorld.getChunkAt(x shr 4, z shr 4)
        val ibd: IBlockData = net.minecraft.server.v1_8_R3.Block.getByCombinedId(material.id + (data.toInt() shl 12))
        nmsChunk.a(BlockPosition(x, y, z), ibd)
        *//*val cs: ChunkSection = ChunkSection(y shr 4 shl 4, true)
        nmsChunk.sections[y shr 4] = cs
        cs.setType(x and 15, y and 15, z and 15, ibd)*//*
    }*/

    private fun createCylinder() {
        //val chunkSet = hashSetOf<Chunk>()
        for (x in -radius until radius) {
            for (z in -radius until radius) {
                for (y in 0..10) {
                    val loc = feastCenter?.block?.getRelative(x, y, z)?.location!!
                    val material = if (y == 0) platformMaterial ?: Material.GRASS else Material.AIR
                    /*setBlock(loc, material, 0)
                    chunkSet.add(loc.chunk)*/
                    loc.block.type = material
                }
            }
        }
        /*chunkSet.forEach { chunk ->
            val pmc = PacketPlayOutMapChunk((chunk as CraftChunk).handle, true, 65535)
            onlinePlayers.forEach { player ->
                (player as CraftPlayer).handle.playerConnection.sendPacket(pmc)
            }
        }*/

    }

    private fun spawnFeastLoot() {
        feastCenter!!.clone().add(0.0, 1.0, 0.0).block.type = Material.ENCHANTMENT_TABLE
        val chestLocations = arrayOf(
            feastCenter!!.clone().add(1.0, 1.0, 1.0),
            feastCenter!!.clone().add(-1.0, 1.0, 1.0),
            feastCenter!!.clone().add(-1.0, 1.0, -1.0),
            feastCenter!!.clone().add(1.0, 1.0, -1.0),
            feastCenter!!.clone().add(2.0, 1.0, 2.0),
            feastCenter!!.clone().add(0.0, 1.0, 2.0),
            feastCenter!!.clone().add(-2.0, 1.0, 2.0),
            feastCenter!!.clone().add(2.0, 1.0, 0.0),
            feastCenter!!.clone().add(-2.0, 1.0, 0.0),
            feastCenter!!.clone().add(2.0, 1.0, -2.0),
            feastCenter!!.clone().add(0.0, 1.0, -2.0),
            feastCenter!!.clone().add(-2.0, 1.0, -2.0)
        )
        chestLocations.forEach { it.block.type = Material.CHEST }

        //FEAST ITEMS
        val ironItems: RandomCollection<ItemStack> = RandomCollection()
        ironItems.add(1.0, ItemStack(Material.IRON_HELMET))
        ironItems.add(1.0, ItemStack(Material.IRON_CHESTPLATE))
        ironItems.add(1.0, ItemStack(Material.IRON_LEGGINGS))
        ironItems.add(1.0, ItemStack(Material.IRON_BOOTS))
        ironItems.add(1.0, ItemStack(Material.IRON_SWORD))
        ironItems.add(1.07, ItemStack(Material.IRON_PICKAXE))

        val diamondItems: RandomCollection<ItemStack> = RandomCollection()
        diamondItems.add(1.0, ItemStack(Material.DIAMOND_HELMET))
        diamondItems.add(1.0, ItemStack(Material.DIAMOND_CHESTPLATE))
        diamondItems.add(1.0, ItemStack(Material.DIAMOND_LEGGINGS))
        diamondItems.add(1.0, ItemStack(Material.DIAMOND_BOOTS))
        diamondItems.add(1.07, ItemStack(Material.DIAMOND_SWORD))

        val sizeableItems: RandomCollection<ItemStack> = RandomCollection()
        sizeableItems.add(1.0, ItemStack(Material.COOKED_BEEF))
        sizeableItems.add(1.0, ItemStack(Material.COOKED_CHICKEN))
        sizeableItems.add(1.0, ItemStack(Material.MUSHROOM_SOUP))

        val singleItems: RandomCollection<ItemStack> = RandomCollection()
        singleItems.add(1.0, ItemStack(Material.BOW))
        singleItems.add(1.0, ItemStack(Material.WEB))
        singleItems.add(1.0, ItemStack(Material.FLINT_AND_STEEL))
        singleItems.add(1.0, ItemStack(Material.TNT))
        singleItems.add(1.0, ItemStack(Material.ENDER_PEARL))
        singleItems.add(1.0, ItemStack(Material.LAVA_BUCKET))
        singleItems.add(1.0, ItemStack(Material.WATER_BUCKET))
        /*val strengthPotion = ItemStack(Material.POTION, 1, 16393.toShort())
        val meta: PotionMeta = strengthPotion.itemMeta as PotionMeta
        meta.addCustomEffect(PotionEffect(PotionEffectType.INCREASE_DAMAGE, 2500, 0), true)
        strengthPotion.setItemMeta(meta)*/
        singleItems.add(0.2, ItemStack(Material.POTION, 1, 16393.toShort()))

        val lootPool: RandomCollection<RandomCollection<ItemStack>> = RandomCollection()
        lootPool.add(21.0, ironItems)
        lootPool.add(13.0, diamondItems)
        lootPool.add(33.0, sizeableItems)
        lootPool.add(33.0, singleItems)
        for (chestLocation in chestLocations) {
            val chest = chestLocation!!.block.state as Chest
            for (i in 0 until maxItemsInChest) {
                val randomItemCollection: RandomCollection<ItemStack> = lootPool.getRandom()
                val item: ItemStack = randomItemCollection.getRandom()
                if (randomItemCollection == sizeableItems) {
                    item.amount = Random.nextInt(11) + 1
                }
                if (shouldDamageItems) {
                    if (randomItemCollection == diamondItems) {
                        val maxDurability: Int = item.type.maxDurability.toInt()
                        item.durability = (maxDurability - Random.nextInt(maxDurability / 4)).toShort()
                    }
                }
                chest.inventory.setItem(Random.nextInt(26 - 1) + 1, item)
            }
        }
    }

    private fun startCountDown() {
        runBlocking {
            launch {
                task(false, 0, 20) {
                    if (timer!!.decrementAndGet() <= 0) {
                        //CHEST SPAWNING
                        inPreparation = false
                        isFinished = true
                        feastBlocks.forEach { feastBlock: Block ->
                            feastBlock.removeMetadata(BLOCK_KEY, Manager)
                        }
                        sync {
                            announceFeast()
                            spawnFeastLoot()
                        }
                        it.cancel()
                    } else {
                        if (timer!!.get() % 60 == 0 || when (timer!!.get()) {
                                30, 15, 10, 5, 3, 2, 1 -> true
                                else -> false
                            }
                        ) {
                            announceFeast()
                        }
                    }
                }
            }
        }
    }

    private fun announceFeast() {
        broadcast("${Prefix}Feast will spawn at ${getCenterString()} ${ChatColor.GRAY}in ${getTimeString()}${ChatColor.GRAY}.")
    }

    private fun getCenterString(): String? {
        val loc = feastCenter ?: return null
        return "${ChatColor.WHITE}${loc.x}${ChatColor.DARK_GRAY}, ${ChatColor.WHITE}${loc.y}${ChatColor.DARK_GRAY}, ${ChatColor.WHITE}${loc.z}${ChatColor.DARK_GRAY}"
    }

    private fun getTimeString(): String? {
        val time = timer?.get() ?: return null
        return "${ChatColor.WHITE}${TimeConverter.stringify(time)}"
    }

    companion object {
        const val BLOCK_KEY = "FEAST_BLOCK"
    }
}