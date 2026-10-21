package com.sandolpin.nfcreader.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * スマホ(とカード)のイラスト。画像ファイルは持たず、Canvas で描く。
 *
 * [withCard] が true のときは、スマホにカードをかざした絵(待機画面)、false のときはスマホだけ(ヘルプ)。
 * 大きさは [size] で決まり、中の寸法はすべてその比率で計算するので、どんなサイズでも崩れない。
 */
@Composable
fun ReaderIllustration(
    withCard: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 232.dp
) {
    val circleColor = MaterialTheme.colorScheme.surfaceVariant
    val description = if (withCard) {
        "スマートフォンの読み取り部にカードをかざしているイラスト"
    } else {
        "スマートフォンのイラスト"
    }

    Canvas(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = description }
    ) {
        val d = this.size.minDimension

        // 背景の円
        drawCircle(color = circleColor, radius = d / 2f, center = center)

        // スマホ本体
        val phoneW = d * 0.38f
        val phoneH = d * 0.74f
        val phoneLeft = if (withCard) d * 0.21f else (d - phoneW) / 2f
        val phoneTop = d * 0.13f
        drawRoundRect(
            color = PhoneBody,
            topLeft = Offset(phoneLeft, phoneTop),
            size = Size(phoneW, phoneH),
            cornerRadius = CornerRadius(d * 0.03f)
        )

        // カメラ部(灰色の縦長の台に、黒いレンズが2つと小さな白いライト)
        val camW = phoneW * 0.22f
        val camH = phoneH * 0.27f
        val camLeft = phoneLeft + phoneW * 0.08f
        val camTop = phoneTop + phoneH * 0.06f
        drawRoundRect(
            color = CameraBase,
            topLeft = Offset(camLeft, camTop),
            size = Size(camW, camH),
            cornerRadius = CornerRadius(camW / 2f)
        )
        val lensR = camW * 0.36f
        drawCircle(Color.Black, lensR, Offset(camLeft + camW / 2f, camTop + camH * 0.26f))
        drawCircle(Color.Black, lensR, Offset(camLeft + camW / 2f, camTop + camH * 0.58f))
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(camLeft + camW * 0.25f, camTop + camH * 0.82f),
            size = Size(camW * 0.5f, camH * 0.08f),
            cornerRadius = CornerRadius(camH * 0.04f)
        )

        // かざしているカード(右下に少し傾けて重ねる)
        if (withCard) {
            val cardW = d * 0.52f
            val cardH = d * 0.34f
            val cardLeft = d * 0.33f
            val cardTop = d * 0.45f
            rotate(degrees = 10f, pivot = Offset(cardLeft + cardW / 2f, cardTop + cardH / 2f)) {
                val radius = CornerRadius(d * 0.035f)
                drawRoundRect(CardBlue, Offset(cardLeft, cardTop), Size(cardW, cardH), radius)
                // 左寄りの縦じま
                drawRect(CardStripeLight, Offset(cardLeft + cardW * 0.18f, cardTop), Size(cardW * 0.07f, cardH))
                drawRect(CardStripeMid, Offset(cardLeft + cardW * 0.28f, cardTop), Size(cardW * 0.05f, cardH))
            }
        }
    }
}

private val PhoneBody = Color(0xFFFAFAFA)
private val CameraBase = Color(0xFFBDBDBD)
private val CardBlue = Color(0xFF0D3B9E)
private val CardStripeLight = Color(0xFFB4C3E6)
private val CardStripeMid = Color(0xFF7F9BD4)