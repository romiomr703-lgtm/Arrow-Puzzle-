package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.IntSize
import com.example.model.Arrow
import com.example.model.GridPoint
import com.example.model.PuzzleLevel
import com.example.ui.theme.ArrowBoardPalette
import com.example.ui.theme.LocalArrowBoardPalette
import com.example.viewmodel.BlockedCollisionAnim
import com.example.viewmodel.EscapingArrowAnim
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin

private const val ESCAPE_DURATION_NANOS = 340_000_000L
private const val COLLISION_DURATION_NANOS = 330_000_000L

@Composable
fun ArrowBoardCanvas(
    level: PuzzleLevel,
    remainingArrows: List<Arrow>,
    escapingArrows: List<EscapingArrowAnim>,
    collisionAnim: BlockedCollisionAnim?,
    highlightedHintArrowId: Int?,
    showGridLines: Boolean,
    zoomScale: Float,
    panOffset: Offset,
    onTransformChange: (Float, Offset) -> Unit,
    onArrowTapped: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalArrowBoardPalette.current
    val touchSlop = LocalViewConfiguration.current.touchSlop
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val latestArrows by rememberUpdatedState(remainingArrows)
    val latestLevel by rememberUpdatedState(level)
    val latestScale by rememberUpdatedState(zoomScale)
    val latestPan by rememberUpdatedState(panOffset)
    val latestOnTap by rememberUpdatedState(onArrowTapped)
    val latestOnTransform by rememberUpdatedState(onTransformChange)

    // Frame clock active only while animations are running (zero battery drain when idle)
    var frameTimeNanos by remember { mutableLongStateOf(System.nanoTime()) }
    val hasActiveAnims = escapingArrows.isNotEmpty() || collisionAnim != null
    LaunchedEffect(hasActiveAnims) {
        if (hasActiveAnims) {
            while (true) {
                withFrameNanos { nanos ->
                    frameTimeNanos = nanos
                }
            }
        }
    }

    // Breathing glow pulse for hinted arrow
    val infiniteTransition = rememberInfiniteTransition(label = "hintPulse")
    val hintPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "hintPulseAlpha",
    )

    val occupiedCells = remember(remainingArrows) {
        buildSet {
            for (arrow in remainingArrows) {
                addAll(arrow.cells)
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = it }
            .pointerInput(level.levelNumber, level.dailyDateKey) {
                awaitEachGesture {
                    val firstDown = awaitFirstDown(requireUnconsumed = false)
                    val downPosition = firstDown.position
                    var totalDrag = 0f
                    var wasMultiTouch = false
                    var didTransform = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val pressedCount = event.changes.count { it.pressed }
                        if (pressedCount == 0) {
                            break
                        }
                        if (pressedCount > 1) {
                            wasMultiTouch = true
                        }
                        val zoomChange = event.calculateZoom()
                        val panChange = event.calculatePan()
                        val centroidSize = event.calculateCentroidSize(useCurrent = false)
                        totalDrag += panChange.getDistance()

                        val shouldTransform =
                            (wasMultiTouch && (abs(zoomChange - 1f) > 0.004f || centroidSize > 0f)) ||
                                (latestScale > 1.02f && totalDrag > touchSlop * 0.65f)

                        if (shouldTransform) {
                            didTransform = true
                            val newScale = (latestScale * zoomChange).coerceIn(1f, 2.6f)
                            val maxPanX = (canvasSize.width * (newScale - 1f) * 0.55f).coerceAtLeast(0f)
                            val maxPanY = (canvasSize.height * (newScale - 1f) * 0.55f).coerceAtLeast(0f)
                            val newPan = if (newScale <= 1.01f) {
                                Offset.Zero
                            } else {
                                Offset(
                                    x = (latestPan.x + panChange.x).coerceIn(-maxPanX, maxPanX),
                                    y = (latestPan.y + panChange.y).coerceIn(-maxPanY, maxPanY),
                                )
                            }
                            latestOnTransform(newScale, newPan)
                            event.changes.forEach { if (it.positionChanged()) it.consume() }
                        }
                    }

                    if (!wasMultiTouch && !didTransform && totalDrag < touchSlop * 1.4f) {
                        val hitId = findTappedArrowId(
                            tapPos = downPosition,
                            canvasSize = canvasSize,
                            level = latestLevel,
                            arrows = latestArrows,
                            scale = latestScale,
                            pan = latestPan,
                        )
                        if (hitId != null) {
                            latestOnTap(hitId)
                        }
                    }
                }
            }
    ) {
        val metrics = computeBoardMetrics(size.width, size.height, level.cols, level.rows)

        // Subtle atmospheric ambient aura behind the board
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(palette.boardAuraStart, Color.Transparent),
                center = Offset(size.width / 2f, size.height / 2f),
                radius = minOf(size.width, size.height) * 0.54f,
            ),
            radius = minOf(size.width, size.height) * 0.54f,
            center = Offset(size.width / 2f, size.height / 2f),
        )

        withTransform({
            translate(left = panOffset.x, top = panOffset.y)
            scale(scaleX = zoomScale, scaleY = zoomScale, pivot = Offset(size.width / 2f, size.height / 2f))
        }) {
            // 1. Optional subtle architectural alignment grid lines
            if (showGridLines) {
                drawAlignmentGrid(level = level, metrics = metrics, color = palette.gridLine)
            }

            // 2. Minimalist dots on cleared/empty mask cells
            val dotRadius = (metrics.cellSize * 0.088f).coerceIn(2.0f, 5.5f)
            for (pt in level.validCells) {
                if (pt !in occupiedCells) {
                    val center = metrics.cellCenter(pt)
                    drawCircle(
                        color = palette.gridDot,
                        radius = dotRadius,
                        center = center,
                    )
                }
            }

            // 3. Draw static & colliding remaining arrows (non-hinted first)
            val strokeWidth = (metrics.cellSize * 0.165f).coerceIn(3.5f, 13.5f)

            for (arrow in remainingArrows) {
                if (arrow.id == highlightedHintArrowId) continue

                val isColliding = collisionAnim?.arrowId == arrow.id
                val isBlocker = collisionAnim?.blockingArrowId == arrow.id
                val baseColor = palette.arrowColors[arrow.colorIndex % palette.arrowColors.size]

                if (isColliding && collisionAnim != null) {
                    val elapsed = (frameTimeNanos - collisionAnim.startTimeNanos).coerceAtLeast(0L)
                    val progress = (elapsed.toFloat() / COLLISION_DURATION_NANOS).coerceIn(0f, 1f)
                    val bounceWave = sin(progress * PI.toFloat())
                    val maxSlide = (collisionAnim.distanceCells - 0.36f).coerceAtLeast(0.18f)
                    val slideCells = bounceWave * maxSlide
                    val flashColor = lerpColor(baseColor, palette.collisionColor, (bounceWave * 1.25f).coerceIn(0f, 1f))

                    drawSlidingArrow(
                        arrow = arrow,
                        slideCells = slideCells,
                        metrics = metrics,
                        strokeWidth = strokeWidth,
                        color = flashColor,
                        glowColor = palette.collisionColor.copy(alpha = bounceWave * 0.28f),
                        alpha = 1f,
                    )
                } else {
                    val color = if (isBlocker && collisionAnim != null) {
                        val elapsed = (frameTimeNanos - collisionAnim.startTimeNanos).coerceAtLeast(0L)
                        val progress = (elapsed.toFloat() / COLLISION_DURATION_NANOS).coerceIn(0f, 1f)
                        val wave = sin(progress * PI.toFloat())
                        lerpColor(baseColor, palette.collisionColor, wave * 0.65f)
                    } else {
                        baseColor
                    }
                    drawSlidingArrow(
                        arrow = arrow,
                        slideCells = 0f,
                        metrics = metrics,
                        strokeWidth = strokeWidth,
                        color = color,
                        glowColor = null,
                        alpha = 1f,
                    )
                }
            }

            // 4. Draw impact ring at collision point if colliding
            if (collisionAnim != null) {
                val elapsed = (frameTimeNanos - collisionAnim.startTimeNanos).coerceAtLeast(0L)
                val progress = (elapsed.toFloat() / COLLISION_DURATION_NANOS).coerceIn(0f, 1f)
                if (progress < 1f) {
                    val center = metrics.cellCenter(collisionAnim.blockingCell)
                    val ringRadius = metrics.cellSize * (0.18f + progress * 0.48f)
                    drawCircle(
                        color = palette.collisionColor.copy(alpha = (1f - progress) * 0.65f),
                        radius = ringRadius,
                        center = center,
                        style = Stroke(width = strokeWidth * 0.55f),
                    )
                }
            }

            // 5. Draw hinted arrow ON TOP with vibrant breathing aura
            if (highlightedHintArrowId != null) {
                val hintArrow = remainingArrows.firstOrNull { it.id == highlightedHintArrowId }
                if (hintArrow != null) {
                    val glowAlpha = 0.28f + hintPulse * 0.34f
                    drawSlidingArrow(
                        arrow = hintArrow,
                        slideCells = 0f,
                        metrics = metrics,
                        strokeWidth = strokeWidth * 1.12f,
                        color = palette.hintColor,
                        glowColor = palette.hintGlow.copy(alpha = glowAlpha),
                        alpha = 1f,
                    )
                }
            }

            // 6. Draw escaping arrows smoothly unspooling along their path & shooting off the grid!
            for (anim in escapingArrows) {
                val elapsed = (frameTimeNanos - anim.startTimeNanos).coerceAtLeast(0L)
                val rawT = (elapsed.toFloat() / ESCAPE_DURATION_NANOS).coerceIn(0f, 1f)
                val easedT = rawT.pow(1.65f)
                val exitDistance = maxOf(level.cols, level.rows) + 3.2f
                val arrowLen = (anim.arrow.cells.size - 1).toFloat().coerceAtLeast(1f)
                val slideCells = easedT * (arrowLen + exitDistance)
                val fadeAlpha = if (rawT > 0.72f) ((1f - rawT) / 0.28f).coerceIn(0f, 1f) else 1f

                val escapeColor = if (palette.isDark) {
                    palette.arrowColors[anim.arrow.colorIndex % palette.arrowColors.size]
                } else {
                    palette.escapingColor
                }

                drawSlidingArrow(
                    arrow = anim.arrow,
                    slideCells = slideCells,
                    metrics = metrics,
                    strokeWidth = strokeWidth * 1.06f,
                    color = escapeColor,
                    glowColor = escapeColor.copy(alpha = 0.28f * fadeAlpha),
                    alpha = fadeAlpha,
                )
            }
        }
    }
}

