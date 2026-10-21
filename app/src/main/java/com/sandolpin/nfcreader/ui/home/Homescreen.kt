package com.sandolpin.nfcreader.ui.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * 待機画面。カードやNFCタグをかざすよう案内する。
 * 上下の余白(ステータスバー・下部ナビ)は、呼び出し側(Scaffold)が [Modifier.padding] で避けてくれる。
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 上を少し短くして、画面イメージのように中身を上寄りに置く
        Spacer(Modifier.weight(1f))

        ReaderIllustration(withCard = true)
        Spacer(Modifier.height(24.dp))

        Text(
            text = "カードやNFCタグを\n読み取り部にかざしてください",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        HelpButton(onClick = viewModel::showHelp)

        Spacer(Modifier.weight(1.4f))
    }

    if (state.isHelpVisible) {
        HelpDialog(
            nfcStatus = state.nfcStatus,
            onOpenSettings = { openNfcSettings(context) },
            onDismiss = viewModel::hideHelp
        )
    }
}

/** 灰色の丸の中に、黒い丸と「?」を置いたボタン。タップ領域は 56dp 確保する。 */
@Composable
private fun HelpButton(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(56.dp)
            .background(colors.surfaceVariant, CircleShape)
            .semantics { contentDescription = "読み取れないときのヘルプを開く" }
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(colors.onSurface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "?",
                color = colors.surface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** NFCの設定画面を開く。端末に専用の画面がなければ、無線の設定にする。 */
private fun openNfcSettings(context: Context) {
    try {
        context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
    } catch (e: ActivityNotFoundException) {
        runCatching { context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS)) }
    }
}