package com.sandolpin.nfcreader.ui.result

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sandolpin.nfcreader.domain.model.TagReadResult
import com.sandolpin.nfcreader.ui.components.CardFrame

/**
 * 交通系IC以外(クレジットカード・その他のNFCタグ)の読み取り結果画面。
 * 履歴は読み取れないので、種類とカードIDだけを出し、「保存できません」と伝える(ヘッダーに保存ボタンは無い)。
 */
@Composable
fun OtherResultScreen(
    result: TagReadResult.Other,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Header(onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (result.isPaymentCard) {
                PaymentCardIllustration()
                PaymentCardLabel(brandName = result.paymentBrand?.displayName.orEmpty())
            } else {
                TagIllustration()
                Text(
                    text = "その他のNFCタグ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }

            OutlinedTextField(
                value = result.uid,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                label = { Text("カードID") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "この履歴は保存できません。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun Header(onBack: () -> Unit) {
    Row(
        modifier = Modifier
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
            fontWeight = FontWeight.Bold
        )
    }
}

/** 角丸の太い枠に、「クレジットカード」とブランド名を並べた表示。 */
@Composable
private fun PaymentCardLabel(brandName: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(3.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("クレジットカード", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = brandName,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

private val IllustrationGray = Color(0xFFAAAAAA)
private val IllustrationDarkGray = Color(0xFF8E8E8E)

/** 灰色のカードにICチップを描いた絵(画像ファイルは使わず Canvas で描く)。 */
@Composable
private fun PaymentCardIllustration() {
    CardFrame(
        modifier = Modifier
            .widthIn(max = 260.dp)
            .fillMaxWidth(0.6f)
            .semantics { contentDescription = "クレジットカードのイラスト" },
        cornerRadius = 10.dp
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(IllustrationGray)
            // ICチップ(白い角丸の四角に、区切りの線)
            val chipW = size.width * 0.17f
            val chipH = size.height * 0.30f
            val left = size.width * 0.14f
            val top = (size.height - chipH) / 2f
            drawRoundRect(Color.White, Offset(left, top), Size(chipW, chipH), CornerRadius(chipW * 0.12f))
            val line = size.height * 0.018f
            listOf(0.33f, 0.66f).forEach { fy ->
                drawRect(IllustrationGray, Offset(left, top + chipH * fy - line / 2), Size(chipW, line))
            }
            drawRect(IllustrationGray, Offset(left + chipW * 0.5f - line / 2, top), Size(line, chipH))
        }
    }
}

/** 灰色のカード・スマホ・小さなタグが散らばった絵。 */
@Composable
private fun TagIllustration() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth(0.62f)
            .widthIn(max = 280.dp)
            .aspectRatio(1.55f)
            .semantics { contentDescription = "NFCタグのイラスト" }
    ) {
        val w = size.width
        val h = size.height

        // 大きなカード(左に寝かせて、少し傾ける)
        tiltedRoundRect(
            color = Color(0xFFB3B3B3), center = Offset(w * 0.36f, h * 0.62f),
            rectSize = Size(w * 0.66f, h * 0.52f), degrees = 8f, radius = w * 0.03f
        )
        // 小さなタグ(上に、反対向きに傾ける)
        tiltedRoundRect(
            color = IllustrationDarkGray, center = Offset(w * 0.46f, h * 0.13f),
            rectSize = Size(w * 0.17f, h * 0.20f), degrees = -14f, radius = w * 0.025f
        )
        // 縦長の角丸(右。スマホを表す)
        tiltedRoundRect(
            color = Color(0xFF9B9B9B), center = Offset(w * 0.86f, h * 0.60f),
            rectSize = Size(w * 0.24f, h * 0.60f), degrees = 0f, radius = w * 0.06f
        )
    }
}

private fun DrawScope.tiltedRoundRect(
    color: Color,
    center: Offset,
    rectSize: Size,
    degrees: Float,
    radius: Float
) {
    rotate(degrees = degrees, pivot = center) {
        drawRoundRect(
            color = color,
            topLeft = Offset(center.x - rectSize.width / 2f, center.y - rectSize.height / 2f),
            size = rectSize,
            cornerRadius = CornerRadius(radius)
        )
    }
}