package com.sandolpin.nfcreader.ui.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sandolpin.nfcreader.domain.model.CardSummary
import com.sandolpin.nfcreader.ui.components.CardAppearanceView
import com.sandolpin.nfcreader.ui.navigation.CardTransition
import com.sandolpin.nfcreader.util.CurrencyFormat
import com.sandolpin.nfcreader.util.DateFormats

/**
 * 履歴一覧の1枚。名前、カード画像、残高、最終更新日時を縦に並べる。
 * [compact] はグリッド(2列)用で、幅が狭いぶん、文字を小さくして画像を少し大きく見せる。
 *
 * カード画像には、詳細画面へ飛ばす shared element のキーを付ける([transition])。
 */
@Composable
fun SavedCardItem(
    card: CardSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    transition: CardTransition? = null
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(if (compact) 20.dp else 24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 12.dp else 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = card.name,
                style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            // sharedElement は、大きさを決める Modifier より前に置く
            val sharedModifier = transition?.modifierFor(card.cardId) ?: Modifier
            CardAppearanceView(
                appearance = card.appearance,
                bordered = true,
                modifier = sharedModifier.fillMaxWidth(if (compact) 0.92f else 0.62f)
            )
            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = CurrencyFormat.yen(card.balance),
                    style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "最終更新:" + DateFormats.dateTime(card.lastUpdated),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}