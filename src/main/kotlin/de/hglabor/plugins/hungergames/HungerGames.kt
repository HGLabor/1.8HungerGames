package de.hglabor.plugins.hungergames

import de.hglabor.plugins.hungergames.commands.*
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.arena.Arena
import de.hglabor.plugins.hungergames.game.mechanics.MechanicsGUI
import de.hglabor.plugins.hungergames.game.mechanics.implementation.*
import net.axay.kspigot.extensions.bukkit.register
import net.axay.kspigot.main.KSpigot
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.WorldCreator
import java.io.File
import java.util.*

class HungerGames : KSpigot() {
    companion object {
        lateinit var INSTANCE: HungerGames; private set
    }

    override fun load() {
        File("world/").let { file ->
            if (file.exists() && file.isDirectory) file.deleteRecursively()
        }
        INSTANCE = this
    }

    override fun startup() {
        registerListeners()
        this.server.createWorld(WorldCreator("arena"))
        registerMechanics()
        registerCommands()
        whitelistManager()
    }

    override fun shutdown() {

    }

    private fun registerCommands() {
        StartCommand.register("start")
        FeastCommand.register("feast")
        ReviveCommand.register("revive")
        ArenaTpCommand.register("arenatp")
        ArenaTpCommand.register("info")
        KitCommand.register("kit")
        getCommand("kit").apply {
            executor = KitCommand
            tabCompleter = KitCommand
        }
    }

    private fun registerListeners() {

    }

    private fun registerMechanics() {
        Arena.register()
        GameManager.enable()
        SoupHealing.register()
        PlayerTracker.register()
        KitSelector.register()
        RecraftRecipes.register()
        MechanicsGUI(
            BuildHeightLimit,
            DamageNerf,
            OreNerf,
            LapisInEnchanter,
            BlocksToInv,
            RemoveFishingRod,
            NoInvDropOnClose,
            MoreDurability,
            MushroomCowNerf,
            HungerNerf
        ).register()
    }

    private fun whitelistManager() {
        Bukkit.getOfflinePlayer(UUID.fromString("26a4fcde-de39-4ff0-8ea1-786582b7d8ee")).apply { //NORISKK
            isWhitelisted = true; isOp = true
        }
        Bukkit.getOfflinePlayer(UUID.fromString("5e492d5e-b610-4351-8fcb-b2fc4bdd3245")).apply { //BESTAUTO
            isWhitelisted = true; isOp = true
        }
        Bukkit.getOfflinePlayer(UUID.fromString("e4ccbe7c-45ef-4194-b645-851f2002de89")).apply { //MOOZIII
            isWhitelisted = true; isOp = true
        }
        Bukkit.getOfflinePlayer(UUID.fromString("50bf6931-e149-4743-9210-92cd58d85c5d")).apply { //TAITO
            isWhitelisted = true; isOp = true
        }
    }
}

val Manager by lazy { HungerGames.INSTANCE }
val PrimaryColor = ChatColor.DARK_PURPLE
val SecondaryColor = ChatColor.LIGHT_PURPLE
val Prefix = " ${ChatColor.DARK_GRAY}| ${PrimaryColor}HGLabor ${ChatColor.DARK_GRAY}» ${ChatColor.GRAY}"
