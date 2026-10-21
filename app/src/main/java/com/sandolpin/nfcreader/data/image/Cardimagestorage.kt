package com.sandolpin.nfcreader.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

/**
 * カード画像として選ばれた画像を、アプリ内部ストレージにコピーして保管する。
 *
 * 選ばれた Uri は、あとで読めなくなることがある。そのため、カードの縦横比(約1.586:1)の中央を切り抜いて、
 * 幅 1024px 以下に縮小した JPEG を filesDir/card_images に保存し、そのパスを返す。
 * カードを削除したり外観を変えたりしたときは、[delete] で後始末する。
 */
class CardImageStorage(context: Context) {

    private val resolver = context.applicationContext.contentResolver
    private val directory = File(context.applicationContext.filesDir, "card_images")

    /** [uri] の画像を取り込み、保存したファイルの絶対パスを返す。失敗時は IOException。 */
    suspend fun import(uri: Uri): String = withContext(Dispatchers.IO) {
        directory.mkdirs()
        val bitmap = decode(uri) ?: throw IOException("画像を読み込めませんでした")
        val file = File(directory, "${UUID.randomUUID()}.jpg")
        try {
            FileOutputStream(file).use { out ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)) {
                    throw IOException("画像を保存できませんでした")
                }
            }
        } catch (e: Throwable) {
            file.delete() // 中途半端なファイルを残さない
            throw e
        } finally {
            bitmap.recycle()
        }
        file.absolutePath
    }

    /** 取り込んだ画像を削除する。このストレージ外のパスは、誤って他のファイルを消さないよう無視する。 */
    fun delete(path: String) {
        val file = File(path)
        if (file.parentFile?.canonicalFile == directory.canonicalFile) {
            file.delete()
        }
    }

    // ---- 画像の読み込みと整形 ----

    private fun decode(uri: Uri): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight)
        }
        val decoded = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: return null

        val rotated = rotateByExif(decoded, readOrientation(uri))
        val cropped = centerCrop(rotated)
        return scaleDown(cropped)
    }

    /** 縦横とも MAX_WIDTH 以上を保てる範囲で、一番大きく間引く(メモリ節約)。 */
    private fun sampleSize(width: Int, height: Int): Int {
        var sample = 1
        while (width / (sample * 2) >= MAX_WIDTH && height / (sample * 2) >= MAX_WIDTH) sample *= 2
        return sample
    }

    private fun readOrientation(uri: Uri): Int =
        runCatching {
            resolver.openInputStream(uri)?.use {
                ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            }
        }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL

    private fun rotateByExif(src: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.postRotate(90f); matrix.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.postRotate(-90f); matrix.postScale(-1f, 1f) }
            else -> return src
        }
        val result = Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
        if (result !== src) src.recycle()
        return result
    }

    private fun centerCrop(src: Bitmap): Bitmap {
        val aspect = src.width.toFloat() / src.height
        val width: Int
        val height: Int
        if (aspect > CARD_ASPECT) {
            height = src.height
            width = (height * CARD_ASPECT).toInt()
        } else {
            width = src.width
            height = (width / CARD_ASPECT).toInt()
        }
        val x = (src.width - width) / 2
        val y = (src.height - height) / 2
        val result = Bitmap.createBitmap(src, x, y, width, height)
        if (result !== src) src.recycle()
        return result
    }

    private fun scaleDown(src: Bitmap): Bitmap {
        if (src.width <= MAX_WIDTH) return src
        val height = (MAX_WIDTH / CARD_ASPECT).toInt()
        val result = Bitmap.createScaledBitmap(src, MAX_WIDTH, height, true)
        if (result !== src) src.recycle()
        return result
    }

    private companion object {
        const val CARD_ASPECT = 1.586f // クレジットカードの縦横比(85.6mm x 54mm)
        const val MAX_WIDTH = 1024
        const val JPEG_QUALITY = 90
    }
}