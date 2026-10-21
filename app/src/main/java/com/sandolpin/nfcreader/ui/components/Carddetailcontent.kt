package com.sandolpin.nfcreader.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sandolpin.nfcreader.domain.model.CardAppearance
import com.sandolpin.nfcreader.domain.model.HistoryEntry
import com.sandolpin.nfcreader.util.CurrencyFormat

/**
 * カードの本体(外観・残高・カードID・利用履歴)。読み取り結果画面と、保存済みカードの詳細画面で共有する。
 * 上のヘッダー(戻る・タイトル・保存/編集/削除)は画面ごとに違うので含めず、呼び出し側が置く。
 *
 * @param appearanceModifier カード画像に渡す Modifier。一覧から詳細への shared element transition は、
 *   ここに sharedElement の指定を渡して実現する。
 */
@Composable
fun CardDetailContent(
    appearance: CardAppearance,
    balance: Int,
    cardId: String,
    history: List<HistoryEntry>,
    modifier: Modifier = Modifier,
    appearanceModifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            CardAppearanceView(
                appearance = appearance,
                modifier = appearanceModifier.widthIn(max = 260.dp).fillMaxWidth(0.6f)
            )
        }
        item { BalanceBox(balance) }
        item {
            OutlinedTextField(
                value = cardId,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                label = { Text("カードID") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item { HistoryHeader() }

        if (history.isEmpty()) {
            item {
                Text(
                    text = "利用履歴はありません",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            itemsIndexed(history, key = { index, _ -> index }) { _, entry ->
                HistoryItem(entry)
            }
        }
    }
}

/** 角丸の太い枠の中に「残高」と金額を並べた表示。 */
@Composable
private fun BalanceBox(balance: Int) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(3.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("残高", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = CurrencyFormat.yen(balance),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HistoryHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(8.dp))
        Text("利用履歴", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(12.dp))
        HorizontalDivider(Modifier.weight(1f))
    }
}