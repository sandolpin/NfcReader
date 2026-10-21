package com.sandolpin.nfcreader.data.nfc

import android.nfc.Tag
import android.nfc.tech.IsoDep
import com.sandolpin.nfcreader.domain.emv.EmvParser
import com.sandolpin.nfcreader.domain.model.CardBrand
import java.io.IOException

/**
 * ISO-DEP のカードに、非接触決済の名簿(PPSE)を問い合わせて、ブランドを調べる。blocking なので、バックグラウンドで呼ぶこと。
 * 決済カードでない・通信できなかった場合は null(その他のタグとして扱う)。
 */
class IsoDepCommand {

    fun readPaymentBrand(tag: Tag): CardBrand? {
        val isoDep = IsoDep.get(tag) ?: return null
        return try {
            isoDep.connect()
            isoDep.timeout = TIMEOUT_MS
            EmvParser.brandFromPpseResponse(isoDep.transceive(EmvParser.SELECT_PPSE))
        } catch (e: IOException) {
            null // タグが離れた、など
        } catch (e: SecurityException) {
            null // 別のアプリがタグを使っている、など
        } finally {
            runCatching { isoDep.close() }
        }
    }

    private companion object {
        const val TIMEOUT_MS = 2_000
    }
}