/**
 * Reusable miniature puzzle artwork preview (used in Home Hero card and Victory Celebration showcase).
 */
@Composable
fun MiniPuzzlePreviewCanvas(
    level: PuzzleLevel,
    palette: ArrowBoardPalette = LocalArrowBoardPalette.current,
    monochromeInk: Color? = null,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val metrics = computeBoardMetrics(
            width = size.width,
            height = size.height,
            cols = level.cols,
            rows = level.rows,
            paddingFactor = 0.85f,
            capMaxCell = false,
        )
        val strokeWidth = (metrics.cellSize * 0.175f).coerceIn(1.8f, 6.5f)
        for (arrow in level.arrows) {
            val color = monochromeInk ?: palette.arrowColors[arrow.colorIndex % palette.arrowColors.size]
            drawSlidingArrow(
                arrow = arrow,
                slideCells = 0f,
                metrics = metrics,
                strokeWidth = strokeWidth,
                color = color,
                glowColor = null,
                alpha = 1f,
            )
        }
    }
}

private data class BoardMetrics(
    val cellSize: Float,
    val originX: Float,
    val originY: Float,
) {
    fun cellCenter(pt: GridPoint): Offset = Offset(
        x = originX + (pt.col + 0.5f) * cellSize,
        y = originY + (pt.row + 0.5f) * cellSize,
    )

    fun gridCoordToOffset(colF: Float, rowF: Float): Offset = Offset(
        x = originX + (colF + 0.5f) * cellSize,
        y = originY + (rowF + 0.5f) * cellSize,
    )
}

