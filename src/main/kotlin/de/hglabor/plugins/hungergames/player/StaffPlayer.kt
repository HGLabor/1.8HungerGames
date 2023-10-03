package de.hglabor.plugins.hungergames.player

import de.hglabor.plugins.hungergames.staff.StaffMode
import org.bukkit.ChatColor
import org.bukkit.GameMode
import java.util.*

class StaffPlayer(uuid: UUID, name: String) : HGPlayer(uuid, name) {
    var isStaffMode: Boolean = false
    var isBuildMode: Boolean = false
    var isVisible: Boolean = false
    var canCollectItems: Boolean = false

    fun toggleStaffMode() {
        if (isStaffMode) {
            bukkitPlayer?.sendMessage("${StaffMode.prefix}${ChatColor.LIGHT_PURPLE}Staffmode ${ChatColor.DARK_GRAY}» ${ChatColor.GREEN}Restored staff inventory")
            StaffMode.setStaffInventory(this)
            return
        }
        isStaffMode = !isStaffMode
        bukkitPlayer?.sendMessage("${StaffMode.prefix}${ChatColor.LIGHT_PURPLE}Staffmode ${ChatColor.DARK_GRAY}» ${isStaffMode.text}")
        status = PlayerStatus.SPECTATOR
        bukkitPlayer?.gameMode = GameMode.CREATIVE
        StaffMode.setStaffInventory(this)
        StaffMode.addScoreboardLines(this)
        StaffMode.hide(this)
    }

    fun toggleBuildMode() {
        isBuildMode = !isBuildMode
        bukkitPlayer?.sendMessage("${StaffMode.prefix}${ChatColor.LIGHT_PURPLE}Buildmode ${ChatColor.DARK_GRAY}» ${isBuildMode.text}")
    }

    fun toggleCollectingItems() {
        canCollectItems = !canCollectItems
        bukkitPlayer?.sendMessage("${StaffMode.prefix}${ChatColor.LIGHT_PURPLE}Collecting Items ${ChatColor.DARK_GRAY}» ${canCollectItems.text}")
    }

    fun toggleVisibility() {
        isVisible = !isVisible
        bukkitPlayer?.sendMessage("${StaffMode.prefix}${ChatColor.LIGHT_PURPLE}Visibility ${ChatColor.DARK_GRAY}» ${isVisible.text}")

        if (isVisible) StaffMode.show(this)
        else StaffMode.hide(this)

    }


    val Boolean.text get() = if (this) "${ChatColor.GREEN}Enabled" else "${ChatColor.RED}Disabled"
}