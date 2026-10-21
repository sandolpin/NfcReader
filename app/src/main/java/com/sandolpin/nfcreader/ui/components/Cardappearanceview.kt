package com.sandolpin.nfcreader.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sandolpin.nfcreader.domain.model.CardAppearance
import com.sandolpin.nfcreader.domain.model.CardStyle
import java.io.File

/** カードの縦横比(クレジットカードと同じ 85.6mm x 54mm)。 */
const val CARD_ASPECT_RATIO = 1.586f

private val FrameBackground = Color(0xFFEDEDED)

/**
 * カード画像の枠(角丸・背景・任意で枠線)。プリセットの絵も、アップロードされた画像も、この枠に入れて表示する。
 * 大きさは呼び出し側の [modifier] で決める(横幅を指定すれば、縦は縦横比から決まる)。
 * shared element transition のために、modifier を外から渡せるようにしてある。
 */
@Composable
fun CardFrame(
    modifier: Modifier = Modifier,
    bordered: Boolean = false,
    cornerRadius: Dp = 12.dp,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .aspectRatio(CARD_ASPECT_RATIO)
            .clip(shape)
            .background(FrameBackground)
            .then(
                if (bordered) Modifier.border(BorderStroke(2.dp, MaterialTheme.colorScheme.outline), shape)
                else Modifier
            )
    ) {
        content()
    }
}

/**
 * カードの外観。プリセットは Canvas で描き、アップロード画像は Coil で表示する(画像ファイルが無いときは枠だけ)。
 */
@Composable
fun CardAppearanceView(
    appearance: CardAppearance,
    modifier: Modifier = Modifier,
    bordered: Boolean = false
) {
    CardFrame(modifier = modifier, bordered = bordered) {
        when (appearance) {
            is CardAppearance.Preset -> Canvas(Modifier.fillMaxSize()) { drawCardStyle(appearance.style) }
            is CardAppearance.Custom -> AsyncImage(
                model = File(appearance.imagePath),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// ---- プリセット4種の描画 ----

private val StyleGreen = Color(0xFF2BDB1A)
private val StyleBlue = Color(0xFF12AAFF)
private val StyleCyan = Color(0xFF14F4FF)
private val StylePink = Color(0xFFFF8FB0)
private val StyleWhite = Color(0xFFFFFFFF)
private val StyleGrayCircle = Color(0xFFE3E3E3)

private fun DrawScope.drawCardStyle(style: CardStyle) {
    val w = size.width
    val h = size.height
    when (style) {
        CardStyle.GREEN -> {
            // 左上から右下へ斜めに切れた形
            val m = h * 0.07f
            fillRoundedPolygon(
                listOf(Offset(m, m), Offset(w * 0.42f, m), Offset(w * 0.88f, h - m), Offset(m, h - m)),
                StyleGreen, radius = h * 0.05f
            )
        }
        CardStyle.BLUE -> {
            // 開いた本のように、2つの丘が並んだ形
            val path = Path().apply {
                moveTo(0f, h * 0.62f)
                cubicTo(w * 0.12f, h * 0.28f, w * 0.40f, h * 0.28f, w * 0.50f, h * 0.55f)
                cubicTo(w * 0.60f, h * 0.28f, w * 0.88f, h * 0.28f, w, h * 0.62f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(path, StyleBlue, style = Fill)
        }
        CardStyle.CYAN -> {
            // 左上と右下の角を落とした形
            val m = h * 0.1f
            fillRoundedPolygon(
                listOf(
                    Offset(w * 0.22f, m), Offset(w - m, m), Offset(w - m, h * 0.62f),
                    Offset(w * 0.76f, h - m), Offset(m, h - m), Offset(m, h * 0.4f)
                ),
                StyleCyan, radius = h * 0.04f
            )
        }
        CardStyle.WHITE_PINK -> {
            // 白いカードに、右上の灰色の大きな円と、右下のピンクの3つの点
            drawRoundRect(StyleWhite, Offset(w * 0.03f, h * 0.06f), Size(w * 0.94f, h * 0.88f), CornerRadius(h * 0.06f))
            drawCircle(StyleGrayCircle, radius = h * 0.42f, center = Offset(w * 0.8f, h * 0.38f))
            val r = h * 0.07f
            listOf(0.78f, 0.86f, 0.94f).forEach { x ->
                drawCircle(StylePink, r, Offset(w * x, h * 0.82f))
            }
        }
    }
}

/**
 * 角が丸い多角形を塗る。多角形を少し内側に縮めてから、角を丸く結合した太い線で縁取ることで角を丸める。
 */
private fun DrawScope.fillRoundedPolygon(points: List<Offset>, color: Color, radius: Float) {
    val centroid = Offset(
        points.map { it.x }.average().toFloat(),
        points.map { it.y }.average().toFloat()
    )
    val shrunk = points.map { p ->
        val toCenter = centroid - p
        val length = toCenter.getDistance()
        if (length == 0f) p else p + toCenter * (radius / length)
    }
    val path = Path().apply {
        moveTo(shrunk.first().x, shrunk.first().y)
        shrunk.drop(1).forEach { lineTo(it.x, it.y) }
        close()
    }
    drawPath(path, color, style = Fill)
    drawPath(path, color, style = Stroke(width = radius * 2f, join = StrokeJoin.Round))
}