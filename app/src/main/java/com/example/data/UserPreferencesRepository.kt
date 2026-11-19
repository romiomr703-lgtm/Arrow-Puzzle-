package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.GameThemePalette
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPrefsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "arrow_escape_settings"
)

data class UserSettings(
    val themePalette: GameThemePalette = GameThemePalette.CLASSIC_INK,
    val showGridLines: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val hintBalance: Int = 5,
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_PALETTE = stringPreferencesKey("theme_palette_v2")
        val SHOW_GRID_LINES = booleanPreferencesKey("show_grid_lines")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HINT_BALANCE = intPreferencesKey("hint_balance")
    }

    val settingsFlow: Flow<UserSettings> = context.userPrefsDataStore.data.map { prefs ->
        UserSettings(
            themePalette = GameThemePalette.fromKey(prefs[Keys.THEME_PALETTE]),
            showGridLines = prefs[Keys.SHOW_GRID_LINES] ?: false,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            hintBalance = prefs[Keys.HINT_BALANCE] ?: 5,
        )
    }

    suspend fun setThemePalette(palette: GameThemePalette) {
        context.userPrefsDataStore.edit { it[Keys.THEME_PALETTE] = palette.storageKey }
    }

    suspend fun setShowGridLines(enabled: Boolean) {
        context.userPrefsDataStore.edit { it[Keys.SHOW_GRID_LINES] = enabled }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.userPrefsDataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.userPrefsDataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun decrementHint(): Boolean {
        var consumed = false
        context.userPrefsDataStore.edit { prefs ->
            val cur = prefs[Keys.HINT_BALANCE] ?: 5
            if (cur > 0) {
                prefs[Keys.HINT_BALANCE] = cur - 1
                consumed = true
            }
        }
        return consumed
    }

    suspend fun addHints(amount: Int) {
        if (amount <= 0) return
        context.userPrefsDataStore.edit { prefs ->
            val cur = prefs[Keys.HINT_BALANCE] ?: 5
            prefs[Keys.HINT_BALANCE] = (cur + amount).coerceAtMost(99)
        }
    }

    suspend fun resetHintsToDefault() {
        context.userPrefsDataStore.edit { prefs ->
            prefs[Keys.HINT_BALANCE] = 5
        }
    }
}
