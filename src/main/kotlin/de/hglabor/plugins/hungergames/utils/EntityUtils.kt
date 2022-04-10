package de.hglabor.plugins.hungergames.utils

import de.hglabor.plugins.hungergames.Manager
import org.bukkit.entity.Entity
import org.bukkit.metadata.FixedMetadataValue

fun Entity.mark(data: String) = setMetadata(data, FixedMetadataValue(Manager, ""))
fun Entity.unmark(data: String) = removeMetadata(data, Manager)
fun Entity.hasMark(data: String) = hasMetadata(data)



