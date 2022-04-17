package de.hglabor.plugins.hungergames.utils

import de.hglabor.plugins.hungergames.Manager
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import org.bukkit.entity.Entity
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.metadata.FixedMetadataValue
import java.util.*

fun Entity.mark(data: String) = setMetadata(data, FixedMetadataValue(Manager, ""))
fun Entity.unmark(data: String) = removeMetadata(data, Manager)
fun Entity.hasMark(data: String) = hasMetadata(data)

// EntityDamage
object EntityDamageUtils {
    class CancelFallDamage(uuid: UUID, ticks: Long, val reallowAfterFalldamage: Boolean = false) {
        var task: KSpigotRunnable? = null

        init {
            task = task(true, ticks, howOften = 1, endCallback = {
                fallDamage.remove(uuid)
            }) { }
        }
    }

    val fallDamage = mutableMapOf<UUID, CancelFallDamage>()

    init {
        listen<EntityDamageEvent> {
            when (it.cause) {
                EntityDamageEvent.DamageCause.FALL -> {
                    if (!it.entity.allowsFalldamage) {
                        it.isCancelled = true

                        if (fallDamage[it.entity.uniqueId]?.reallowAfterFalldamage == true) {
                            it.entity.allowFalldamage()
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

fun Entity.cancelFalldamage(ticks: Long, reallowAfterFalldamage: Boolean = false) {
    this.allowFalldamage()
    EntityDamageUtils.fallDamage[uniqueId] = EntityDamageUtils.CancelFallDamage(uniqueId, ticks, reallowAfterFalldamage)
}

fun Entity.allowFalldamage() {
    EntityDamageUtils.fallDamage[uniqueId]?.task?.cancel()
    EntityDamageUtils.fallDamage.remove(uniqueId)
}

val Entity.allowsFalldamage: Boolean
    get() = !EntityDamageUtils.fallDamage.containsKey(uniqueId)






