package com.sandolpin.nfcreader.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sandolpin.nfcreader.domain.model.HistoryLayout
import com.sandolpin.nfcreader.domain.model.ThemeMode

/**
 * 設定画面。項目は3つで、どれも変更するとすぐ保存・反映される(「保存」ボタンは無い)。
 * 上下の余白(ステータスバー・下部ナビ)は、呼び出し側(Scaffold)が避けてくれる。
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "設定",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )

        // 読み込み前は項目を出さない(デフォルト値が一瞬見えるのを避ける)
        val current = settings ?: return@Column

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsCard(title = "ダークモード") {
                SegmentedChoices(
                    options = listOf(
                        ThemeMode.LIGHT to "OFF",
                        ThemeMode.DARK to "ON",
                        ThemeMode.SYSTEM to "システム"
                    ),
                    selected = current.themeMode,
                    onSelect = viewModel::onThemeModeChange
                )
            }

            SettingsCard(title = "カード読み取り時に音を鳴らす") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (current.soundEnabled) "鳴らす" else "鳴らさない",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = current.soundEnabled,
                        onCheckedChange = viewModel::onSoundEnabledChange
                    )
                }
            }

            SettingsCard(title = "履歴画面のスタイル") {
                SegmentedChoices(
                    options = listOf(
                        HistoryLayout.GRID to "グリッド",
                        HistoryLayout.LIST to "リスト"
                    ),
                    selected = current.historyLayout,
                    onSelect = viewModel::onHistoryLayoutChange
                )
            }
        }
    }
}

/** 1項目ぶんの枠。見出しと、その下の操作部品を縦に並べる。 */
@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

/** 横並びのボタンから1つ選ぶ部品(セグメントボタン)。 */
@Composable
private fun <T> SegmentedChoices(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (value, label) ->
            SegmentedButton(
                selected = value == selected,
                onClick = { onSelect(value) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
            ) {
                Text(label)
            }
        }
    }
}