private fun computeBoardMetrics(
    width: Float,
    height: Float,
    cols: Int,
    rows: Int,
    paddingFactor: Float = 1.15f,
    capMaxCell: Boolean = true,
): BoardMetrics {
    val safeW = width.coerceAtLeast(1f)
    val safeH = height.coerceAtLeast(1f)
    val rawCell = minOf(safeW / (cols + paddingFactor), safeH / (rows + paddingFactor))
    val cellSize = if (capMaxCell) {
        val maxRefinedCell = minOf(safeW, safeH) / 7.2f
        minOf(rawCell, maxRefinedCell)
    } else {
        rawCell
    }
    val boardW = cols * cellSize
    val boardH = rows * cellSize
    return BoardMetrics(
        cellSize = cellSize,
        originX = (safeW - boardW) / 2f,
        originY = (safeH - boardH) / 2f,
    )
}

private fun DrawScope.drawAlignmentGrid(
    level: PuzzleLevel,
    metrics: BoardMetrics,
    color: Color,
) {
    val half = metrics.cellSize * 0.5f
    val lineStroke = (metrics.cellSize * 0.032f).coerceIn(1f, 2.2f)
    for (pt in level.validCells) {
        val c = metrics.cellCenter(pt)
        val rightNeighbor = GridPoint(pt.col + 1, pt.row)
        val downNeighbor = GridPoint(pt.col, pt.row + 1)
        if (rightNeighbor in level.validCells) {
            drawLine(
                color = color,
                start = c,
                end = Offset(c.x + metrics.cellSize, c.y),
                strokeWidth = lineStroke,
            )
        }
        if (downNeighbor in level.validCells) {
            drawLine(
                color = color,
                start = c,
                end = Offset(c.x, c.y + metrics.cellSize),
                strokeWidth = lineStroke,
            )
        }
        // Subtle outer corner tick
        drawCircle(color = color, radius = lineStroke * 0.9f, center = Offset(c.x, c.y))
    }
    // Suppress unused warning on half if not needed
    if (half < 0f) Unit
}

