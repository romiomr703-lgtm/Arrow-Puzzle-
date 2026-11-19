package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.CenterFocusWeak
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.UserSettings
import com.example.ui.components.ArrowBoardCanvas
import com.example.ui.components.CelebrationOverlay
import com.example.ui.theme.LocalArrowBoardPalette
import com.example.viewmodel.GameStatus
import com.example.viewmodel.GameUiState
import com.example.viewmodel.HapticCue
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun GameScreen(
    state: GameUiState,
    settings: UserSettings,
    hapticEvents: SharedFlow<HapticCue>,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onRestart: () -> Unit,
    onArrowTapped: (Int) -> Unit,
    onRequestHint: () -> Unit,
    onToggleGrid: () -> Unit,
    onNextLevel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalArrowBoardPalette.current
    val haptic = LocalHapticFeedback.current

    // Zoom & pan state resets automatically whenever a new level loads
    var zoomScale by remember(state.level.levelNumber, state.level.dailyDateKey) { mutableFloatStateOf(1f) }
    var panOffset by remember(state.level.levelNumber, state.level.dailyDateKey) { mutableStateOf(Offset.Zero) }

    LaunchedEffect(hapticEvents) {
        hapticEvents.collect { cue ->
            when (cue) {
                HapticCue.ESCAPE -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                HapticCue.BLOCKED -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                HapticCue.HINT -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                HapticCue.VICTORY -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }

    val (diffBg, diffFg) = difficultyColors(state.level.difficulty, palette.isDark)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.boardBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("game_back_button"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (state.level.isDaily) {
                            stringResource(R.string.game_daily_title)
                        } else {
                            stringResource(R.string.game_level_title, state.level.levelNumber)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = state.level.shape.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            zoomScale = 1f
                            panOffset = Offset.Zero
                            onRestart()
                        },
                        modifier = Modifier.testTag("game_restart_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.cd_restart_level),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("game_settings_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.cd_settings),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            // Sub-Header Status Strip: Remaining Arrows | Hearts/Zen | Difficulty Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Remaining Arrows Counter Pill
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.testTag("remaining_arrows_pill"),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NearMe,
                            contentDescription = stringResource(R.string.cd_remaining_arrows),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp),
                        )
                        AnimatedContent(
                            targetState = state.remainingArrows.size,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "remainingCount",
                        ) { count ->
                            Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                // Center 3 Red Hearts Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("hearts_row"),
                ) {
                    for (i in 1..state.maxHearts) {
                        val active = i <= state.hearts
                        val heartScale by animateFloatAsState(
                            targetValue = if (active) 1f else 0.78f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium,
                            ),
                            label = "heartScale_$i",
                        )
                        Icon(
                            imageVector = if (active) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = stringResource(
                                if (active) R.string.cd_heart_full else R.string.cd_heart_empty
                            ),
                            tint = if (active) palette.heartActive else palette.heartLost,
                            modifier = Modifier
                                .size(26.dp)
                                .scale(heartScale),
                        )
                    }
                }

                // Difficulty Pill
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = diffBg,
                    modifier = Modifier.testTag("difficulty_badge"),
                ) {
                    Text(
                        text = state.level.difficulty.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = diffFg,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    )
                }
            }

            // Main Interactive Arrow Board Canvas Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                ArrowBoardCanvas(
                    level = state.level,
                    remainingArrows = state.remainingArrows,
                    escapingArrows = state.escapingArrows,
                    collisionAnim = state.collisionAnim,
                    highlightedHintArrowId = state.highlightedHintArrowId,
                    showGridLines = settings.showGridLines,
                    zoomScale = zoomScale,
                    panOffset = panOffset,
                    onTransformChange = { newScale, newPan ->
                        zoomScale = newScale
                        panOffset = newPan
                    },
                    onArrowTapped = onArrowTapped,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("arrow_board_canvas"),
                )

                // Contextual Hint / Status Toast Banner
                androidx.compose.animation.AnimatedVisibility(
                    visible = state.statusBanner != null,
                    enter = fadeIn() + slideInVertically { -it / 2 },
                    exit = fadeOut() + slideOutVertically { -it / 2 },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 6.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 6.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    ) {
                        Text(
                            text = state.statusBanner.orEmpty(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }

                // Floating Reset Zoom pill (visible only when user zoomed in)
                androidx.compose.animation.AnimatedVisibility(
                    visible = zoomScale > 1.06f,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 8.dp),
                ) {
                    Surface(
                        onClick = {
                            zoomScale = 1f
                            panOffset = Offset.Zero
                        },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.testTag("reset_zoom_button"),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CenterFocusWeak,
                                contentDescription = stringResource(R.string.cd_reset_zoom),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = stringResource(R.string.game_reset_zoom),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            // Bottom Floating Circular Controls (Hint + Grid Guide)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Hint Button with Badge
                Box(contentAlignment = Alignment.TopEnd) {
                    val hintActive = state.highlightedHintArrowId != null
                    Surface(
                        onClick = onRequestHint,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 6.dp,
                        border = BorderStroke(
                            width = if (hintActive) 2.5.dp else 1.dp,
                            color = if (hintActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        ),
                        modifier = Modifier
                            .size(62.dp)
                            .testTag("hint_button"),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Lightbulb,
                                contentDescription = stringResource(R.string.cd_hint),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                    }

                    // Hint Count Badge
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .offset(x = 4.dp, y = (-4).dp)
                            .size(26.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = settings.hintBalance.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                }

                // Grid Guides Button (#)
                Surface(
                    onClick = onToggleGrid,
                    shape = CircleShape,
                    color = if (settings.showGridLines) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    shadowElevation = 6.dp,
                    border = BorderStroke(
                        width = if (settings.showGridLines) 2.dp else 1.dp,
                        color = if (settings.showGridLines) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                    ),
                    modifier = Modifier
                        .size(62.dp)
                        .testTag("grid_toggle_button"),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Tag,
                            contentDescription = stringResource(R.string.cd_toggle_grid),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                }
            }
        }

        // Out of Hearts Overlay
        AnimatedVisibility(
            visible = state.status == GameStatus.LOST,
            enter = fadeIn() + scaleIn(initialScale = 0.92f),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.48f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 380.dp)
                        .testTag("game_over_dialog"),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(palette.heartActive.copy(alpha = 0.14f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FavoriteBorder,
                                contentDescription = null,
                                tint = palette.heartActive,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.game_over_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.game_over_subtitle, state.remainingArrows.size),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(22.dp))
                        Button(
                            onClick = {
                                zoomScale = 1f
                                panOffset = Offset.Zero
                                onRestart()
                            },
                            shape = CircleShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("game_over_retry_button"),
                        ) {
                            Text(
                                text = stringResource(R.string.game_over_retry),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(
                            onClick = onBack,
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("game_over_home_button"),
                        ) {
                            Text(text = stringResource(R.string.game_over_home))
                        }
                    }
                }
            }
        }

        // Level Completed Celebration Overlay
        AnimatedVisibility(
            visible = state.status == GameStatus.WON,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            CelebrationOverlay(
                level = state.level,
                starsEarned = state.starsEarned,
                elapsedSeconds = state.elapsedSeconds,
                earnedHintBonus = state.earnedHintBonus,
                onNextLevel = {
                    zoomScale = 1f
                    panOffset = Offset.Zero
                    onNextLevel()
                },
                onHome = onBack,
            )
        }
    }
}
