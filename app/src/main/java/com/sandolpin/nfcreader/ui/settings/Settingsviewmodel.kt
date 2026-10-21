package com.sandolpin.nfcreader.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandolpin.nfcreader.data.repository.SettingsRepository
import com.sandolpin.nfcreader.domain.model.AppSettings
import com.sandolpin.nfcreader.domain.model.HistoryLayout
import com.sandolpin.nfcreader.domain.model.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 設定画面の ViewModel。設定は DataStore に保存され、変更はそのまま流れ戻ってくる(Flow)ので、
 * ここで状態を二重に持たない。アプリ全体のテーマ・履歴画面・読み取り音は、同じ設定を見て自動で変わる。
 */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    /** 読み込み前は null(画面側は何も出さず、設定が一瞬デフォルト値でちらつくのを防ぐ)。 */
    val settings: StateFlow<AppSettings?> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun onThemeModeChange(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun onSoundEnabledChange(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSoundEnabled(enabled) }
    }

    fun onHistoryLayoutChange(layout: HistoryLayout) {
        viewModelScope.launch { settingsRepository.setHistoryLayout(layout) }
    }
}