/**
 * Slides an arrow along its own polyline + forward exit ray by [slideCells] grid units
 * and draws the rounded-corner shaft + crisp geometric arrowhead.
 */
private fun DrawScope.drawSlidingArrow(
    arrow: Arrow,
    slideCells: Float,
    metrics: BoardMetrics,
    strokeWidth: Float,
    color: Color,
    glowColor: Color?,
    alpha: Float,
) {
    if (arrow.cells.size < 2 || alpha <= 0.01f) return

    // Extended polyline in grid space: tail -> ... -> head -> far exit point
    val baseLen = (arrow.cells.size - 1).toFloat()
    val exitExtension = slideCells + 4f
    val extended = ArrayList<Pair<Float, Float>>(arrow.cells.size + 1)
    for (pt in arrow.cells) {
        extended.add(pt.col.toFloat() to pt.row.toFloat())
    }
    extended.add(
        (arrow.head.col + arrow.direction.dx * exitExtension) to
            (arrow.head.row + arrow.direction.dy * exitExtension)
    )

    // Slice sub-polyline from sStart = slideCells to sEnd = slideCells + baseLen
    // Leave tiny margin at tail (0.14) and head tip (0.10) for balanced visual spacing
    val sStart = slideCells + 0.10f
    val sEnd = (slideCells + baseLen + 0.14f).coerceAtLeast(sStart + 0.25f)

    val slicedGridPts = slicePolyline(extended, sStart, sEnd)
    if (slicedGridPts.size < 2) return

    val screenPts = slicedGridPts.map { (cx, cy) -> metrics.gridCoordToOffset(cx, cy) }

    // Heading vector from final segment
    val tip = screenPts.last()
    val preTip = screenPts[screenPts.size - 2]
    val dx = tip.x - preTip.x
    val dy = tip.y - preTip.y
    val segLen = hypot(dx, dy).coerceAtLeast(0.001f)
    val ux = dx / segLen
    val uy = dy / segLen

    // Shorten shaft slightly before the tip so the arrowhead apex is crisp and pointed
    val headLen = (metrics.cellSize * 0.34f).coerceIn(strokeWidth * 1.9f, metrics.cellSize * 0.42f)
    val headHalfW = headLen * 0.68f
    val shaftTrim = (headLen * 0.58f).coerceAtMost(segLen * 0.72f)
    val shaftPts = screenPts.toMutableList()
    shaftPts[shaftPts.lastIndex] = Offset(tip.x - ux * shaftTrim, tip.y - uy * shaftTrim)

    val cornerRadius = (metrics.cellSize * 0.24f).coerceAtMost(metrics.cellSize * 0.32f)
    val shaftPath = buildRoundedPolylinePath(shaftPts, cornerRadius)

    val headPath = Path().apply {
        val baseCenter = Offset(tip.x - ux * headLen, tip.y - uy * headLen)
        val perpX = -uy
        val perpY = ux
        val leftCorner = Offset(baseCenter.x + perpX * headHalfW, baseCenter.y + perpY * headHalfW)
        val rightCorner = Offset(baseCenter.x - perpX * headHalfW, baseCenter.y - perpY * headHalfW)
        val notchCenter = Offset(tip.x - ux * (headLen * 0.92f), tip.y - uy * (headLen * 0.92f))
        moveTo(tip.x, tip.y)
        lineTo(leftCorner.x, leftCorner.y)
        lineTo(notchCenter.x, notchCenter.y)
        lineTo(rightCorner.x, rightCorner.y)
        close()
    }

    val drawColor = color.copy(alpha = color.alpha * alpha)

    // Soft glow pass (for hints, collisions, or active escapes)
    if (glowColor != null) {
        drawPath(
            path = shaftPath,
            color = glowColor,
            style = Stroke(width = strokeWidth * 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        drawPath(
            path = headPath,
            color = glowColor,
            style = Stroke(width = strokeWidth * 1.4f, join = StrokeJoin.Round),
        )
    }

    // Crisp shaft
    drawPath(
        path = shaftPath,
        color = drawColor,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )

    // Crisp geometric arrowhead
    drawPath(path = headPath, color = drawColor)
}

private fun slicePolyline(
    pts: List<Pair<Float, Float>>,
    sStart: Float,
    sEnd: Float,
): List<Pair<Float, Float>> {
    val result = ArrayList<Pair<Float, Float>>()
    var acc = 0f

    for (i in 0 until pts.size - 1) {
        val (x0, y0) = pts[i]
        val (x1, y1) = pts[i + 1]
        val len = hypot(x1 - x0, y1 - y0)
        if (len <= 1e-5f) continue
        val segStart = acc
        val segEnd = acc + len

        if (segEnd >= sStart && segStart <= sEnd) {
            val t0 = ((sStart - segStart) / len).coerceIn(0f, 1f)
            val t1 = ((sEnd - segStart) / len).coerceIn(0f, 1f)
            val p0 = (x0 + (x1 - x0) * t0) to (y0 + (y1 - y0) * t0)
            val p1 = (x0 + (x1 - x0) * t1) to (y0 + (y1 - y0) * t1)

            if (result.isEmpty()) {
                result.add(p0)
            } else {
                val last = result.last()
                if (hypot(p0.first - last.first, p0.second - last.second) > 1e-3f) {
                    result.add(p0)
                }
            }
            if (hypot(p1.first - p0.first, p1.second - p0.second) > 1e-3f) {
                result.add(p1)
            }
        }
        acc = segEnd
        if (acc > sEnd) break
    }
    return result
}

private fun buildRoundedPolylinePath(points: List<Offset>, radius: Float): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points[0].x, points[0].y)
    if (points.size == 2) {
        path.lineTo(points[1].x, points[1].y)
        return path
    }
    for (i in 1 until points.size - 1) {
        val prev = points[i - 1]
        val curr = points[i]
        val next = points[i + 1]

        val v1x = prev.x - curr.x
        val v1y = prev.y - curr.y
        val len1 = hypot(v1x, v1y)

        val v2x = next.x - curr.x
        val v2y = next.y - curr.y
        val len2 = hypot(v2x, v2y)

        if (len1 < 0.01f || len2 < 0.01f) {
            path.lineTo(curr.x, curr.y)
            continue
        }

        // Check collinearity — skip rounding if straight segment
        val cross = abs(v1x * v2y - v1y * v2x) / (len1 * len2)
        if (cross < 0.05f) {
            path.lineTo(curr.x, curr.y)
            continue
        }

        val r = minOf(radius, len1 * 0.45f, len2 * 0.45f)
        val pBefore = Offset(curr.x + (v1x / len1) * r, curr.y + (v1y / len1) * r)
        val pAfter = Offset(curr.x + (v2x / len2) * r, curr.y + (v2y / len2) * r)
        path.lineTo(pBefore.x, pBefore.y)
        path.quadraticTo(curr.x, curr.y, pAfter.x, pAfter.y)
    }
    val last = points.last()
    path.lineTo(last.x, last.y)
    return path
}

