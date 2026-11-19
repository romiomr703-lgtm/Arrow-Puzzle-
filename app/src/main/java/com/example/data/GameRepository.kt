package com.example.data

import com.example.model.GameThemePalette
import kotlinx.coroutines.flow.Flow

class GameRepository(
    private val dao: GameProgressDao,
    private val prefs: UserPreferencesRepository,
) {
    val allLevelProgress: Flow<List<LevelProgressEntity>> = dao.observeAllLevelProgress()
    val allDailyProgress: Flow<List<DailyProgressEntity>> = dao.observeAllDailyProgress()
    val settings: Flow<UserSettings> = prefs.settingsFlow

    /**
     * Records level completion and returns true if this was the first time clearing the level.
     */
    suspend fun recordLevelWin(
        levelNumber: Int,
        starsEarned: Int,
        elapsedSeconds: Int,
    ): Boolean {
        val existing = dao.getLevelProgress(levelNumber)
        val isFirstClear = existing == null || !existing.completed
        val bestStars = maxOf(existing?.bestStars ?: 0, starsEarned)
        val bestTime = when {
            existing == null || existing.bestTimeSeconds <= 0 -> elapsedSeconds
            elapsedSeconds < existing.bestTimeSeconds -> elapsedSeconds
            else -> existing.bestTimeSeconds
        }
        dao.upsertLevelProgress(
            LevelProgressEntity(
                levelNumber = levelNumber,
                completed = true,
                bestStars = bestStars,
                bestTimeSeconds = bestTime,
                completedAtMillis = System.currentTimeMillis(),
            )
        )
        if (isFirstClear) {
            prefs.addHints(1)
        }
        return isFirstClear
    }

    suspend fun recordDailyWin(
        dateKey: String,
        starsEarned: Int,
        elapsedSeconds: Int,
    ): Boolean {
        val existing = dao.getDailyProgress(dateKey)
        val isFirstClear = existing == null || !existing.completed
        val bestStars = maxOf(existing?.stars ?: 0, starsEarned)
        val bestTime = when {
            existing == null || existing.bestTimeSeconds <= 0 -> elapsedSeconds
            elapsedSeconds < existing.bestTimeSeconds -> elapsedSeconds
            else -> existing.bestTimeSeconds
        }
        dao.upsertDailyProgress(
            DailyProgressEntity(
                dateKey = dateKey,
                completed = true,
                stars = bestStars,
                bestTimeSeconds = bestTime,
                completedAtMillis = System.currentTimeMillis(),
            )
        )
        if (isFirstClear) {
            prefs.addHints(2)
        }
        return isFirstClear
    }

    suspend fun consumeHint(): Boolean = prefs.decrementHint()

    suspend fun setThemePalette(palette: GameThemePalette) = prefs.setThemePalette(palette)
    suspend fun setShowGridLines(enabled: Boolean) = prefs.setShowGridLines(enabled)
    suspend fun setHapticsEnabled(enabled: Boolean) = prefs.setHapticsEnabled(enabled)
    suspend fun setSoundEnabled(enabled: Boolean) = prefs.setSoundEnabled(enabled)

    suspend fun resetAllProgress() {
        dao.clearAllLevelProgress()
        dao.clearAllDailyProgress()
        prefs.resetHintsToDefault()
    }
}
