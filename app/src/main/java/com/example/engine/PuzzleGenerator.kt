package com.example.engine

import com.example.model.Arrow
import com.example.model.BoardShape
import com.example.model.Difficulty
import com.example.model.Direction
import com.example.model.GridPoint
import com.example.model.PuzzleLevel
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

data class LevelSpec(
    val levelNumber: Int,
    val cols: Int,
    val rows: Int,
    val difficulty: Difficulty,
    val shape: BoardShape,
    val minArrowLen: Int,
    val maxArrowLen: Int,
    val bendChance: Float,
    val interlockBias: Float,
)

data class CollisionInfo(
    val arrowId: Int,
    val blockingArrowId: Int,
    val blockingCell: GridPoint,
    val distanceCells: Int,
)

object PuzzleGenerator {

    private val allShapes = listOf(
        BoardShape.CLASSIC,
        BoardShape.DIAMOND,
        BoardShape.PLUS,
        BoardShape.FRAME,
        BoardShape.HEART,
        BoardShape.LEAF,
        BoardShape.STAR,
        BoardShape.HEXAGON,
        BoardShape.CROWN,
        BoardShape.HOURGLASS,
        BoardShape.BUTTERFLY,
        BoardShape.ARROW,
    )

    fun specForLevel(levelNumber: Int): LevelSpec {
        val n = levelNumber.coerceAtLeast(1)
        val base = when {
            n == 1 -> LevelSpec(n, 4, 4, Difficulty.EASY, BoardShape.CLASSIC, 2, 4, 0.35f, 0.25f)
            n == 2 -> LevelSpec(n, 5, 5, Difficulty.EASY, BoardShape.CLASSIC, 2, 4, 0.45f, 0.35f)
            n == 3 -> LevelSpec(n, 5, 6, Difficulty.EASY, BoardShape.CLASSIC, 2, 5, 0.50f, 0.45f)
            n == 4 -> LevelSpec(n, 6, 6, Difficulty.EASY, BoardShape.DIAMOND, 2, 5, 0.52f, 0.50f)
            n == 5 -> LevelSpec(n, 6, 6, Difficulty.EASY, BoardShape.PLUS, 2, 5, 0.55f, 0.55f)
            n <= 8 -> {
                val shapes = listOf(BoardShape.CLASSIC, BoardShape.FRAME, BoardShape.HEXAGON)
                LevelSpec(n, 6, 7, Difficulty.EASY, shapes[(n - 6) % shapes.size], 2, 6, 0.55f, 0.58f)
            }
            n <= 15 -> {
                val shapes = listOf(
                    BoardShape.CLASSIC,
                    BoardShape.HEART,
                    BoardShape.ARROW,
                    BoardShape.DIAMOND,
                    BoardShape.FRAME,
                    BoardShape.LEAF,
                    BoardShape.CLASSIC,
                )
                val cols = if (n <= 11) 7 else 8
                val rows = if (n <= 11) 8 else 9
                LevelSpec(n, cols, rows, Difficulty.NORMAL, shapes[(n - 9) % shapes.size], 3, 7, 0.62f, 0.68f)
            }
            n <= 25 -> {
                val shapes = listOf(
                    BoardShape.CLASSIC,
                    BoardShape.HEART,
                    BoardShape.STAR,
                    BoardShape.LEAF,
                    BoardShape.HOURGLASS,
                    BoardShape.CROWN,
                    BoardShape.BUTTERFLY,
                    BoardShape.HEXAGON,
                    BoardShape.FRAME,
                    BoardShape.CLASSIC,
                )
                val cols = if (n <= 20) 9 else 10
                val rows = if (n <= 20) 10 else 11
                LevelSpec(n, cols, rows, Difficulty.HARD, shapes[(n - 16) % shapes.size], 3, 9, 0.68f, 0.78f)
            }
            n <= 40 -> {
                val shape = allShapes[(n * 5 + 3) % allShapes.size]
                val cols = if (n <= 32) 10 else 11
                val rows = if (n <= 32) 12 else 13
                LevelSpec(n, cols, rows, Difficulty.EXPERT, shape, 3, 10, 0.72f, 0.85f)
            }
            else -> {
                val shape = allShapes[(n * 7 + 1) % allShapes.size]
                val tier = ((n - 41) / 15).coerceAtMost(2)
                val cols = 12 + tier
                val rows = 14 + tier
                LevelSpec(n, cols, rows, Difficulty.MASTER, shape, 4, 11, 0.75f, 0.90f)
            }
        }
        // Sculpted silhouettes cover ~55%-72% of their bounding box; scale dimensions so they stay lush & dense
        val shapeBonus = when (base.shape) {
            BoardShape.CLASSIC -> 0
            BoardShape.HEXAGON, BoardShape.FRAME -> 1
            BoardShape.DIAMOND, BoardShape.HEART, BoardShape.LEAF, BoardShape.ARROW -> 2
            BoardShape.PLUS, BoardShape.STAR, BoardShape.CROWN, BoardShape.HOURGLASS, BoardShape.BUTTERFLY -> 3
        }
        return base.copy(
            cols = base.cols + shapeBonus,
            rows = base.rows + shapeBonus,
        )
    }

