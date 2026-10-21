package com.sandolpin.nfcreader.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sandolpin.nfcreader.ui.components.CardDetailContent
import com.sandolpin.nfcreader.ui.navigation.CardTransition
import com.sandolpin.nfcreader.ui.result.SaveCardDialog

/**
 * 保存済みカードの詳細画面。本体(外観・残高・カードID・利用履歴)は読み取り結果画面と共通で、
 * ヘッダーだけが「戻る / カード名 / 編集 / 削除」になる。
 * 上下の余白(ステータスバー・下部ナビ)は、呼び出し側(Scaffold)が避けてくれる。
 */
@Composable
fun CardDetailScreen(
    viewModel: CardDetailViewModel,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    transition: CardTransition,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val card = state.card

    // 存在しないカードを開いた場合は戻る。削除中は、削除完了の通知(Deleted)で1回だけ戻る
    LaunchedEffect(state.isLoading, card, state.isDeleting) {
        if (!state.isLoading && card == null && !state.isDeleting) onBack()
    }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                CardDetailEvent.Deleted -> onBack()
                is CardDetailEvent.Message -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(event.text)
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        DetailHeader(
            title = card?.name.orEmpty(),
            onBack = onBack,
            onEdit = viewModel::openEdit,
            onDelete = viewModel::requestDelete,
            actionsEnabled = card != null && !state.isDeleting
        )
        if (card != null) {
            CardDetailContent(
                appearance = card.appearance,
                balance = card.balance,
                cardId = card.cardId,
                history = card.history,
                modifier = Modifier.weight(1f),
                // 一覧のカード画像と同じキーを付けて、画像が一覧から飛んでくるように見せる
                appearanceModifier = transition.modifierFor(card.cardId)
            )
        }
    }

    state.editDialog?.let { dialog ->
        SaveCardDialog(
            state = dialog,
            title = "カードを編集",
            confirmLabel = "変更を保存する",
            onNameChange = viewModel::onNameChange,
            onSelectStyle = viewModel::onSelectStyle,
            onImagePicked = viewModel::onImagePicked,
            onDismiss = viewModel::dismissEdit,
            onSave = viewModel::saveEdit
        )
    }

    if (state.showDeleteConfirm && card != null) {
        DeleteCardDialog(
            cardName = card.name,
            appearance = card.appearance,
            isDeleting = state.isDeleting,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete
        )
    }
}

@Composable
private fun DetailHeader(
    title: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    actionsEnabled: Boolean
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)
                .background(colors.surfaceVariant, CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        // 編集は灰色、削除は破壊的な操作なので赤。どちらも文字色(白系)が読める濃さにしてある
        IconButton(
            onClick = onEdit,
            enabled = actionsEnabled,
            modifier = Modifier
                .size(44.dp)
                .background(colors.onSurfaceVariant, CircleShape)
        ) {
            Icon(Icons.Filled.Edit, contentDescription = "カードを編集", tint = colors.surface)
        }
        IconButton(
            onClick = onDelete,
            enabled = actionsEnabled,
            modifier = Modifier
                .size(44.dp)
                .background(colors.error, CircleShape)
        ) {
            Icon(Icons.Filled.Delete, contentDescription = "カードを削除", tint = colors.onError)
        }
    }
}