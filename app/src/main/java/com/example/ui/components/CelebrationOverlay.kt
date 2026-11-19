package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.PuzzleLevel
import com.example.ui.theme.CoralPunch
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.LocalArrowBoardPalette
import com.example.ui.theme.RoyalViolet
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.VividCyan
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiPiece(
    val startX: Float,
    val startY: Float,
    val speedY: Float,
    val swayAmp: Float,
    val swayFreq: Float,
    val rotSpeed: Float,
    val width: Float,
    val height: Float,
    val color: Color,
)

@Composable
fun CelebrationOverlay(
    level: PuzzleLevel,
    starsEarned: Int,
    elapsedSeconds: Int,
    earnedHintBonus: Boolean,
    onNextLevel: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalArrowBoardPalette.current
    val infinite = rememberInfiniteTransition(label = "celebration")

    val sunRotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "sunRotation",
    )

    val confettiPhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "confettiFall",
    )

    val confettiPieces = remember {
        val rng = Random(42)
        val colors = listOf(CoralPunch, SunsetAmber, EmeraldMint, VividCyan, RoyalViolet, Color.White)
        List(38) {
            ConfettiPiece(
                startX = rng.nextFloat(),
                startY = rng.nextFloat(),
                speedY = 0.65f + rng.nextFloat() * 0.7f,
                swayAmp = 0.02f + rng.nextFloat() * 0.04f,
                swayFreq = 2f + rng.nextFloat() * 4f,
                rotSpeed = 120f + rng.nextFloat() * 240f,
                width = 10f + rng.nextFloat() * 10f,
                height = 5f + rng.nextFloat() * 6f,
                color = colors[it % colors.size],
            )
        }
    }

    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(60L)
        revealed = true
    }

    val cardScale by animateFloatAsState(
        targetValue = if (revealed) 1f else 0.82f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "victoryCardScale",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.sunburstPrimary)
            .testTag("victory_overlay"),
        contentAlignment = Alignment.Center,
    ) {
        // Rotating radial sunburst rays + falling minimalist confetti
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.44f)
            val maxRadius = kotlin.math.hypot(size.width, size.height) * 1.15f
            val rayCount = 16
            val raySweep = 360f / (rayCount * 2)

            rotate(degrees = sunRotation, pivot = center) {
                for (i in 0 until rayCount) {
                    val startAngle = Math.toRadians((i * raySweep * 2.0))
                    val endAngle = Math.toRadians((i * raySweep * 2.0 + raySweep))
                    val path = Path().apply {
                        moveTo(center.x, center.y)
                        lineTo(
                            center.x + (cos(startAngle) * maxRadius).toFloat(),
                            center.y + (sin(startAngle) * maxRadius).toFloat(),
                        )
                        lineTo(
                            center.x + (cos(endAngle) * maxRadius).toFloat(),
                            center.y + (sin(endAngle) * maxRadius).toFloat(),
                        )
                        close()
                    }
                    drawPath(path = path, color = palette.sunburstSecondary.copy(alpha = 0.48f))
                }
            }

            // Falling confetti rectangles
            for (piece in confettiPieces) {
                val yFrac = (piece.startY + confettiPhase * piece.speedY) % 1.15f - 0.08f
                val xFrac = piece.startX + sin(confettiPhase * 6.283f * piece.swayFreq) * piece.swayAmp
                val px = xFrac * size.width
                val py = yFrac * size.height
                rotate(degrees = confettiPhase * piece.rotSpeed, pivot = Offset(px, py)) {
                    drawRect(
                        color = piece.color.copy(alpha = 0.92f),
                        topLeft = Offset(px - piece.width / 2f, py - piece.height / 2f),
                        size = Size(piece.width, piece.height),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .widthIn(max = 460.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.victory_eyebrow).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (level.isDaily) {
                        stringResource(R.string.victory_daily_title)
                    } else {
                        stringResource(R.string.victory_title)
                    },
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Animated Stars Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    for (starIndex in 1..3) {
                        val earned = starIndex <= starsEarned
                        val starScale by animateFloatAsState(
                            targetValue = if (revealed && earned) 1.0f else 0.72f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow,
                            ),
                            label = "starScale_$starIndex",
                        )
                        Icon(
                            imageVector = if (earned) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                            contentDescription = stringResource(
                                if (earned) R.string.cd_star_earned else R.string.cd_star_empty
                            ),
                            tint = if (earned) Color(0xFFFFD43B) else Color.White.copy(alpha = 0.45f),
                            modifier = Modifier
                                .size(40.dp)
                                .scale(starScale),
                        )
                    }
                }
            }

            // Solved Silhouette Showcase Card (faithful to minimalist gallery aesthetic)
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.84f)
                    .scale(cardScale),
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 14.dp,
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    MiniPuzzlePreviewCanvas(
                        level = level,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        VictoryStatChip(
                            label = stringResource(R.string.victory_stat_time),
                            value = formatDuration(elapsedSeconds),
                        )
                        VictoryStatChip(
                            label = stringResource(R.string.victory_stat_arrows),
                            value = level.arrows.size.toString(),
                        )
                    }
                    if (earnedHintBonus) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE0F2FE),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = stringResource(R.string.victory_hint_reward),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF0369A1),
                                )
                            }
                        }
                    }
                }
            }

            // Primary Next Level & Secondary Home Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = onNextLevel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("victory_next_button"),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = palette.sunburstPrimary,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                ) {
                    Text(
                        text = stringResource(R.string.victory_next_level),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onHome,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("victory_home_button"),
                ) {
                    Text(
                        text = stringResource(R.string.victory_home),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun VictoryStatChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF64748B),
        )
    }
}

fun formatDuration(totalSeconds: Int): String {
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    return if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
}
