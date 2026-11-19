package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DailyProgressEntity
import com.example.data.GameRepository
import com.example.data.LevelProgressEntity
import com.example.data.UserPreferencesRepository
import com.example.data.UserSettings
import com.example.engine.PuzzleGenerator
import com.example.engine.SoundEffectsPlayer
import com.example.model.Arrow
import com.example.model.Difficulty
import com.example.model.GameThemePalette
import com.example.model.GridPoint
import com.example.model.LevelSummary
import com.example.model.PuzzleLevel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    GAME,
}

enum class GameStatus {
    PLAYING,
    WON,
    LOST,
}

enum class HapticCue {
    ESCAPE,
    BLOCKED,
    HINT,
    VICTORY,
}

data class EscapingArrowAnim(
    val arrow: Arrow,
    val startTimeNanos: Long,
)

data class BlockedCollisionAnim(
    val arrowId: Int,
    val blockingArrowId: Int,
    val blockingCell: GridPoint,
    val distanceCells: Int,
    val startTimeNanos: Long,
)

data class HomeUiState(
    val highestUnlockedLevel: Int = 1,
    val solvedCount: Int = 0,
    val totalStars: Int = 0,
    val hintBalance: Int = 5,
    val dailyDateKey: String = PuzzleGenerator.todayDateKey(),
    val dailyCompleted: Boolean = false,
    val dailyStars: Int = 0,
    val selectedFilter: Difficulty? = null,
    val visibleLevelCount: Int = 40,
    val levels: List<LevelSummary> = emptyList(),
    val heroPreviewLevel: PuzzleLevel = PuzzleGenerator.generateLevel(1),
)

data class GameUiState(
    val level: PuzzleLevel = PuzzleGenerator.generateLevel(1),
    val remainingArrows: List<Arrow> = emptyList(),
    val escapingArrows: List<EscapingArrowAnim> = emptyList(),
    val collisionAnim: BlockedCollisionAnim? = null,
    val hearts: Int = 3,
    val maxHearts: Int = 3,
    val comboCount: Int = 0,
    val highlightedHintArrowId: Int? = null,
    val statusBanner: String? = null,
    val elapsedSeconds: Int = 0,
    val status: GameStatus = GameStatus.PLAYING,
    val starsEarned: Int = 3,
    val earnedHintBonus: Boolean = false,
)

class ArrowPuzzleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository = GameRepository(
        dao = AppDatabase.getInstance(application).gameProgressDao(),
        prefs = UserPreferencesRepository(application),
    )

    private val soundPlayer = SoundEffectsPlayer()

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    private val _selectedFilter = MutableStateFlow<Difficulty?>(null)
    private val _visibleLevelCount = MutableStateFlow(40)

    private val _gameState = MutableStateFlow(GameUiState())
    val gameState: StateFlow<GameUiState> = _gameState.asStateFlow()

    private val _hapticEvents = MutableSharedFlow<HapticCue>(extraBufferCapacity = 8)
    val hapticEvents: SharedFlow<HapticCue> = _hapticEvents.asSharedFlow()

    val settings: StateFlow<UserSettings> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = UserSettings(),
    )

    private var cachedHeroLevelNum = -1
    private var cachedHeroPuzzle: PuzzleLevel = PuzzleGenerator.generateLevel(1)

    val homeState: StateFlow<HomeUiState> = combine(
        repository.allLevelProgress,
        repository.allDailyProgress,
        settings,
        _selectedFilter,
        _visibleLevelCount,
    ) { progressList, dailyList, userSettings, filter, visibleCount ->
        buildHomeState(progressList, dailyList, userSettings, filter, visibleCount)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = HomeUiState(),
    )

    private var timerJob: Job? = null
    private var bannerJob: Job? = null
    private var lastEscapeTimestampMs: Long = 0L

    private fun buildHomeState(
        progressList: List<LevelProgressEntity>,
        dailyList: List<DailyProgressEntity>,
        userSettings: UserSettings,
        filter: Difficulty?,
        visibleCount: Int,
    ): HomeUiState {
        val progressMap = progressList.associateBy { it.levelNumber }
        val completedLevels = progressList.filter { it.completed }
        val maxCompleted = completedLevels.maxOfOrNull { it.levelNumber } ?: 0
        val highestUnlocked = maxCompleted + 1
        val totalLevelsToShow = maxOf(visibleCount, highestUnlocked + 8)

        val todayKey = PuzzleGenerator.todayDateKey()
        val todayProgress = dailyList.firstOrNull { it.dateKey == todayKey }

        if (cachedHeroLevelNum != highestUnlocked) {
            cachedHeroLevelNum = highestUnlocked
            cachedHeroPuzzle = PuzzleGenerator.generateLevel(highestUnlocked)
        }

        val summaries = (1..totalLevelsToShow).map { num ->
            val spec = PuzzleGenerator.specForLevel(num)
            val entity = progressMap[num]
            LevelSummary(
                levelNumber = num,
                difficulty = spec.difficulty,
                shape = spec.shape,
                unlocked = num <= highestUnlocked,
                completed = entity?.completed == true,
                bestStars = entity?.bestStars ?: 0,
                bestTimeSeconds = entity?.bestTimeSeconds ?: 0,
            )
        }.let { all ->
            if (filter == null) all else all.filter { it.difficulty == filter }
        }

        return HomeUiState(
            highestUnlockedLevel = highestUnlocked,
            solvedCount = completedLevels.size + dailyList.count { it.completed },
            totalStars = completedLevels.sumOf { it.bestStars } + dailyList.sumOf { it.stars },
            hintBalance = userSettings.hintBalance,
            dailyDateKey = todayKey,
            dailyCompleted = todayProgress?.completed == true,
            dailyStars = todayProgress?.stars ?: 0,
            selectedFilter = filter,
            visibleLevelCount = totalLevelsToShow,
            levels = summaries,
            heroPreviewLevel = cachedHeroPuzzle,
        )
    }

    fun selectDifficultyFilter(difficulty: Difficulty?) {
        _selectedFilter.value = difficulty
    }

    fun loadMoreLevels() {
        _visibleLevelCount.update { it + 20 }
    }

    fun openSettings() {
        _showSettings.value = true
    }

    fun closeSettings() {
        _showSettings.value = false
    }

    fun navigateHome() {
        stopTimer()
        _currentScreen.value = AppScreen.HOME
    }

    fun startLevel(levelNumber: Int) {
        val puzzle = PuzzleGenerator.generateLevel(levelNumber)
        beginSession(puzzle)
    }

    fun startDailyChallenge() {
        val puzzle = PuzzleGenerator.generateDailyLevel()
        beginSession(puzzle)
    }

    fun restartCurrentLevel() {
        val current = _gameState.value.level
        beginSession(current)
    }

    fun startNextLevel() {
        val current = _gameState.value.level
        if (current.isDaily) {
            startLevel(homeState.value.highestUnlockedLevel)
        } else {
            startLevel(current.levelNumber + 1)
        }
    }

    private fun beginSession(puzzle: PuzzleLevel) {
        stopTimer()
        bannerJob?.cancel()
        lastEscapeTimestampMs = 0L
        _gameState.value = GameUiState(
            level = puzzle,
            remainingArrows = puzzle.arrows,
            escapingArrows = emptyList(),
            collisionAnim = null,
            hearts = 3,
            maxHearts = 3,
            comboCount = 0,
            highlightedHintArrowId = null,
            statusBanner = null,
            elapsedSeconds = 0,
            status = GameStatus.PLAYING,
            starsEarned = 3,
            earnedHintBonus = false,
        )
        _currentScreen.value = AppScreen.GAME
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val cur = _gameState.value
                if (cur.status == GameStatus.PLAYING) {
                    _gameState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
                } else {
                    break
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun onArrowTapped(arrowId: Int) {
        val state = _gameState.value
        if (state.status != GameStatus.PLAYING) return

        val tappedArrow = state.remainingArrows.firstOrNull { it.id == arrowId } ?: return
        val collision = PuzzleGenerator.findBlockingCollision(
            arrow = tappedArrow,
            remainingArrows = state.remainingArrows,
            cols = state.level.cols,
            rows = state.level.rows,
        )

        val nowNanos = System.nanoTime()
        val curSettings = settings.value

        if (collision == null) {
            // Arrow is FREE! Launch it smoothly off the board!
            val nowMs = System.currentTimeMillis()
            val nextCombo = if (nowMs - lastEscapeTimestampMs < 2400L) state.comboCount + 1 else 0
            lastEscapeTimestampMs = nowMs

            if (curSettings.soundEnabled) soundPlayer.playEscape(nextCombo)
            if (curSettings.hapticsEnabled) _hapticEvents.tryEmit(HapticCue.ESCAPE)

            val updatedRemaining = state.remainingArrows.filter { it.id != arrowId }
            val escaping = EscapingArrowAnim(arrow = tappedArrow, startTimeNanos = nowNanos)
            val updatedEscaping = state.escapingArrows + escaping
            val clearedHint = if (state.highlightedHintArrowId == arrowId) null else state.highlightedHintArrowId

            _gameState.update {
                it.copy(
                    remainingArrows = updatedRemaining,
                    escapingArrows = updatedEscaping,
                    collisionAnim = null,
                    comboCount = nextCombo,
                    highlightedHintArrowId = clearedHint,
                    statusBanner = if (clearedHint == null) null else it.statusBanner,
                )
            }

            // Clean up finished escape animation after it completes
            viewModelScope.launch {
                delay(420L)
                _gameState.update { cur ->
                    cur.copy(escapingArrows = cur.escapingArrows.filter { it.arrow.id != arrowId })
                }
            }

            if (updatedRemaining.isEmpty()) {
                handleLevelWon()
            }
        } else {
            // Arrow is BLOCKED! Bounce toward the blocker and deduct a heart (unless Zen Mode)
            if (curSettings.soundEnabled) soundPlayer.playBlocked()
            if (curSettings.hapticsEnabled) _hapticEvents.tryEmit(HapticCue.BLOCKED)

            val anim = BlockedCollisionAnim(
                arrowId = collision.arrowId,
                blockingArrowId = collision.blockingArrowId,
                blockingCell = collision.blockingCell,
                distanceCells = collision.distanceCells,
                startTimeNanos = nowNanos,
            )

            val newHearts = (state.hearts - 1).coerceAtLeast(0)
            val newStatus = if (newHearts <= 0) GameStatus.LOST else GameStatus.PLAYING

            if (newStatus == GameStatus.LOST) {
                stopTimer()
            }

            _gameState.update {
                it.copy(
                    collisionAnim = anim,
                    hearts = newHearts,
                    comboCount = 0,
                    status = newStatus,
                )
            }

            viewModelScope.launch {
                delay(380L)
                _gameState.update { cur ->
                    if (cur.collisionAnim?.startTimeNanos == nowNanos) {
                        cur.copy(collisionAnim = null)
                    } else cur
                }
            }
        }
    }

    private fun handleLevelWon() {
        stopTimer()
        val state = _gameState.value
        val stars = when {
            state.hearts >= 3 -> 3
            state.hearts == 2 -> 2
            else -> 1
        }

        viewModelScope.launch {
            // Let the final arrow finish zipping off the board before celebration card pops
            delay(360L)
            if (settings.value.soundEnabled) soundPlayer.playVictory()
            if (settings.value.hapticsEnabled) _hapticEvents.tryEmit(HapticCue.VICTORY)

            val earnedHint = if (state.level.isDaily && state.level.dailyDateKey != null) {
                repository.recordDailyWin(
                    dateKey = state.level.dailyDateKey,
                    starsEarned = stars,
                    elapsedSeconds = state.elapsedSeconds,
                )
            } else {
                repository.recordLevelWin(
                    levelNumber = state.level.levelNumber,
                    starsEarned = stars,
                    elapsedSeconds = state.elapsedSeconds,
                )
            }

            _gameState.update {
                it.copy(
                    status = GameStatus.WON,
                    starsEarned = stars,
                    earnedHintBonus = earnedHint,
                    highlightedHintArrowId = null,
                    statusBanner = null,
                )
            }
        }
    }

    fun requestHint() {
        val state = _gameState.value
        if (state.status != GameStatus.PLAYING) return
        if (state.remainingArrows.isEmpty()) return

        // If a hint is already glowing on screen, don't spend another hint
        if (state.highlightedHintArrowId != null &&
            state.remainingArrows.any { it.id == state.highlightedHintArrowId }
        ) {
            showTemporaryBanner("Tap the glowing arrow to clear a path")
            return
        }

        viewModelScope.launch {
            val consumed = repository.consumeHint()
            if (!consumed) {
                showTemporaryBanner("Complete levels to earn more hints")
                return@launch
            }
            val best = PuzzleGenerator.findBestHintArrow(
                remainingArrows = _gameState.value.remainingArrows,
                cols = _gameState.value.level.cols,
                rows = _gameState.value.level.rows,
            )
            if (best != null) {
                if (settings.value.soundEnabled) soundPlayer.playHint()
                if (settings.value.hapticsEnabled) _hapticEvents.tryEmit(HapticCue.HINT)
                _gameState.update { it.copy(highlightedHintArrowId = best.id) }
                showTemporaryBanner("Tap the glowing arrow to clear a path")
            }
        }
    }

    private fun showTemporaryBanner(message: String) {
        bannerJob?.cancel()
        _gameState.update { it.copy(statusBanner = message) }
        bannerJob = viewModelScope.launch {
            delay(2800L)
            _gameState.update { it.copy(statusBanner = null) }
        }
    }

    fun toggleGridLines() {
        viewModelScope.launch {
            repository.setShowGridLines(!settings.value.showGridLines)
        }
    }

    fun setThemePalette(palette: GameThemePalette) {
        viewModelScope.launch { repository.setThemePalette(palette) }
    }

    fun setShowGridLines(enabled: Boolean) {
        viewModelScope.launch { repository.setShowGridLines(enabled) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setHapticsEnabled(enabled) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setSoundEnabled(enabled) }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            repository.resetAllProgress()
            _currentScreen.value = AppScreen.HOME
        }
    }
}
