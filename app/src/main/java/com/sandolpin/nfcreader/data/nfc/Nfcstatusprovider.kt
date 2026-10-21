package com.sandolpin.nfcreader.data.nfc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import androidx.core.content.ContextCompat
import com.sandolpin.nfcreader.domain.model.NfcStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/** 端末のNFC状態(非対応 / 無効 / 有効)を取得・監視する。 */
class NfcStatusProvider(context: Context) {

    private val appContext = context.applicationContext

    fun current(): NfcStatus {
        val adapter = NfcAdapter.getDefaultAdapter(appContext) ?: return NfcStatus.UNSUPPORTED
        return if (adapter.isEnabled) NfcStatus.ENABLED else NfcStatus.DISABLED
    }

    /** NFCのオン/オフが切り替わるたびに最新の状態を流す。設定アプリから戻る前でも反映される。 */
    fun observe(): Flow<NfcStatus> = callbackFlow {
        trySend(current())
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(current())
            }
        }
        ContextCompat.registerReceiver(
            appContext,
            receiver,
            IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        awaitClose { appContext.unregisterReceiver(receiver) }
    }.distinctUntilChanged()
}