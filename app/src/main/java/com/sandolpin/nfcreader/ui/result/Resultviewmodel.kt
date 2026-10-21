package com.sandolpin.nfcreader.ui.result

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandolpin.nfcreader.data.repository.CardRepository
import com.sandolpin.nfcreader.domain.model.CardReadResult
import com.sandolpin.nfcreader.domain.model.CardStyle
import com.sandolpin.nfcreader.domain.model.CardSummary
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResultUiState(
    /** すでに保存済みのカードなら、その要約(外観を引き継ぐ・ダイアログの初期値に使う)。 */
    val existing: CardSummary? = null,
    /** 保存ダイアログ。null なら閉じている。 */
    val dialog: SaveDialogState? = null
)

class ResultViewModel(private val cardRepository: CardRepository) : ViewModel() {

    private val _state = MutableStateFlow(ResultUiState())
    val state: StateFlow<ResultUiState> = _state.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    /** 保存の成功・失敗など、画面に一度だけ出すメッセージ。 */
    val messages: Flow<String> = _messages.receiveAsFlow()

    /** 読み取ったカードが保存済みかを調べる。 */
    fun load(cardId: String) {
        viewModelScope.launch {
            val existing = cardRepository.findSummary(cardId)
            _state.update { it.copy(existing = existing) }
        }
    }

    /** 保存済みのカードなら、名前と外観を初期値に入れて開く。新規なら、空の名前と緑のスタイル。 */
    fun openSaveDialog() {
        val existing = _state.value.existing
        val initial = if (existing != null) {
            SaveDialogState(name = existing.name, selection = AppearanceSelection.KeepExisting(existing.appearance))
        } else {
            SaveDialogState()
        }
        _state.update { it.copy(dialog = initial) }
    }

    fun dismissDialog() {
        // 保存中に閉じると、結果が分からなくなるので閉じさせない
        if (_state.value.dialog?.isSaving == true) return
        _state.update { it.copy(dialog = null) }
    }

    fun onNameChange(name: String) = updateDialog { it.copy(name = name.take(MAX_NAME_LENGTH)) }

    fun onSelectStyle(style: CardStyle) = updateDialog { it.copy(selection = AppearanceSelection.Preset(style)) }

    fun onImagePicked(uri: Uri) = updateDialog { it.copy(selection = AppearanceSelection.NewImage(uri)) }

    fun save(result: CardReadResult) {
        val dialog = _state.value.dialog ?: return
        if (!dialog.canSave) return

        updateDialog { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                cardRepository.save(result, dialog.name, dialog.selection.toInput())
                val saved = cardRepository.findSummary(result.cardId)
                _state.update { it.copy(existing = saved, dialog = null) }
                _messages.send("保存しました")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                updateDialog { it.copy(isSaving = false) }
                _messages.send("保存できませんでした")
            }
        }
    }

    private fun updateDialog(transform: (SaveDialogState) -> SaveDialogState) {
        _state.update { state -> state.copy(dialog = state.dialog?.let(transform)) }
    }

    private companion object {
        const val MAX_NAME_LENGTH = 30
    }
}