package de.hglabor.plugins.hungergames.staff

import de.hglabor.plugins.hungergames.player.StaffPlayer
import de.hglabor.plugins.hungergames.staff.module.implementation.*
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.onlinePlayers
import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Field
import java.lang.reflect.Method

object StaffMode {
    val modules = listOf(
        BuildMode,
        CollectItems,
        Invsee,
        PlayerInformation,
        RandomTP,
        Visibility
    )

    init {
        StaffCommand
    }

    val prefix = " ${KColors.DARKGRAY}| ${KColors.DARKPURPLE}Staff ${KColors.DARKGRAY}» ${KColors.GRAY}"

    fun hide(staffPlayer: StaffPlayer) {
        val player = staffPlayer.bukkitPlayer ?: return
        staffPlayer.isVisible = false
        onlinePlayers.forEach { on ->
            on.hidePlayer(player)
        }
    }

    fun show(staffPlayer: StaffPlayer) {
        val player = staffPlayer.bukkitPlayer ?: return
        staffPlayer.isVisible = true
        onlinePlayers.forEach { on ->
            on.showPlayer(player)
        }
    }

    fun setStaffInventory(staffPlayer: StaffPlayer) {
        staffPlayer.bukkitPlayer?.inventory?.apply {
            clear()
            setItem(0, RandomTP.item)
            setItem(1, Invsee.item)
            setItem(2, PlayerInformation.item)
            setItem(6, CollectItems.item)
            setItem(7, Visibility.item)
            setItem(8, BuildMode.item)
        }
    }

    fun addScoreboardLines(staffPlayer: StaffPlayer) {
        /*fun tpsString(): String {
            val tps = getTps()
            val color = when {
                tps >= 17.0 -> ChatColor.GREEN
                tps >= 13.0 -> ChatColor.DARK_GREEN
                tps >= 10.0 -> ChatColor.YELLOW
                tps >= 7.0 -> ChatColor.GOLD
                else -> ChatColor.RED
            }
            val roundedValue = "%.1f".format(tps)
            return "${color}${roundedValue}"
        }*/

        staffPlayer.board?.apply {
            addLineBelow("${ChatColor.DARK_PURPLE}${ChatColor.BOLD}Staff:")
            addLineBelow { "  ${ChatColor.LIGHT_PURPLE}${ChatColor.BOLD}Visible:# ${if (staffPlayer.isVisible) "${ChatColor.GREEN}Yes" else "${ChatColor.RED}No"}" }
            addLineBelow { "  ${ChatColor.LIGHT_PURPLE}${ChatColor.BOLD}Build:# ${if (staffPlayer.isBuildMode) "${ChatColor.GREEN}Yes" else "${ChatColor.RED}No"}" }
            addLineBelow { "  ${ChatColor.LIGHT_PURPLE}${ChatColor.BOLD}Pick up:# ${if (staffPlayer.canCollectItems) "${ChatColor.GREEN}Yes" else "${ChatColor.RED}No"}" }
        }
    }

    init {
        listen<PlayerDropItemEvent> {
            if (it.itemDrop.itemStack.isStaffItem) it.isCancelled = true
        }
    }
}

val ItemStack?.isStaffItem: Boolean
    get() {
        if (this == null) return false
        if (type == Material.AIR) return false
        if (itemMeta == null) return false
        if (itemMeta.lore == null || itemMeta.lore.isEmpty()) return false
        return itemMeta.lore.first() == "${ChatColor.DARK_PURPLE}Staff Item"
    }