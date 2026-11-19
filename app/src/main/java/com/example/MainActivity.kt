package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.GameThemePalette
import com.example.ui.components.SettingsBottomSheet
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.ArrowEscapeTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ArrowPuzzleViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArrowEscapeApp(
                onDarkStatusBarChanged = { isDark ->
                    enableEdgeToEdge(
                        statusBarStyle = if (isDark) {
                            SystemBarStyle.dark(Color.TRANSPARENT)
                        } else {
                            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                        },
                        navigationBarStyle = if (isDark) {
                            SystemBarStyle.dark(Color.TRANSPARENT)
                        } else {
                            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                        },
                    )
                }
            )
        }
    }
}

@Composable
private fun ArrowEscapeApp(
    onDarkStatusBarChanged: (Boolean) -> Unit,
    viewModel: ArrowPuzzleViewModel = viewModel(),
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val homeState by viewModel.homeState.collectAsStateWithLifecycle()
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val showSettings by viewModel.showSettings.collectAsStateWithLifecycle()

    val isDark = settings.themePalette == GameThemePalette.MIDNIGHT_GLOW
    LaunchedEffect(isDark) {
        onDarkStatusBarChanged(isDark)
    }

    BackHandler(enabled = currentScreen == AppScreen.GAME) {
        viewModel.navigateHome()
    }

    ArrowEscapeTheme(palette = settings.themePalette) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                if (targetState == AppScreen.GAME) {
                    (slideInHorizontally { it / 5 } + fadeIn()) togetherWith fadeOut()
                } else {
                    fadeIn() togetherWith (slideOutHorizontally { it / 5 } + fadeOut())
                }
            },
            label = "screenTransition",
        ) { screen ->
            when (screen) {
                AppScreen.HOME -> HomeScreen(
                    state = homeState,
                    onPlayLevel = viewModel::startLevel,
                    onPlayDaily = viewModel::startDailyChallenge,
                    onSelectFilter = viewModel::selectDifficultyFilter,
                    onLoadMoreLevels = viewModel::loadMoreLevels,
                    onOpenSettings = viewModel::openSettings,
                )

                AppScreen.GAME -> GameScreen(
                    state = gameState,
                    settings = settings,
                    hapticEvents = viewModel.hapticEvents,
                    onBack = viewModel::navigateHome,
                    onOpenSettings = viewModel::openSettings,
                    onRestart = viewModel::restartCurrentLevel,
                    onArrowTapped = viewModel::onArrowTapped,
                    onRequestHint = viewModel::requestHint,
                    onToggleGrid = viewModel::toggleGridLines,
                    onNextLevel = viewModel::startNextLevel,
                )
            }
        }

        if (showSettings) {
            SettingsBottomSheet(
                settings = settings,
                onDismiss = viewModel::closeSettings,
                onSelectTheme = viewModel::setThemePalette,
                onToggleGrid = viewModel::setShowGridLines,
                onToggleHaptics = viewModel::setHapticsEnabled,
                onToggleSound = viewModel::setSoundEnabled,
                onResetProgress = viewModel::resetAllProgress,
            )
        }
    }
}
