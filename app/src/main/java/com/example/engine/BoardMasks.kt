package com.example.engine

import com.example.model.BoardShape
import com.example.model.GridPoint
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

object BoardMasks {

    fun generateMask(shape: BoardShape, cols: Int, rows: Int): Set<GridPoint> {
        val raw = when (shape) {
            BoardShape.CLASSIC -> rectangleMask(cols, rows)
            BoardShape.DIAMOND -> diamondMask(cols, rows)
            BoardShape.PLUS -> plusMask(cols, rows)
            BoardShape.FRAME -> frameMask(cols, rows)
            BoardShape.HEART -> heartMask(cols, rows)
            BoardShape.LEAF -> leafMask(cols, rows)
            BoardShape.STAR -> starMask(cols, rows)
            BoardShape.HEXAGON -> hexagonMask(cols, rows)
            BoardShape.CROWN -> crownMask(cols, rows)
            BoardShape.HOURGLASS -> hourglassMask(cols, rows)
            BoardShape.BUTTERFLY -> butterflyMask(cols, rows)
            BoardShape.ARROW -> chevronArrowMask(cols, rows)
        }
        return if (raw.size >= 8) raw else rectangleMask(cols, rows)
    }

    private fun rectangleMask(cols: Int, rows: Int): Set<GridPoint> = buildSet {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                add(GridPoint(c, r))
            }
        }
    }

    private fun diamondMask(cols: Int, rows: Int): Set<GridPoint> = buildSet {
        val cx = (cols - 1) / 2.0
        val cy = (rows - 1) / 2.0
        val rx = (cols / 2.0).coerceAtLeast(1.0)
        val ry = (rows / 2.0).coerceAtLeast(1.0)
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val dist = abs(c - cx) / rx + abs(r - cy) / ry
                if (dist <= 1.08) add(GridPoint(c, r))
            }
        }
    }

    private fun plusMask(cols: Int, rows: Int): Set<GridPoint> = buildSet {
        val cx = (cols - 1) / 2.0
        val cy = (rows - 1) / 2.0
        val armX = cols * 0.27
        val armY = rows * 0.27
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (abs(c - cx) <= armX || abs(r - cy) <= armY) {
                    add(GridPoint(c, r))
                }
            }
        }
    }

    private fun frameMask(cols: Int, rows: Int): Set<GridPoint> = buildSet {
        val holeHalfW = (cols * 0.18).toInt().coerceAtLeast(1)
        val holeHalfH = (rows * 0.18).toInt().coerceAtLeast(1)
        val cx = cols / 2
        val cy = rows / 2
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val inHole = abs(c - cx) < holeHalfW && abs(r - cy) < holeHalfH
                val cornerCut = (c == 0 || c == cols - 1) && (r == 0 || r == rows - 1)
                if (!inHole && !cornerCut) {
                    add(GridPoint(c, r))
                }
            }
        }
    }

    private fun heartMask(cols: Int, rows: Int): Set<GridPoint> = buildSet {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                // Normalized to [-1.25, 1.25] x [-1.15, 1.30] with y pointing up
                val nx = ((c + 0.5) / cols - 0.5) * 2.55
                val ny = (0.5 - (r + 0.5) / rows) * 2.50 + 0.12
                val a = nx * nx + ny * ny - 1.0
                val inside = (a * a * a) - (nx * nx * ny * ny * ny) <= 0.08
                if (inside) add(GridPoint(c, r))
            }
        }
    }

    private fun leafMask(cols: Int, rows: Int): Set<GridPoint> = buildSet {
        val angle = Math.toRadians(-38.0)
        val cosA = cos(angle)
        val sinA = sin(angle)
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val x0 = ((c + 0.5) / cols - 0.5) * 2.2
                val y0 = ((r + 0.5) / rows - 0.5) * 2.2
                val u = x0 * cosA - y0 * sinA
                val v = x0 * sinA + y0 * cosA
                // Vesica piscis / leaf blade
                val blade = hypot(u, v - 0.55) <= 1.28 && hypot(u, v + 0.55) <= 1.28
                // Short stem at bottom-right
                val stem = u in 0.72..1.05 && abs(v) <= 0.14
                if (blade || stem) add(GridPoint(c, r))
            }
        }
    }

    private fun starMask(cols: Int, rows: Int): Set<GridPoint> = buildSet {
        val cx = (cols - 1) / 2.0
        val cy = (rows - 1) / 2.0
        val scale = minOf(cols, rows) / 2.0
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val x = (c - cx) / scale
                val y = (r - cy) / scale
                val rad = hypot(x, y)
                val theta = kotlin.math.atan2(y, x) + Math.PI / 2.0
                // 5-pointed star boundary approximation
                val starRadius = 0.72 + 0.34 * cos(5.0 * theta)
                if (rad <= starRadius) add(GridPoint(c, r))
            }
        }
    }

    private fun hexagonMask(cols: Int, rows: Int): Set<GridPoint> = buildSet {
        val cx = (cols - 1) / 2.0
        val cy = (rows - 1) / 2.0
        val rx = (cols / 2.0).coerceAtLeast(1.0)
        val ry = (rows / 2.0).coerceAtLeast(1.0)
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val x = abs(c - cx) / rx
                val y = abs(r - cy) / ry
                if (x <= 0.96 && y <= 0.96 && (x * 0.65 + y) <= 1.22) {
                    add(GridPoint(c, r))
                }
            }
        }
    }

    private fun crownMask(cols: Int, rows: Int): Set<GridPoint> = buildSet {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val nx = (c + 0.5) / cols // 0..1
                val ny = (r + 0.5) / rows // 0..1 (top to bottom)
                val base = ny >= 0.52 && nx in 0.08..0.92
                val peak1 = ny >= 0.12 && abs(nx - 0.18) <= 0.13 * (ny + 0.35)
                val peak2 = ny >= 0.06 && abs(nx - 0.50) <= 0.16 * (ny + 0.35)
                val peak3 = ny >= 0.12 && abs(nx - 0.82) <= 0.13 * (ny + 0.35)
                if (base || peak1 || peak2 || peak3) {
                    add(GridPoint(c, r))
                }
            }
        }
    }

    private fun hourglassMask(cols: Int, rows: Int): Set<GridPoint> = buildSet {
        val cx = (cols - 1) / 2.0
        val cy = (rows - 1) / 2.0
        val rx = (cols / 2.0).coerceAtLeast(1.0)
        val ry = (rows / 2.0).coerceAtLeast(1.0)
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val x = abs(c - cx) / rx
                val y = abs(r - cy) / ry
                val allowedWidth = 0.28 + 0.78 * y
                if (x <= allowedWidth) add(GridPoint(c, r))
            }
        }
    }

    private fun butterflyMask(cols: Int, rows: Int): Set<GridPoint> = buildSet {
        val cx = (cols - 1) / 2.0
        val cy = (rows - 1) / 2.0
        val rx = (cols / 2.0).coerceAtLeast(1.0)
        val ry = (rows / 2.0).coerceAtLeast(1.0)
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val x = abs(c - cx) / rx
                val y = (r - cy) / ry
                val upperWing = hypot(x - 0.52, y + 0.36) <= 0.54
                val lowerWing = hypot(x - 0.44, y - 0.38) <= 0.46
                val body = x <= 0.18 && abs(y) <= 0.74
                if (upperWing || lowerWing || body) add(GridPoint(c, r))
            }
        }
    }

    private fun chevronArrowMask(cols: Int, rows: Int): Set<GridPoint> = buildSet {
        val cx = (cols - 1) / 2.0
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val ny = (r + 0.5) / rows
                val nxDist = abs(c - cx) / (cols / 2.0)
                val head = ny <= 0.50 && nxDist <= (ny * 2.08 + 0.10)
                val shaft = ny > 0.50 && nxDist <= 0.46
                if (head || shaft) add(GridPoint(c, r))
            }
        }
    }
}
