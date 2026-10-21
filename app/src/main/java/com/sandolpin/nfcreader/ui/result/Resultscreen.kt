package com.sandolpin.nfcreader.ui.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sandolpin.nfcreader.domain.model.CardAppearance
import com.sandolpin.nfcreader.domain.model.CardReadResult
import com.sandolpin.nfcreader.domain.model.CardStyle
import com.sandolpin.nfcreader.ui.components.CardDetailContent

/**
 * 読み取り結果画面。ヘッダーは「戻る / 読み取り結果 / 保存」。
 * 上下の余白(ステータスバー・下部ナビ)は、呼び出し側(Scaffold)が避けてくれる。
 */
@Composable
fun ResultScreen(
    result: CardReadResult,
    viewModel: ResultViewModel,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // 読み取ったカードが保存済みか調べる(別のカードを読み取ったときもやり直す)
    LaunchedEffect(result.cardId) { viewModel.load(result.cardId) }
    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        ResultHeader(onBack = onBack, onSave = viewModel::openSaveDialog)
        CardDetailContent(
            // 保存済みなら保存した外観、未保存なら既定の緑
            appearance = state.existing?.appearance ?: CardAppearance.Preset(CardStyle.GREEN),
            balance = result.balance,
            cardId = result.cardId,
            history = result.history,
            modifier = Modifier.weight(1f)
        )
    }

    state.dialog?.let { dialog ->
        SaveCardDialog(
            state = dialog,
            onNameChange = viewModel::onNameChange,
            onSelectStyle = viewModel::onSelectStyle,
            onImagePicked = viewModel::onImagePicked,
            onDismiss = viewModel::dismissDialog,
            onSave = { viewModel.save(result) }
        )
    }
}

@Composable
private fun ResultHeader(
    onBack: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
        }
        Text(
            text = "読み取り結果",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Button(onClick = onSave, modifier = Modifier.height(40.dp)) {
            Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(6.dp))
            Text("保存", fontWeight = FontWeight.Bold)
        }
    }
}