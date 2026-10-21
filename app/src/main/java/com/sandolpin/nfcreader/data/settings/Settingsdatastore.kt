package com.sandolpin.nfcreader.data.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sandolpin.nfcreader.domain.model.AppSettings
import com.sandolpin.nfcreader.domain.model.HistoryLayout
import com.sandolpin.nfcreader.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// DataStore は1ファイルにつき1インスタンスなので、トップレベルで1つだけ定義する
private val Context.settingsStore by preferencesDataStore(name = "settings")

/** 設定の読み書き(DataStore / Preferences)。保存値が壊れていても既定値に戻して動き続ける。 */
class SettingsDataStore(context: Context) {

    private val store = context.applicationContext.settingsStore

    val settings: Flow<AppSettings> = store.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs -> prefs.toSettings() }

    suspend fun setThemeMode(mode: ThemeMode) {
        store.edit { it[KEY_THEME_MODE] = mode.name }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        store.edit { it[KEY_SOUND_ENABLED] = enabled }
    }

    suspend fun setHistoryLayout(layout: HistoryLayout) {
        store.edit { it[KEY_HISTORY_LAYOUT] = layout.name }
    }

    private fun Preferences.toSettings(): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            themeMode = this[KEY_THEME_MODE]
                ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: defaults.themeMode,
            soundEnabled = this[KEY_SOUND_ENABLED] ?: defaults.soundEnabled,
            historyLayout = this[KEY_HISTORY_LAYOUT]
                ?.let { runCatching { HistoryLayout.valueOf(it) }.getOrNull() } ?: defaults.historyLayout
        )
    }

    private companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val KEY_HISTORY_LAYOUT = stringPreferencesKey("history_layout")
    }
}