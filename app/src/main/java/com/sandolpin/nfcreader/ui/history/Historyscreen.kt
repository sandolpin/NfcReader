package com.sandolpin.nfcreader.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sandolpin.nfcreader.domain.model.CardSummary
import com.sandolpin.nfcreader.domain.model.HistoryLayout
import com.sandolpin.nfcreader.ui.navigation.CardTransition

/**
 * 履歴画面(中央のタブ)。保存済みカードを、設定に応じてリストまたはグリッドで並べる。
 * 上下の余白(ステータスバー・下部ナビ)は、呼び出し側(Scaffold)が避けてくれる。
 */
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onOpenCard: (cardId: String) -> Unit,
    transition: CardTransition,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "履歴",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )

        when {
            state.isLoading -> Unit // 読み込み中は空の状態を出さない
            state.cards.isEmpty() -> EmptyState()
            else -> CardList(
                cards = state.cards,
                layout = state.layout,
                onOpenCard = onOpenCard,
                transition = transition
            )
        }
    }
}

@Composable
private fun CardList(
    cards: List<CardSummary>,
    layout: HistoryLayout,
    onOpenCard: (String) -> Unit,
    transition: CardTransition
) {
    val contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp)

    when (layout) {
        HistoryLayout.LIST -> LazyColumn(
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(cards, key = { it.cardId }) { card ->
                SavedCardItem(
                    card = card,
                    onClick = { onOpenCard(card.cardId) },
                    transition = transition
                )
            }
        }

        HistoryLayout.GRID -> LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(cards, key = { it.cardId }) { card ->
                SavedCardItem(
                    card = card,
                    onClick = { onOpenCard(card.cardId) },
                    compact = true,
                    transition = transition
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "保存したカードはまだありません\n読み取り結果の「保存」から追加できます",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}