package com.sandolpin.nfcreader.ui.history

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandolpin.nfcreader.data.repository.CardRepository
import com.sandolpin.nfcreader.domain.model.CardStyle
import com.sandolpin.nfcreader.domain.model.SavedCard
import com.sandolpin.nfcreader.ui.result.AppearanceSelection
import com.sandolpin.nfcreader.ui.result.SaveDialogState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CardDetailUiState(
    /** 最初の読み込みが終わるまで true。 */
    val isLoading: Boolean = true,
    /** 保存済みカード。読み込み後も null なら、存在しない(削除済み)。 */
    val card: SavedCard? = null,
    /** 編集ダイアログ(保存ダイアログを再利用)。null なら閉じている。 */
    val editDialog: SaveDialogState? = null,
    val showDeleteConfirm: Boolean = false,
    val isDeleting: Boolean = false
)

/** 画面に一度だけ伝える出来事。 */
sealed interface CardDetailEvent {
    data class Message(val text: String) : CardDetailEvent
    /** 削除が終わった。画面を閉じる。 */
    data object Deleted : CardDetailEvent
}

class CardDetailViewModel(
    private val cardId: String,
    private val cardRepository: CardRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CardDetailUiState())
    val state: StateFlow<CardDetailUiState> = _state.asStateFlow()

    private val _events = Channel<CardDetailEvent>(Channel.BUFFERED)
    val events: Flow<CardDetailEvent> = _events.receiveAsFlow()

    init {
        // カードや履歴が変わる(編集・再保存・削除)たびに、最新の内容が流れてくる
        viewModelScope.launch {
            cardRepository.observeCard(cardId).collect { card ->
                _state.update { it.copy(isLoading = false, card = card) }
            }
        }
    }

    // ---- 編集 ----

    fun openEdit() {
        val card = _state.value.card ?: return
        _state.update {
            it.copy(editDialog = SaveDialogState(name = card.name, selection = AppearanceSelection.KeepExisting(card.appearance)))
        }
    }

    fun dismissEdit() {
        if (_state.value.editDialog?.isSaving == true) return
        _state.update { it.copy(editDialog = null) }
    }

    fun onNameChange(name: String) = updateEdit { it.copy(name = name.take(MAX_NAME_LENGTH)) }

    fun onSelectStyle(style: CardStyle) = updateEdit { it.copy(selection = AppearanceSelection.Preset(style)) }

    fun onImagePicked(uri: Uri) = updateEdit { it.copy(selection = AppearanceSelection.NewImage(uri)) }

    fun saveEdit() {
        val dialog = _state.value.editDialog ?: return
        if (!dialog.canSave) return

        updateEdit { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                cardRepository.update(cardId, dialog.name, dialog.selection.toInput())
                _state.update { it.copy(editDialog = null) }
                _events.send(CardDetailEvent.Message("更新しました"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                updateEdit { it.copy(isSaving = false) }
                _events.send(CardDetailEvent.Message("更新できませんでした"))
            }
        }
    }

    // ---- 削除 ----

    fun requestDelete() {
        _state.update { it.copy(showDeleteConfirm = true) }
    }

    fun cancelDelete() {
        if (_state.value.isDeleting) return
        _state.update { it.copy(showDeleteConfirm = false) }
    }

    fun confirmDelete() {
        if (_state.value.isDeleting) return
        _state.update { it.copy(isDeleting = true) }
        viewModelScope.launch {
            try {
                cardRepository.delete(cardId)
                // 削除中は isDeleting のままにして、画面側が「カードなし」を見て二重に戻らないようにする
                _events.send(CardDetailEvent.Deleted)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isDeleting = false, showDeleteConfirm = false) }
                _events.send(CardDetailEvent.Message("削除できませんでした"))
            }
        }
    }

    private fun updateEdit(transform: (SaveDialogState) -> SaveDialogState) {
        _state.update { state -> state.copy(editDialog = state.editDialog?.let(transform)) }
    }

    private companion object {
        const val MAX_NAME_LENGTH = 30
    }
}