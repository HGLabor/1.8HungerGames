package de.hglabor.plugins.hungergames.scoreboard

import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Team

class Board(var updatingPeriod: Long = 20L) {
    val lines = mutableListOf<BoardLine>()
    var title = ""
        set(value) {
            field = value
            objective.displayName = value
        }
    val scoreboard = Bukkit.getScoreboardManager().newScoreboard
    val objective: Objective = scoreboard.registerNewObjective("aaa", "bbb")
    var runnable: KSpigotRunnable? = null

    init {
        objective.displaySlot = DisplaySlot.SIDEBAR
        startRunnable()
    }

    private fun startRunnable() {
        task(true, 20L, updatingPeriod) {
            updateBoard()
        }
    }

    fun updateBoard() {
        lines.filter { it.shouldUpdate }.forEach { it.update() }
    }

    fun resetBoard() {
        lines.forEach { it.unregister() }
        lines.forEach { it.register() }
    }

    fun addLine(line: Int = -1, boardLine: BoardLine) {
        if (line == -1) {
            lines.add(boardLine)
            boardLine.register()
        } else {
            lines.forEach { it.unregister() }
            lines.add(line, boardLine)
            lines.forEach { it.register() }
        }
    }

    fun addLine(line: Int = -1, textCallback: () -> String) {
        addLine(line, BoardLine(textCallback))
    }

    fun addLine(line: Int = -1, text: String) {
        addLine(line, BoardLine(text))
    }

    fun addLineBelow(textCallback: () -> String) {
        addLine(0, BoardLine(textCallback))
    }

    fun addLineBelow(text: String) {
        addLine(0, BoardLine(text))
    }

    fun getLine(line: Int) = lines.getOrNull(line)

    fun setLine(line: Int, text: String) {
        val l = getLine(line) ?: error("Line $line not found!")
        l.shouldUpdate = false
        l.set(text)
    }

    fun setLine(line: Int, textCallback: () -> String) {
        val l = getLine(line) ?: error("Line $line not found!")
        l.shouldUpdate = true
        l.set(textCallback)
    }

    fun deleteLine(boardLine: BoardLine) {
        lines -= boardLine
        boardLine.apply {
            shouldUpdate = false
            team.unregister()
            scoreboard.resetScores(entry)
        }
        resetBoard()
    }

    fun deleteLine(line: Int) {
        deleteLine(lines[line])
    }

    fun clear() {
        lines.forEach { it.unregister() }
    }

    fun setScoreboard(player: Player): Board {
        player.scoreboard = scoreboard
        return this
    }

    inner class BoardLine(var textCallback: () -> String) {
        constructor(text: String) : this({ text }) {
            shouldUpdate = false
        }

        var shouldUpdate: Boolean = true
        var team: Team = scoreboard.getTeam("placeholder") ?: scoreboard.registerNewTeam("placeholder")
        lateinit var entry: String

        fun register() {
            val index = lines.indexOf(this)
            team = scoreboard.getTeam("$index") ?: scoreboard.registerNewTeam("$index")
            entry = entry(index)
            team.addEntry(entry)
            val (pre, suf) = text.split("#")
            team.prefix = pre
            team.suffix = " $suf"
            objective.getScore(entry).score = index
        }

        fun unregister() {
            scoreboard.resetScores(entry)
        }

        fun update() {
            val (pre, suf) = text.split("#")
            team.prefix = pre
            team.suffix = " $suf"
        }

        fun set(textCallback: () -> String) {
            this.textCallback = textCallback
            shouldUpdate = true
        }

        fun set(text: String) {
            this.textCallback = { text }
            shouldUpdate = false
        }

        val text: String get() = textCallback.invoke()
    }

    private fun entry(index: Int) = "${ChatColor.values()[index]}${ChatColor.WHITE}"
}
