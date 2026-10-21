package com.sandolpin.nfcreader.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandolpin.nfcreader.data.nfc.NfcStatusProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * 待機画面の状態。NFCのオン/オフは [NfcStatusProvider] が監視していて、設定アプリで切り替えるとすぐ反映される。
 * (タグの読み取りと結果画面への遷移は、結果画面を実装するときに追加する)
 */
class HomeViewModel(statusProvider: NfcStatusProvider) : ViewModel() {

    private val helpVisible = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(statusProvider.observe(), helpVisible) { status, help ->
        HomeUiState(nfcStatus = status, isHelpVisible = help)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(nfcStatus = statusProvider.current())
    )

    fun showHelp() {
        helpVisible.value = true
    }

    fun hideHelp() {
        helpVisible.value = false
    }
}