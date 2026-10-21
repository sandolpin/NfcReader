package com.sandolpin.nfcreader.data.repository

import android.nfc.Tag
import com.sandolpin.nfcreader.data.nfc.FelicaCommand
import com.sandolpin.nfcreader.data.nfc.IsoDepCommand
import com.sandolpin.nfcreader.domain.felica.FelicaHistoryParser
import com.sandolpin.nfcreader.domain.felica.StationResolver
import com.sandolpin.nfcreader.domain.felica.withStationNames
import com.sandolpin.nfcreader.domain.model.CardReadResult
import com.sandolpin.nfcreader.domain.model.TagReadResult
import com.sandolpin.nfcreader.domain.model.TagType
import com.sandolpin.nfcreader.util.CardIdFormatter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** タグを読み取って、アプリで扱う結果([TagReadResult])に変換する。 */
class NfcRepository(
    private val felica: FelicaCommand,
    private val isoDep: IsoDepCommand,
    private val stations: StationResolver
) {

    /** 通信を行うので、呼び出し側のスレッドに関わらず IO スレッドで実行する。 */
    suspend fun read(tag: Tag): TagReadResult = withContext(Dispatchers.IO) {
        try {
            val type = TagType.fromTechList(tag.techList.toList())
            val uid = CardIdFormatter.format(tag.id)

            if (type == TagType.FELICA) {
                val history = FelicaHistoryParser
                    .parse(felica.readTransitHistoryBlocks(tag))
                    .map { it.withStationNames(stations) }

                // 残高は最新の履歴ブロックに入っている。履歴が1件も読めなければ交通系ICとは扱わない
                if (history.isNotEmpty()) {
                    return@withContext TagReadResult.Transit(
                        CardReadResult(cardId = uid, balance = history.first().balanceAfter, history = history)
                    )
                }
            }
            // ISO-DEP は、決済カード(クレジットカードなど)かどうかをブランド付きで調べる
            val brand = if (type == TagType.ISO_DEP) isoDep.readPaymentBrand(tag) else null
            TagReadResult.Other(type, uid, brand)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            TagReadResult.Failure(e.message)
        }
    }
}