package de.hglabor.plugins.hungergames

import de.hglabor.plugins.hungergames.commands.FeastCommand
import de.hglabor.plugins.hungergames.commands.KitCommand
import de.hglabor.plugins.hungergames.commands.StartCommand
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.mechanics.*
import de.hglabor.plugins.hungergames.game.mechanics.SoupHealing.register
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.extensions.bukkit.register
import net.axay.kspigot.main.KSpigot
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import java.util.*

class HungerGames: KSpigot() {
    companion object {
        lateinit var INSTANCE: HungerGames; private set
    }

    override fun load() {
        INSTANCE = this
    }

    override fun startup() {
        registerMechanics()
        registerCommands()
        whitelistManager()
    }

    override fun shutdown() {

    }

    private fun registerCommands() {
        StartCommand.register("start")
        FeastCommand.register("feast")
        KitCommand.register("kit")
        getCommand("kit").apply {
            executor = KitCommand
            tabCompleter = KitCommand
        }
    }

    private fun registerListeners() {

    }

    private fun registerMechanics() {
        GameManager.enable()
        SoupHealing.register()
        PlayerTracker.register()
        BuildHeightLimit.register()
        DamageNerf.register()
        OreNerf.register()
        LapisInEnchanter.register()
        KitSelector.register()
        RecraftRecipes.register()
        BlocksToInv.register()
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
val PrimaryColor = ChatColor.AQUA
val SecondaryColor = ChatColor.DARK_GRAY
val Prefix = " ${KColors.DARKGRAY}| ${PrimaryColor}HGLabor ${KColors.DARKGRAY}» ${KColors.GRAY}"
