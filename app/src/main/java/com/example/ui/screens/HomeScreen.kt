package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.Difficulty
import com.example.model.LevelSummary
import com.example.ui.components.MiniPuzzlePreviewCanvas
import com.example.ui.theme.DiffEasyBg
import com.example.ui.theme.DiffEasyBgDark
import com.example.ui.theme.DiffEasyFg
import com.example.ui.theme.DiffEasyFgDark
import com.example.ui.theme.DiffExpertBg
import com.example.ui.theme.DiffExpertBgDark
import com.example.ui.theme.DiffExpertFg
import com.example.ui.theme.DiffExpertFgDark
import com.example.ui.theme.DiffHardBg
import com.example.ui.theme.DiffHardBgDark
import com.example.ui.theme.DiffHardFg
import com.example.ui.theme.DiffHardFgDark
import com.example.ui.theme.DiffMasterBg
import com.example.ui.theme.DiffMasterBgDark
import com.example.ui.theme.DiffMasterFg
import com.example.ui.theme.DiffMasterFgDark
import com.example.ui.theme.DiffNormalBg
import com.example.ui.theme.DiffNormalBgDark
import com.example.ui.theme.DiffNormalFg
import com.example.ui.theme.DiffNormalFgDark
import com.example.ui.theme.LocalArrowBoardPalette
import com.example.viewmodel.HomeUiState

