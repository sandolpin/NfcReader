package com.sandolpin.nfcreader.data.sound

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.util.Collections

/**
 * SoundPool による効果音。短い音の連続再生に向いていて、MediaPlayer より遅延が小さい。
 *
 * 音声ファイルは res/raw に koutuu / credit / tag(拡張子は mp3)として置く。
 * ファイルがまだ無いときは、その音だけ鳴らさずに動き続ける(ビルドも壊れない)。
 * 音声属性を「操作音(SONIFICATION)」にしているので、マナーモードでは鳴らない。
 */
class SoundPoolPlayer(private val context: Context) : SoundPlayer {

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val sampleIds = HashMap<SoundType, Int>()

    // 読み込み完了はバックグラウンドのスレッドから通知されるので、同期したセットで持つ
    private val loaded: MutableSet<Int> = Collections.synchronizedSet(HashSet())

    init {
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) loaded.add(sampleId)
        }
        load("koutuu", SoundType.TRANSIT)
        load("credit", SoundType.CREDIT)
        load("tag", SoundType.TAG)
    }

    @SuppressLint("DiscouragedApi") // ファイル未配置でも落ちないよう、名前で探す
    private fun load(resourceName: String, type: SoundType) {
        val resId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
        if (resId != 0) {
            sampleIds[type] = pool.load(context, resId, 1)
        }
    }

    override fun play(type: SoundType) {
        val sampleId = sampleIds[type] ?: return
        if (sampleId in loaded) {
            pool.play(sampleId, 1f, 1f, 1, 0, 1f)
        }
    }

    override fun release() {
        pool.release()
    }
}