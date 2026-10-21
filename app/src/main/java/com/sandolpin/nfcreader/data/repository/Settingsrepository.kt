package com.sandolpin.nfcreader.data.repository

import com.sandolpin.nfcreader.data.settings.SettingsDataStore
import com.sandolpin.nfcreader.domain.model.AppSettings
import com.sandolpin.nfcreader.domain.model.HistoryLayout
import com.sandolpin.nfcreader.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/** アプリの設定。ViewModel はこのクラスだけを見ればよく、保存方法(DataStore)は隠す。 */
class SettingsRepository(private val dataStore: SettingsDataStore) {

    val settings: Flow<AppSettings> = dataStore.settings

    /** 最初の1回分だけ読む(起動時、スプラッシュを維持してテーマのちらつきを防ぐ判断などに使う)。 */
    suspend fun load(): AppSettings = dataStore.settings.first()

    suspend fun setThemeMode(mode: ThemeMode) = dataStore.setThemeMode(mode)
    suspend fun setSoundEnabled(enabled: Boolean) = dataStore.setSoundEnabled(enabled)
    suspend fun setHistoryLayout(layout: HistoryLayout) = dataStore.setHistoryLayout(layout)
}