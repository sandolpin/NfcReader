package com.sandolpin.nfcreader.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
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
    // 縮み具合。0 = 大きいまま、1 = いちばん小さい。スクロールに合わせて変える
    var collapse by remember { mutableFloatStateOf(0f) }
    val collapseRangePx = with(LocalDensity.current) { COLLAPSE_SCROLL_RANGE.toPx() }

    // 上へスクロール(指を上に動かす)したら、リストより先にカードを縮める。
    // 下へスクロールして、リストが一番上まで戻ったあとの余りで、カードを大きく戻す
    val connection = remember(collapseRangePx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y >= 0f || collapse >= 1f) return Offset.Zero
                val next = (collapse - available.y / collapseRangePx).coerceAtMost(1f)
                val used = -(next - collapse) * collapseRangePx
                collapse = next
                return Offset(0f, used)
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (available.y <= 0f || collapse <= 0f) return Offset.Zero
                val next = (collapse - available.y / collapseRangePx).coerceAtLeast(0f)
                val used = (collapse - next) * collapseRangePx
                collapse = next
                return Offset(0f, used)
            }
        }
    }

    Column(
        modifier = modifier.nestedScroll(connection),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // カードは、リストの外に固定する(スクロールで画面から消えない)。大きさだけが変わる
        CardAppearanceView(
            appearance = appearance,
            modifier = appearanceModifier
                .padding(top = 8.dp, bottom = 12.dp)
                .widthIn(max = lerp(LARGE_MAX_WIDTH, SMALL_MAX_WIDTH, collapse))
                .fillMaxWidth(lerp(LARGE_WIDTH_FRACTION, SMALL_WIDTH_FRACTION, collapse))
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
}

/** カードが大きい状態から小さい状態になるまでの、スクロール量。 */
private val COLLAPSE_SCROLL_RANGE = 180.dp
private val LARGE_MAX_WIDTH = 380.dp
private val SMALL_MAX_WIDTH = 190.dp
private const val LARGE_WIDTH_FRACTION = 0.88f
private const val SMALL_WIDTH_FRACTION = 0.46f

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