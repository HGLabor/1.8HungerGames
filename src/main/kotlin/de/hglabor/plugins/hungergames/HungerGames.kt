package de.hglabor.plugins.hungergames

import de.hglabor.plugins.hungergames.commands.*
import de.hglabor.plugins.hungergames.game.GameManager
import de.hglabor.plugins.hungergames.game.mechanics.MechanicsGUI
import de.hglabor.plugins.hungergames.game.mechanics.MechanicsManager
import de.hglabor.plugins.hungergames.game.mechanics.implementation.KitSelector
import de.hglabor.plugins.hungergames.game.mechanics.implementation.PlayerTracker
import de.hglabor.plugins.hungergames.game.mechanics.implementation.RecraftRecipes
import de.hglabor.plugins.hungergames.game.mechanics.implementation.SoupHealing
import net.axay.kspigot.extensions.bukkit.register
import net.axay.kspigot.main.KSpigot
import org.bukkit.ChatColor
import org.bukkit.WorldCreator
import java.io.File

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
    }

    override fun shutdown() {

    }

    private fun registerCommands() {
        StartCommand.register("start")
        FeastCommand.register("feast")
        ReviveCommand.register("revive")
        ArenaTpCommand.register("arenatp")
        InfoCommand.register("info")
        ListCommand.register("list")
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
        KitSelector.register()
        RecraftRecipes.register()
        MechanicsGUI(MechanicsManager.mechanics).register()
    }
}

val Manager by lazy { HungerGames.INSTANCE }
val PrimaryColor = ChatColor.DARK_PURPLE
val SecondaryColor = ChatColor.LIGHT_PURPLE
val Prefix = " ${ChatColor.DARK_GRAY}| ${PrimaryColor}HGLabor ${ChatColor.DARK_GRAY}» ${ChatColor.GRAY}"
