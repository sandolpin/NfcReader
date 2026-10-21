package com.sandolpin.nfcreader.data.nfc

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import com.sandolpin.nfcreader.domain.model.NfcStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest

/**
 * リーダーモード(enableReaderMode)でタグを受け取る。
 *
 * 返す Flow を収集している間だけリーダーモードが有効になり、収集をやめると自動で解除される。
 * Activity の前面表示中(`repeatOnLifecycle(RESUMED)`)に収集すること。NFCがオフの間は待機し、
 * オンになった時点で自動的に有効化する。
 */
class NfcReader(private val statusProvider: NfcStatusProvider) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun tags(activity: Activity): Flow<Tag> =
        statusProvider.observe().flatMapLatest { status ->
            if (status == NfcStatus.ENABLED) readerMode(activity) else emptyFlow()
        }

    private fun readerMode(activity: Activity): Flow<Tag> = callbackFlow {
        val adapter = NfcAdapter.getDefaultAdapter(activity)
        if (adapter == null) {
            close()
            return@callbackFlow
        }
        adapter.enableReaderMode(
            activity,
            { tag -> trySend(tag) },
            READER_FLAGS,
            null
        )
        awaitClose {
            // Activity の終了済みなどで失敗しても問題ないので握りつぶす
            runCatching { adapter.disableReaderMode(activity) }
        }
    }

    private companion object {
        // 効果音は自前で鳴らすので、システム音は止める。NDEFの確認は省いて読み取りを速くする
        const val READER_FLAGS =
            NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_F or
                    NfcAdapter.FLAG_READER_NFC_V or
                    NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS or
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
    }
}