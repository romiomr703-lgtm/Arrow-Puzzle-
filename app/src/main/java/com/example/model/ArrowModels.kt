package com.example.model

import androidx.compose.runtime.Immutable

@Immutable
data class GridPoint(val col: Int, val row: Int) {
    operator fun plus(dir: Direction): GridPoint = GridPoint(col + dir.dx, row + dir.dy)
}

enum class Direction(val dx: Int, val dy: Int) {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    companion object {
        fun fromDelta(dx: Int, dy: Int): Direction = when {
            dx == 0 && dy < 0 -> UP
            dx == 0 && dy > 0 -> DOWN
            dx < 0 && dy == 0 -> LEFT
            dx > 0 && dy == 0 -> RIGHT
            else -> RIGHT
        }
    }
}

@Immutable
data class Arrow(
    val id: Int,
    val cells: List<GridPoint>,
    val direction: Direction,
    val colorIndex: Int,
) {
    val head: GridPoint get() = cells.last()
    val tail: GridPoint get() = cells.first()
}

enum class Difficulty(val label: String) {
    EASY("Easy"),
    NORMAL("Normal"),
    HARD("Hard"),
    EXPERT("Expert"),
    MASTER("Master"),
}

enum class BoardShape(val displayName: String) {
    CLASSIC("Classic Grid"),
    DIAMOND("Diamond"),
    PLUS("Crossroads"),
    FRAME("Courtyard"),
    HEART("Heart"),
    LEAF("Botanical Leaf"),
    STAR("Starlight"),
    HEXAGON("Hexagon"),
    CROWN("Crown"),
    HOURGLASS("Hourglass"),
    BUTTERFLY("Butterfly"),
    ARROW("Chevron"),
}

@Immutable
data class PuzzleLevel(
    val levelNumber: Int,
    val cols: Int,
    val rows: Int,
    val difficulty: Difficulty,
    val shape: BoardShape,
    val validCells: Set<GridPoint>,
    val arrows: List<Arrow>,
    val isDaily: Boolean = false,
    val dailyDateKey: String? = null,
)

enum class GameThemePalette(val storageKey: String) {
    CLASSIC_INK("classic_ink"),
    PRISMATIC("prismatic"),
    MIDNIGHT_GLOW("midnight_glow");

    companion object {
        fun fromKey(key: String?): GameThemePalette =
            entries.firstOrNull { it.storageKey == key } ?: CLASSIC_INK
    }
}

@Immutable
data class LevelSummary(
    val levelNumber: Int,
    val difficulty: Difficulty,
    val shape: BoardShape,
    val unlocked: Boolean,
    val completed: Boolean,
    val bestStars: Int,
    val bestTimeSeconds: Int,
)
