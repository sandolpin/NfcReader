package com.sandolpin.nfcreader.domain.emv

import com.sandolpin.nfcreader.domain.model.CardBrand

/**
 * 非接触決済カード(EMV)の「入口の名簿」(PPSE)への返事を読み解く。Android 非依存の純粋なKotlinで、単体テストできる。
 *
 * 返事は BER-TLV(タグ・長さ・値の入れ子)という形式で、その中に、カードが持つ決済アプリの識別子(AID, タグ 4F)が入っている。
 * AID の先頭5バイト(RID)がブランドごとに決まっているので、そこからブランドが分かる。
 * 読むのは名簿だけで、カード番号・名義・有効期限などには触れない。
 */
object EmvParser {

    private const val TAG_AID = 0x4F

    /** PPSE(2PAY.SYS.DDF01)を選ぶ SELECT コマンド。 */
    val SELECT_PPSE: ByteArray = byteArrayOf(
        0x00, 0xA4.toByte(), 0x04, 0x00, 0x0E,
        0x32, 0x50, 0x41, 0x59, 0x2E, 0x53, 0x59, 0x53, 0x2E, 0x44, 0x44, 0x46, 0x30, 0x31,
        0x00
    )

    /** 応答の末尾2バイト(SW1 SW2)が 90 00(成功)か。 */
    fun isSuccess(response: ByteArray): Boolean =
        response.size >= 2 &&
                response[response.size - 2] == 0x90.toByte() &&
                response[response.size - 1] == 0x00.toByte()

    /**
     * PPSE への応答から、決済カードのブランドを返す。決済カードでなければ null。
     * AID が複数あるとき(例: 国際ブランド+国内ブランド)は、特定できた最初のブランドを選ぶ。
     */
    fun brandFromPpseResponse(response: ByteArray): CardBrand? {
        if (!isSuccess(response)) return null
        val aids = extractAids(response.copyOf(response.size - 2))
        if (aids.isEmpty()) return null
        return aids.map(::brandFromAid).firstOrNull { it != CardBrand.UNKNOWN } ?: CardBrand.UNKNOWN
    }

    /** AID の先頭(RID)からブランドを決める。 */
    fun brandFromAid(aid: ByteArray): CardBrand {
        val hex = aid.joinToString("") { "%02X".format(it.toInt() and 0xFF) }
        return when {
            hex.startsWith("A000000003") -> CardBrand.VISA
            hex.startsWith("A000000004") || hex.startsWith("A000000005") -> CardBrand.MASTERCARD
            hex.startsWith("A000000025") -> CardBrand.AMEX
            hex.startsWith("A000000065") -> CardBrand.JCB
            hex.startsWith("A000000152") -> CardBrand.DISCOVER
            hex.startsWith("A000000277") -> CardBrand.DINERS
            hex.startsWith("A000000333") -> CardBrand.UNIONPAY
            else -> CardBrand.UNKNOWN
        }
    }

    /** TLV を再帰的にたどって、タグ 4F(AID)の値をすべて集める。壊れたデータでは、読めたところまでを返す。 */
    fun extractAids(data: ByteArray): List<ByteArray> {
        val result = mutableListOf<ByteArray>()
        walk(data, 0, data.size, result, depth = 0)
        return result
    }

    private fun walk(data: ByteArray, start: Int, end: Int, out: MutableList<ByteArray>, depth: Int) {
        if (depth > 8) return // 異常に深い入れ子は打ち切る
        var i = start
        while (i < end) {
            // タグ(1バイト。下位5bitが全部1なら続きのバイトがある)
            val firstTagByte = data[i].toInt() and 0xFF
            var tag = firstTagByte
            i++
            if (firstTagByte and 0x1F == 0x1F) {
                while (i < end) {
                    val b = data[i].toInt() and 0xFF
                    tag = (tag shl 8) or b
                    i++
                    if (b and 0x80 == 0) break // 続きがなければ終わり
                }
            }
            if (tag == 0x00 || tag == 0xFF) continue // パディング

            // 長さ(0x80未満はそのまま。0x81/0x82 は続くバイト数が長さ)
            if (i >= end) return
            var length = data[i].toInt() and 0xFF
            i++
            if (length and 0x80 != 0) {
                val n = length and 0x7F
                if (n == 0 || n > 2 || i + n > end) return
                length = 0
                repeat(n) { length = (length shl 8) or (data[i++].toInt() and 0xFF) }
            }
            if (length < 0 || i + length > end) return

            if (tag == TAG_AID) {
                out += data.copyOfRange(i, i + length)
            } else if (firstTagByte and 0x20 != 0) {
                // 入れ子になっているタグ(構造化)は中身もたどる
                walk(data, i, i + length, out, depth + 1)
            }
            i += length
        }
    }
}