    fun generateLevel(levelNumber: Int): PuzzleLevel {
        val spec = specForLevel(levelNumber)
        val seed = (levelNumber.toLong() * 7919L) xor 0x5A3C91E7L
        return buildPuzzle(spec, seed, isDaily = false, dailyDateKey = null)
    }

    fun todayDateKey(calendar: Calendar = Calendar.getInstance()): String {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
    }

    fun generateDailyLevel(calendar: Calendar = Calendar.getInstance()): PuzzleLevel {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val dateKey = String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
        val daySeed = (year.toLong() * 10000L + month * 100L + day) * 2654435761L
        val iconicShapes = listOf(
            BoardShape.HEART,
            BoardShape.LEAF,
            BoardShape.STAR,
            BoardShape.CROWN,
            BoardShape.BUTTERFLY,
            BoardShape.DIAMOND,
            BoardShape.HOURGLASS,
        )
        val shape = iconicShapes[dayOfYear % iconicShapes.size]
        val spec = LevelSpec(
            levelNumber = 0,
            cols = 9,
            rows = 11,
            difficulty = Difficulty.HARD,
            shape = shape,
            minArrowLen = 4,
            maxArrowLen = 11,
            bendChance = 0.68f,
            interlockBias = 0.80f,
        )
        return buildPuzzle(spec, daySeed, isDaily = true, dailyDateKey = dateKey)
    }

