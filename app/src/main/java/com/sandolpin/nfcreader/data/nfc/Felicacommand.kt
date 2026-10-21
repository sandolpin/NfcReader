package com.sandolpin.nfcreader.data.nfc

import android.nfc.Tag
import android.nfc.tech.NfcF
import com.sandolpin.nfcreader.domain.felica.FelicaProtocol
import java.io.IOException

/**
 * FeliCa(NfcF)で交通系ICの利用履歴ブロックを読み出す。blocking なので、必ずバックグラウンドで呼ぶこと。
 * コマンドの組み立てと応答の解析は Android 非依存の [FelicaProtocol] に分けてある。
 */
class FelicaCommand {

    /**
     * 利用履歴を新しい順に最大20ブロック読む。
     * 交通系ICでない、通信が切れた、などで読めなかった分は含まれない(途中までなら途中までを返す)。
     */
    fun readTransitHistoryBlocks(tag: Tag): List<ByteArray> {
        val nfcF = NfcF.get(tag) ?: return emptyList()
        val idm = tag.id
        val blocks = mutableListOf<ByteArray>()

        try {
            nfcF.connect()
            for (blockNumber in 0 until FelicaProtocol.MAX_HISTORY_BLOCKS) {
                val command = FelicaProtocol.buildReadCommand(
                    idm, FelicaProtocol.TRANSIT_HISTORY_SERVICE, blockNumber
                )
                // 履歴の末尾や交通系ICでないカードでは、ステータス異常が返ってくるので打ち切る
                val block = FelicaProtocol.parseReadResponse(nfcF.transceive(command)) ?: break
                blocks += block
            }
        } catch (e: IOException) {
            // タグが離れた(TagLostException)など。読めた分だけ返す
        } catch (e: IllegalArgumentException) {
            // IDm が8バイトでないなど、FeliCa として想定外のタグ
        } finally {
            runCatching { nfcF.close() }
        }
        return blocks
    }
}