private fun findTappedArrowId(
    tapPos: Offset,
    canvasSize: IntSize,
    level: PuzzleLevel,
    arrows: List<Arrow>,
    scale: Float,
    pan: Offset,
): Int? {
    if (canvasSize.width == 0 || canvasSize.height == 0 || arrows.isEmpty()) return null
    val cx = canvasSize.width / 2f
    val cy = canvasSize.height / 2f

    // Invert pan & zoom around canvas center
    val boardPos = Offset(
        x = (tapPos.x - pan.x - cx) / scale + cx,
        y = (tapPos.y - pan.y - cy) / scale + cy,
    )

    val metrics = computeBoardMetrics(
        width = canvasSize.width.toFloat(),
        height = canvasSize.height.toFloat(),
        cols = level.cols,
        rows = level.rows,
    )

    // 1. Check exact grid cell hit first (most natural for dense puzzles)
    val col = kotlin.math.floor((boardPos.x - metrics.originX) / metrics.cellSize).toInt()
    val row = kotlin.math.floor((boardPos.y - metrics.originY) / metrics.cellSize).toInt()
    val cellPt = GridPoint(col, row)
    val exactHit = arrows.firstOrNull { cellPt in it.cells }
    if (exactHit != null) return exactHit.id

    // 2. Generous closest-polyline fallback within 0.60 * cellSize
    var bestId: Int? = null
    var bestDist = metrics.cellSize * 0.62f

    for (arrow in arrows) {
        for (i in 0 until arrow.cells.size - 1) {
            val a = metrics.cellCenter(arrow.cells[i])
            val b = metrics.cellCenter(arrow.cells[i + 1])
            val d = pointToSegmentDistance(boardPos, a, b)
            if (d < bestDist) {
                bestDist = d
                bestId = arrow.id
            }
        }
    }
    return bestId
}

private fun pointToSegmentDistance(p: Offset, a: Offset, b: Offset): Float {
    val vx = b.x - a.x
    val vy = b.y - a.y
    val wx = p.x - a.x
    val wy = p.y - a.y
    val c1 = vx * wx + vy * wy
    if (c1 <= 0f) return hypot(p.x - a.x, p.y - a.y)
    val c2 = vx * vx + vy * vy
    if (c2 <= c1) return hypot(p.x - b.x, p.y - b.y)
    val t = c1 / c2
    val projX = a.x + t * vx
    val projY = a.y + t * vy
    return hypot(p.x - projX, p.y - projY)
}

private fun lerpColor(start: Color, stop: Color, fraction: Float): Color {
    val t = fraction.coerceIn(0f, 1f)
    return Color(
        red = start.red + (stop.red - start.red) * t,
        green = start.green + (stop.green - start.green) * t,
        blue = start.blue + (stop.blue - start.blue) * t,
        alpha = start.alpha + (stop.alpha - start.alpha) * t,
    )
}
