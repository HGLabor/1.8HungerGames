package de.hglabor.plugins.hungergames

import de.hglabor.plugins.hungergames.game.GameManager
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.main.KSpigot
import org.bukkit.ChatColor

class HungerGames: KSpigot() {
    companion object {
        lateinit var INSTANCE: HungerGames; private set
    }

    override fun load() {
        INSTANCE = this
    }

    override fun startup() {
        registerMechanics()
    }

    override fun shutdown() {

    }

    fun registerCommands() {

    }

    fun registerListeners() {

    }

    fun registerMechanics() {
        GameManager.enable()
        Bukkit.pluginManager.registerEvents(Soup(), this)
    }

val Manager by lazy { HungerGames.INSTANCE }
val PrimaryColor = ChatColor.DARK_PURPLE
val SecondaryColor = ChatColor.LIGHT_PURPLE
val Prefix = " ${KColors.DARKGRAY}| ${PrimaryColor}HGLabor ${KColors.DARKGRAY}» ${KColors.GRAY}"
