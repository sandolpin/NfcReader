package com.sandolpin.nfcreader.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.sandolpin.nfcreader.domain.model.NfcStatus

/**
 * 「読み取れないときは」ダイアログ。上のバッジは端末のNFC状態に合わせて出し分ける。
 * NFCがオフのときだけ「NFCの設定を開く」ボタンを足す(画面イメージにはない追加)。
 */
@Composable
fun HelpDialog(
    nfcStatus: NfcStatus,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
        ) {
            // 画面が小さい・文字が大きい設定でも、ボタンが切れないようにスクロールできる
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "読み取れないときは",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                ReaderIllustration(withCard = false, size = 160.dp)
                Spacer(Modifier.height(20.dp))

                StatusBadge(nfcStatus)
                Spacer(Modifier.height(20.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1.NFCが有効になっていることを確認", style = MaterialTheme.typography.bodyLarge)
                    Text("2.端末本体のNFC読み取り部を確認", style = MaterialTheme.typography.bodyLarge)
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "なお、おサイフケータイがない機種は\n交通系ICなどの情報は読み取れません。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))

                if (nfcStatus == NfcStatus.DISABLED) {
                    FilledTonalButton(
                        onClick = onOpenSettings,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("NFCの設定を開く", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                }

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("閉じる", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/** NFCの状態を示すバッジ。色だけに頼らず、アイコンと文言でも状態が分かる。 */
@Composable
private fun StatusBadge(status: NfcStatus) {
    val colors = MaterialTheme.colorScheme
    val (container, content, icon, label) = when (status) {
        NfcStatus.ENABLED -> BadgeStyle(colors.primary, colors.onPrimary, Icons.Filled.CheckCircle, "この端末はNFCが使えます")
        NfcStatus.DISABLED -> BadgeStyle(colors.errorContainer, colors.onErrorContainer, Icons.Filled.Warning, "NFCがオフになっています")
        NfcStatus.UNSUPPORTED -> BadgeStyle(colors.surfaceVariant, colors.onSurfaceVariant, Icons.Filled.Block, "この端末はNFCに対応していません")
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = container,
        contentColor = content,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.size(8.dp))
            Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
    }
}

private data class BadgeStyle(
    val container: Color,
    val content: Color,
    val icon: ImageVector,
    val label: String
)