package com.sandolpin.nfcreader.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandolpin.nfcreader.data.repository.CardRepository
import com.sandolpin.nfcreader.data.repository.SettingsRepository
import com.sandolpin.nfcreader.domain.model.CardSummary
import com.sandolpin.nfcreader.domain.model.HistoryLayout
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HistoryUiState(
    /** 最初の読み込みが終わるまで true。この間は「カードなし」の表示を出さない(ちらつき防止)。 */
    val isLoading: Boolean = true,
    /** 最終更新が新しい順。 */
    val cards: List<CardSummary> = emptyList(),
    val layout: HistoryLayout = HistoryLayout.LIST
)

/** 履歴画面。保存済みカードの一覧と、設定の「リスト/グリッド」を合わせて持つ。 */
class HistoryViewModel(
    cardRepository: CardRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = combine(
        cardRepository.observeSummaries(),
        settingsRepository.settings
    ) { cards, settings ->
        HistoryUiState(isLoading = false, cards = cards, layout = settings.historyLayout)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState()
    )
}