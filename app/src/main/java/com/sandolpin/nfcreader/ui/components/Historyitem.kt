package com.sandolpin.nfcreader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sandolpin.nfcreader.domain.model.HistoryEntry
import com.sandolpin.nfcreader.domain.model.HistoryType
import com.sandolpin.nfcreader.domain.model.Station
import com.sandolpin.nfcreader.util.CurrencyFormat
import com.sandolpin.nfcreader.util.DateFormats

/** 利用履歴1件。鉄道のときだけ、乗車駅と降車駅を2行目に出す。 */
@Composable
fun HistoryItem(
    entry: HistoryEntry,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TypeBadge(entry.type)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = entry.type.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = DateFormats.date(entry.date),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyFormat.signedAmount(entry.amount),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "利用後残高:" + CurrencyFormat.yen(entry.balanceAfter),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (entry.type == HistoryType.RAIL && (entry.boarding != null || entry.alighting != null)) {
                Row(
                    modifier = Modifier.padding(start = 52.dp, top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StationLabel(entry.boarding)
                    Text(
                        text = "▶",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    StationLabel(entry.alighting)
                }
            }
        }
    }
}

/** 種別ごとの色つきアイコン。 */
@Composable
private fun TypeBadge(type: HistoryType) {
    val (color, icon) = when (type) {
        HistoryType.PURCHASE -> Color(0xFF00AEEF) to Icons.Filled.ShoppingCart
        HistoryType.CHARGE -> Color(0xFF1FB84A) to Icons.Filled.AddCard
        HistoryType.RAIL -> Color(0xFF00BFA5) to Icons.Filled.Train
        HistoryType.BUS -> Color(0xFFF59E0B) to Icons.Filled.DirectionsBus
        HistoryType.OTHER -> Color(0xFF8E9099) to Icons.Filled.Receipt
    }
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(color, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
    }
}

/** 駅名(コード表にない駅は「E0-01」のようなコード表示)。丸い目印つき。 */
@Composable
private fun StationLabel(station: Station?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(Color(0xFFBDBDBD), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(8.dp).background(Color(0xFFEDEDED), CircleShape))
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = station?.displayName ?: "-",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

private val HistoryType.label: String
    get() = when (this) {
        HistoryType.RAIL -> "鉄道"
        HistoryType.BUS -> "バス"
        HistoryType.PURCHASE -> "物販"
        HistoryType.CHARGE -> "チャージ"
        HistoryType.OTHER -> "その他"
    }