    private fun buildPuzzle(
        spec: LevelSpec,
        seed: Long,
        isDaily: Boolean,
        dailyDateKey: String?,
    ): PuzzleLevel {
        val rng = Random(seed)
        val mask = BoardMasks.generateMask(spec.shape, spec.cols, spec.rows)
        val occupied = mutableMapOf<GridPoint, Int>() // cell -> arrowId
        val arrows = mutableListOf<Arrow>()

        // Dependency DAG: blocks[id] = set of arrowIds that lie in front of arrow `id`
        val blocks = mutableMapOf<Int, MutableSet<Int>>()
        val blockedBy = mutableMapOf<Int, MutableSet<Int>>()

        fun rayCells(head: GridPoint, dir: Direction): List<GridPoint> {
            val result = mutableListOf<GridPoint>()
            var cur = head + dir
            while (cur.col in 0 until spec.cols && cur.row in 0 until spec.rows) {
                result.add(cur)
                cur = cur + dir
            }
            return result
        }

        // Check if adding edges newId -> outSet and inSet -> newId creates a cycle in the DAG.
        // A cycle exists iff some node in outSet can already reach some node in inSet.
        fun wouldCreateCycle(outSet: Set<Int>, inSet: Set<Int>): Boolean {
            if (outSet.isEmpty() || inSet.isEmpty()) return false
            for (o in outSet) {
                if (o in inSet) return true
            }
            val visited = HashSet<Int>()
            val queue = ArrayDeque<Int>()
            for (o in outSet) {
                if (visited.add(o)) queue.add(o)
            }
            while (queue.isNotEmpty()) {
                val cur = queue.removeFirst()
                val nexts = blocks[cur] ?: continue
                for (nxt in nexts) {
                    if (nxt in inSet) return true
                    if (visited.add(nxt)) queue.add(nxt)
                }
            }
            return false
        }

        fun findIncomingBlockers(cells: List<GridPoint>): Set<Int> {
            val cellSet = cells.toHashSet()
            val incoming = mutableSetOf<Int>()
            for (existing in arrows) {
                var cur = existing.head + existing.direction
                while (cur.col in 0 until spec.cols && cur.row in 0 until spec.rows) {
                    if (cur in cellSet) {
                        incoming.add(existing.id)
                        break
                    }
                    cur = cur + existing.direction
                }
            }
            return incoming
        }

        fun tryBuildArrowFrom(start: GridPoint, desiredLen: Int): Pair<List<GridPoint>, Direction>? {
            val path = mutableListOf(start)
            val pathSet = hashSetOf(start)
            var lastDir: Direction? = null

            while (path.size < desiredLen) {
                val cur = path.last()
                val dirs = Direction.entries.filter { dir ->
                    val next = cur + dir
                    next in mask && next !in occupied && next !in pathSet
                }
                if (dirs.isEmpty()) break

                val chosenDir = if (lastDir != null && lastDir in dirs) {
                    val turns = dirs.filter { it != lastDir }
                    if (turns.isNotEmpty() && rng.nextFloat() < spec.bendChance) {
                        turns[rng.nextInt(turns.size)]
                    } else {
                        lastDir
                    }
                } else {
                    dirs[rng.nextInt(dirs.size)]
                }

                val next = cur + chosenDir
                path.add(next)
                pathSet.add(next)
                lastDir = chosenDir
            }

            if (path.size < 2) return null

            // Consider both orientations (forward and reversed) so we have two head/direction choices
            val candidates = listOf(path, path.reversed()).shuffled(rng)
            var bestValid: Pair<List<GridPoint>, Direction>? = null
            var bestScore = Int.MIN_VALUE

            for (cand in candidates) {
                // Allow trimming up to 2 cells off the head if needed to avoid self-ray or cycle
                val maxTrim = (cand.size - 2).coerceAtMost(3)
                for (trim in 0..maxTrim) {
                    val trimmed = if (trim == 0) cand else cand.subList(0, cand.size - trim)
                    if (trimmed.size < 2) continue
                    val neck = trimmed[trimmed.size - 2]
                    val head = trimmed.last()
                    val dir = Direction.fromDelta(head.col - neck.col, head.row - neck.row)
                    val ray = rayCells(head, dir)
                    val trimmedSet = trimmed.toHashSet()

                    // Must NEVER point into itself
                    if (ray.any { it in trimmedSet }) continue

                    val outSet = mutableSetOf<Int>()
                    for (rc in ray) {
                        val occ = occupied[rc]
                        if (occ != null) outSet.add(occ)
                    }
                    val inSet = findIncomingBlockers(trimmed)

                    if (!wouldCreateCycle(outSet, inSet)) {
                        // Prefer longer, well-interlocked arrows based on difficulty
                        val interlockScore = (outSet.size + inSet.size) * (spec.interlockBias * 10).toInt()
                        val bendCount = countBends(trimmed)
                        val score = trimmed.size * 5 + bendCount * 4 + interlockScore
                        if (score > bestScore) {
                            bestScore = score
                            bestValid = trimmed to dir
                        }
                        break // Found valid orientation at minimal trim
                    }
                }
            }
            return bestValid
        }

        // Order start candidates from interior/mixed outward so arrows weave organically
        val freeCells = mask.toMutableList()
        val maxColorCount = 6
        var nextId = 1
        var passesWithoutProgress = 0

        while (freeCells.isNotEmpty() && passesWithoutProgress < 2) {
            var placedThisPass = 0
            val order = freeCells.shuffled(rng)

            for (cell in order) {
                if (cell in occupied) continue
                // Skip isolated single cells with no unoccupied neighbors
                val hasFreeNeighbor = Direction.entries.any { d ->
                    val nb = cell + d
                    nb in mask && nb !in occupied
                }
                if (!hasFreeNeighbor) continue

                val targetLen = rng.nextInt(spec.minArrowLen, spec.maxArrowLen + 1)
                val built = tryBuildArrowFrom(cell, targetLen)
                    ?: tryBuildArrowFrom(cell, 3)
                    ?: tryBuildArrowFrom(cell, 2)

                if (built != null) {
                    val (cells, dir) = built
                    val id = nextId++

                    // Pick a vibrant color index that contrasts with adjacent arrows!
                    val neighborColors = mutableSetOf<Int>()
                    for (pt in cells) {
                        for (d in Direction.entries) {
                            val nbId = occupied[pt + d]
                            if (nbId != null) {
                                arrows.firstOrNull { it.id == nbId }?.let { neighborColors.add(it.colorIndex) }
                            }
                        }
                    }
                    val availableColors = (0 until maxColorCount).filter { it !in neighborColors }
                    val colorIndex = if (availableColors.isNotEmpty()) {
                        availableColors[rng.nextInt(availableColors.size)]
                    } else {
                        id % maxColorCount
                    }

                    val arrow = Arrow(
                        id = id,
                        cells = cells,
                        direction = dir,
                        colorIndex = colorIndex,
                    )
                    arrows.add(arrow)
                    for (pt in cells) occupied[pt] = id

                    // Update DAG edges
                    val outSet = mutableSetOf<Int>()
                    for (rc in rayCells(arrow.head, arrow.direction)) {
                        val occ = occupied[rc]
                        if (occ != null && occ != id) outSet.add(occ)
                    }
                    val inSet = findIncomingBlockers(cells).filter { it != id }.toSet()

                    blocks[id] = outSet
                    blockedBy[id] = inSet.toMutableSet()
                    for (o in outSet) {
                        blockedBy.getOrPut(o) { mutableSetOf() }.add(id)
                    }
                    for (inc in inSet) {
                        blocks.getOrPut(inc) { mutableSetOf() }.add(id)
                    }

                    placedThisPass++
                }
            }

            freeCells.removeAll { it in occupied }
            if (placedThisPass == 0) passesWithoutProgress++ else passesWithoutProgress = 0
        }

        return PuzzleLevel(
            levelNumber = spec.levelNumber,
            cols = spec.cols,
            rows = spec.rows,
            difficulty = spec.difficulty,
            shape = spec.shape,
            validCells = mask,
            arrows = arrows,
            isDaily = isDaily,
            dailyDateKey = dailyDateKey,
        )
    }

