package com.example

import com.example.engine.PuzzleGenerator
import com.example.model.GridPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class PuzzleGeneratorTest {

    @Test
    fun levels1Through60_are100PercentSolvableAndValid() {
        for (levelNum in 1..60) {
            val level = PuzzleGenerator.generateLevel(levelNum)
            assertTrue("Level $levelNum should have at least 3 arrows", level.arrows.size >= 3)

            // Verify zero overlapping cells & valid orthogonal adjacency for every arrow
            val seenCells = HashSet<GridPoint>()
            for (arrow in level.arrows) {
                assertTrue("Arrow in level $levelNum must have >= 2 cells", arrow.cells.size >= 2)
                for (i in 0 until arrow.cells.size) {
                    val pt = arrow.cells[i]
                    assertTrue("Cell $pt must be in mask", pt in level.validCells)
                    assertTrue("Duplicate cell $pt in level $levelNum", seenCells.add(pt))
                    if (i > 0) {
                        val prev = arrow.cells[i - 1]
                        val manhattan = kotlin.math.abs(pt.col - prev.col) + kotlin.math.abs(pt.row - prev.row)
                        assertEquals("Consecutive cells must be adjacent", 1, manhattan)
                    }
                }
            }

            // Verify mathematical 100% solvability via Kahn's peeling solver
            assertTrue("Level $levelNum must be 100% solvable", PuzzleGenerator.verifySolvable(level))

            // Verify hint finder always finds a valid free arrow on initial state
            val hint = PuzzleGenerator.findBestHintArrow(level.arrows, level.cols, level.rows)
            assertNotNull("Level $levelNum must have at least one initial free arrow", hint)
        }
    }

    @Test
    fun dailyChallengesAcrossYear_areSolvableAndDeterministic() {
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.JANUARY, 1)
        for (day in 0 until 30) {
            val levelA = PuzzleGenerator.generateDailyLevel(cal)
            val levelB = PuzzleGenerator.generateDailyLevel(cal)
            assertEquals(levelA.arrows, levelB.arrows)
            assertTrue("Daily ${levelA.dailyDateKey} must be solvable", PuzzleGenerator.verifySolvable(levelA))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    @Test
    fun difficultyIncreasesAcrossProgression() {
        val level1 = PuzzleGenerator.generateLevel(1)
        val level15 = PuzzleGenerator.generateLevel(15)
        val level45 = PuzzleGenerator.generateLevel(45)
        assertTrue(
            "Later levels should contain more arrows (${level1.arrows.size} < ${level15.arrows.size} < ${level45.arrows.size})",
            level1.arrows.size < level15.arrows.size && level15.arrows.size < level45.arrows.size,
        )
    }
}
