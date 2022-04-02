package de.hglabor.plugins.hungergames.scoreboard

import org.bukkit.entity.Player


class BoardBuilder(val board: Board) {
    var lineBuilder = LineBuilder()

    var title: String
        set(value) {
            board.title = value
            board.objective.displayName = value
        }
        get() = board.title

    var period: Long
        set(value) {
            board.updatingPeriod = value
        }
        get() = board.updatingPeriod

    inline fun content(crossinline callback: LineBuilder.() -> Unit) {
        lineBuilder = LineBuilder().apply(callback)
    }

    fun invoke(reverse: Boolean) {
        if (reverse) reverseLines()
        board.lines.forEach { it.register() }
    }

    private fun reverseLines() {
        board.lines.reverse()
    }

    inner class LineBuilder {
        operator fun String.unaryPlus() {
            board.lines += board.BoardLine(this)
        }

        operator fun (() -> String).unaryPlus() {
            board.lines += board.BoardLine(this)
        }
    }
}

inline fun Player.setScoreboard(updatingPeriod: Long = 20, bottomToTop: Boolean = true, crossinline builder: BoardBuilder.() -> Unit): Board {
    return Board(updatingPeriod).apply {
        BoardBuilder(this).apply(builder).invoke(bottomToTop)
    }.setScoreboard(this)
}

fun Player.setScoreboard(board: Board) {
    board.setScoreboard(this)
}
