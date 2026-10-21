package com.sandolpin.nfcreader.ui.result

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.sandolpin.nfcreader.domain.model.CardAppearance
import com.sandolpin.nfcreader.domain.model.CardStyle
import com.sandolpin.nfcreader.ui.components.CardAppearanceView
import com.sandolpin.nfcreader.ui.components.CardFrame

/**
 * 「カードを保存」ダイアログ。1.スタイル(または画像)を選び、2.名前を付けて保存する。
 * 名前が空のあいだは保存ボタンを押せない。保存済みのカードを読み直したときは、名前と外観が入った状態で開く。
 */
@Composable
fun SaveCardDialog(
    state: SaveDialogState,
    onNameChange: (String) -> Unit,
    onSelectStyle: (CardStyle) -> Unit,
    onImagePicked: (Uri) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    title: String = "カードを保存",
    confirmLabel: String = "これで保存する"
) {
    // Photo Picker はストレージの権限が要らない。選ばれた Uri は、保存時にアプリ内へコピーする
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onImagePicked(uri)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .widthIn(max = 480.dp)
        ) {
            // キーボードが出ても、画面が小さくても、下のボタンまでスクロールして届くようにする
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DialogHeader(title = title, onBack = onDismiss)

                SectionTitle("1.スタイルを選択")
                StyleGrid(selected = state.selectedStyle, onSelect = onSelectStyle)
                ImagePreview(state.selection)

                OutlinedButton(
                    onClick = {
                        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("画像をアップロード")
                }

                SectionTitle("2.名前を設定")
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    placeholder = { Text("例:通勤用のsuica") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = onSave,
                    enabled = state.canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.size(8.dp))
                    Text(confirmLabel, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DialogHeader(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "閉じる")
        }
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        HorizontalDivider(Modifier.weight(1f))
    }
}

/** 4つのスタイルを 2x2 で並べる。選択中のものは濃い枠で囲む。 */
@Composable
private fun StyleGrid(selected: CardStyle?, onSelect: (CardStyle) -> Unit) {
    val styles = CardStyle.entries
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        styles.chunked(2).forEach { rowStyles ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowStyles.forEach { style ->
                    StyleTile(
                        style = style,
                        selected = style == selected,
                        onClick = { onSelect(style) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StyleTile(
    style: CardStyle,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val borderWidth = if (selected) 3.dp else 1.dp
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(BorderStroke(borderWidth, borderColor), RoundedCornerShape(12.dp))
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
    ) {
        CardAppearanceView(CardAppearance.Preset(style))
    }
}

/** 選ばれた画像、または保存済みカードの画像を、プレビューとして出す(プリセットのときは何も出さない)。 */
@Composable
private fun ImagePreview(selection: AppearanceSelection) {
    when (selection) {
        is AppearanceSelection.NewImage -> PreviewFrame {
            AsyncImage(
                model = selection.uri,
                contentDescription = "選んだ画像のプレビュー",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        is AppearanceSelection.KeepExisting ->
            if (selection.appearance is CardAppearance.Custom) {
                PreviewFrame { CardAppearanceView(selection.appearance) }
            }
        is AppearanceSelection.Preset -> Unit
    }
}

@Composable
private fun PreviewFrame(content: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        CardFrame(
            modifier = Modifier.fillMaxWidth(0.6f),
            bordered = true
        ) { content() }
    }
}