@Composable
fun HomeScreen(
    state: HomeUiState,
    onPlayLevel: (Int) -> Unit,
    onPlayDaily: () -> Unit,
    onSelectFilter: (Difficulty?) -> Unit,
    onLoadMoreLevels: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalArrowBoardPalette.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 78.dp),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Top Brand Header
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(palette.heroGradientStart, palette.heroGradientEnd)
                                    )
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NearMe,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                text = stringResource(R.string.home_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    ) {
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.testTag("home_settings_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = stringResource(R.string.cd_settings),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            // Hero Active Level Card with live miniature board preview
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(palette.heroGradientStart, palette.heroGradientEnd)
                            )
                        )
                        .clickable { onPlayLevel(state.highestUnlockedLevel) }
                        .padding(20.dp)
                        .testTag("hero_continue_card"),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.22f),
                            ) {
                                Text(
                                    text = "${state.heroPreviewLevel.difficulty.label} • ${state.heroPreviewLevel.shape.displayName}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.home_level_label, state.highestUnlockedLevel),
                                style = MaterialTheme.typography.displaySmall,
                                color = Color.White,
                            )
                            Text(
                                text = "${state.heroPreviewLevel.arrows.size} arrows",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.86f),
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { onPlayLevel(state.highestUnlockedLevel) },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = palette.heroGradientStart,
                                ),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("hero_play_button"),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.home_continue_level, state.highestUnlockedLevel),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Live miniature puzzle artwork preview
                        Surface(
                            modifier = Modifier.size(116.dp),
                            shape = RoundedCornerShape(22.dp),
                            color = Color.White.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
                        ) {
                            MiniPuzzlePreviewCanvas(
                                level = state.heroPreviewLevel,
                                monochromeInk = Color.White,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp),
                            )
                        }
                    }
                }
            }

            // Stats Strip (Solved, Stars, Hints)
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    HomeStatCard(
                        icon = Icons.Rounded.CheckCircle,
                        iconTint = Color(0xFF10B981),
                        value = state.solvedCount.toString(),
                        label = stringResource(R.string.home_stat_solved),
                        modifier = Modifier.weight(1f),
                    )
                    HomeStatCard(
                        icon = Icons.Rounded.Star,
                        iconTint = palette.starActive,
                        value = state.totalStars.toString(),
                        label = stringResource(R.string.home_stat_stars),
                        modifier = Modifier.weight(1f),
                    )
                    HomeStatCard(
                        icon = Icons.Outlined.Lightbulb,
                        iconTint = MaterialTheme.colorScheme.primary,
                        value = state.hintBalance.toString(),
                        label = stringResource(R.string.home_stat_hints),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Daily Maze Special Card
            item(span = { GridItemSpan(maxLineSpan) }) {
                Surface(
                    onClick = onPlayDaily,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("daily_challenge_card"),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = if (state.dailyCompleted) {
                                    Icons.Rounded.CheckCircle
                                } else {
                                    Icons.Rounded.CalendarToday
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.home_daily_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = if (state.dailyCompleted) {
                                    stringResource(R.string.home_daily_completed)
                                } else {
                                    stringResource(R.string.home_daily_subtitle)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = if (state.dailyCompleted) {
                                MaterialTheme.colorScheme.surfaceVariant
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                        ) {
                            Text(
                                text = stringResource(R.string.home_daily_play),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (state.dailyCompleted) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onPrimary
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                    }
                }
            }

            // Section Heading + Difficulty Chapter Filter Chips
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(modifier = Modifier.padding(top = 6.dp)) {
                    Text(
                        text = stringResource(R.string.home_levels_heading),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = state.selectedFilter == null,
                            onClick = { onSelectFilter(null) },
                            label = { Text(stringResource(R.string.home_filter_all)) },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            modifier = Modifier.testTag("filter_all"),
                        )
                        Difficulty.entries.forEach { diff ->
                            FilterChip(
                                selected = state.selectedFilter == diff,
                                onClick = {
                                    onSelectFilter(if (state.selectedFilter == diff) null else diff)
                                },
                                label = { Text(diff.label) },
                                shape = CircleShape,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                                modifier = Modifier.testTag("filter_${diff.name.lowercase()}"),
                            )
                        }
                    }
                }
            }

            // Level Grid Tiles
            items(
                items = state.levels,
                key = { it.levelNumber },
            ) { summary ->
                LevelTileCard(
                    summary = summary,
                    isCurrent = summary.levelNumber == state.highestUnlockedLevel,
                    onClick = {
                        if (summary.unlocked) {
                            onPlayLevel(summary.levelNumber)
                        }
                    },
                )
            }

            // Expand infinite levels button
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    OutlinedButton(
                        onClick = onLoadMoreLevels,
                        shape = CircleShape,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("load_more_levels_button"),
                    ) {
                        Text(
                            text = stringResource(R.string.home_load_more),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp),
            )
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun LevelTileCard(
    summary: LevelSummary,
    isCurrent: Boolean,
    onClick: () -> Unit,
) {
    val palette = LocalArrowBoardPalette.current
    val (_, diffAccent) = difficultyColors(summary.difficulty, palette.isDark)

    val containerColor = when {
        isCurrent -> MaterialTheme.colorScheme.primaryContainer
        summary.completed -> MaterialTheme.colorScheme.surface
        summary.unlocked -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    }

    val borderStroke = when {
        isCurrent -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        summary.completed -> BorderStroke(1.dp, diffAccent.copy(alpha = 0.35f))
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    }

    Surface(
        onClick = onClick,
        enabled = summary.unlocked,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.94f)
            .testTag("level_tile_${summary.levelNumber}"),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = borderStroke,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Subtle difficulty indicator bar
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(if (summary.unlocked) diffAccent else MaterialTheme.colorScheme.outline),
            )

            Text(
                text = summary.levelNumber.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (summary.unlocked) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                },
            )

            if (summary.completed) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    for (i in 1..3) {
                        val filled = i <= summary.bestStars
                        Icon(
                            imageVector = if (filled) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                            contentDescription = null,
                            tint = if (filled) palette.starActive else palette.starInactive,
                            modifier = Modifier.size(13.dp),
                        )
                    }
                }
            } else if (!summary.unlocked) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.size(14.dp),
                )
            } else {
                Text(
                    text = summary.difficulty.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = diffAccent,
                    maxLines = 1,
                )
            }
        }
    }
}

fun difficultyColors(difficulty: Difficulty, isDark: Boolean): Pair<Color, Color> =
    if (isDark) {
        when (difficulty) {
            Difficulty.EASY -> DiffEasyBgDark to DiffEasyFgDark
            Difficulty.NORMAL -> DiffNormalBgDark to DiffNormalFgDark
            Difficulty.HARD -> DiffHardBgDark to DiffHardFgDark
            Difficulty.EXPERT -> DiffExpertBgDark to DiffExpertFgDark
            Difficulty.MASTER -> DiffMasterBgDark to DiffMasterFgDark
        }
    } else {
        when (difficulty) {
            Difficulty.EASY -> DiffEasyBg to DiffEasyFg
            Difficulty.NORMAL -> DiffNormalBg to DiffNormalFg
            Difficulty.HARD -> DiffHardBg to DiffHardFg
            Difficulty.EXPERT -> DiffExpertBg to DiffExpertFg
            Difficulty.MASTER -> DiffMasterBg to DiffMasterFg
        }
    }