    private fun countBends(cells: List<GridPoint>): Int {
        if (cells.size < 3) return 0
        var bends = 0
        var prevDir = Direction.fromDelta(cells[1].col - cells[0].col, cells[1].row - cells[0].row)
        for (i in 2 until cells.size) {
            val d = Direction.fromDelta(cells[i].col - cells[i - 1].col, cells[i].row - cells[i - 1].row)
            if (d != prevDir) bends++
            prevDir = d
        }
        return bends
    }

    /**
     * Returns null if [arrow] has a clear ray off the board, or [CollisionInfo] for the closest blocker.
     */
    fun findBlockingCollision(
        arrow: Arrow,
        remainingArrows: List<Arrow>,
        cols: Int,
        rows: Int,
    ): CollisionInfo? {
        val occupiedByOther = HashMap<GridPoint, Int>()
        for (other in remainingArrows) {
            if (other.id == arrow.id) continue
            for (pt in other.cells) {
                occupiedByOther[pt] = other.id
            }
        }
        var cur = arrow.head + arrow.direction
        var dist = 1
        while (cur.col in 0 until cols && cur.row in 0 until rows) {
            val blockerId = occupiedByOther[cur]
            if (blockerId != null) {
                return CollisionInfo(
                    arrowId = arrow.id,
                    blockingArrowId = blockerId,
                    blockingCell = cur,
                    distanceCells = dist,
                )
            }
            cur = cur + arrow.direction
            dist++
        }
        return null
    }

    /**
     * Picks the most helpful currently-unblocked arrow (one that frees the most other arrows).
     */
    fun findBestHintArrow(
        remainingArrows: List<Arrow>,
        cols: Int,
        rows: Int,
    ): Arrow? {
        val freeArrows = remainingArrows.filter { arrow ->
            findBlockingCollision(arrow, remainingArrows, cols, rows) == null
        }
        if (freeArrows.isEmpty()) return null
        if (freeArrows.size == 1) return freeArrows.first()

        // Prefer the free arrow that directly unblocks the most other arrows
        return freeArrows.maxByOrNull { candidate ->
            val afterRemoval = remainingArrows.filter { it.id != candidate.id }
            afterRemoval.count { other ->
                findBlockingCollision(other, afterRemoval, cols, rows) == null
            } * 10 + candidate.cells.size
        }
    }

    /**
     * Verifies via Kahn's peeling algorithm that the puzzle is 100% solvable.
     */
    fun verifySolvable(level: PuzzleLevel): Boolean {
        val remaining = level.arrows.toMutableList()
        while (remaining.isNotEmpty()) {
            val free = remaining.firstOrNull { arrow ->
                findBlockingCollision(arrow, remaining, level.cols, level.rows) == null
            } ?: return false
            remaining.remove(free)
        }
        return true
    }
}
