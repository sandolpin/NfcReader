package com.sandolpin.nfcreader.domain.felica

import com.sandolpin.nfcreader.domain.model.HistoryType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class FelicaHistoryParserTest {

    /** テスト用に16バイトのブロックを組み立てる。 */
    private fun block(
        terminal: Int,
        process: Int,
        year: Int = 2025, month: Int = 12, day: Int = 18,
        inLine: Int = 0, inStation: Int = 0,
        outLine: Int = 0, outStation: Int = 0,
        balance: Int,
        sequence: Int,
        region: Int = 0
    ): ByteArray {
        val date = ((year - 2000) shl 9) or (month shl 5) or day
        return byteArrayOf(
            terminal.toByte(), process.toByte(), 0, 0,
            (date shr 8).toByte(), date.toByte(),
            inLine.toByte(), inStation.toByte(), outLine.toByte(), outStation.toByte(),
            balance.toByte(), (balance shr 8).toByte(),
            (sequence shr 16).toByte(), (sequence shr 8).toByte(), sequence.toByte(),
            region.toByte()
        )
    }

    @Test
    fun `鉄道の履歴を解析できる`() {
        val entry = FelicaHistoryParser.parseBlock(
            block(0x16, 0x01, inLine = 0x10, inStation = 0x20, outLine = 0x11, outStation = 0x21,
                balance = 360, sequence = 1, region = 2)
        )
        assertNotNull(entry)
        entry!!
        assertEquals(HistoryType.RAIL, entry.type)
        assertEquals(LocalDate.of(2025, 12, 18), entry.date)
        assertEquals(360, entry.balanceAfter)
        assertEquals(1, entry.sequence)
        assertEquals(0x10, entry.boarding!!.lineCode)
        assertEquals(0x20, entry.boarding!!.stationCode)
        assertEquals(0x11, entry.alighting!!.lineCode)
        assertEquals(0x21, entry.alighting!!.stationCode)
        assertEquals(2, entry.boarding!!.regionCode)
        assertNull(entry.amount) // 1ブロックだけでは金額は決まらない
    }

    @Test
    fun `残高は2バイトのリトルエンディアン`() {
        val entry = FelicaHistoryParser.parseBlock(block(0x16, 0x01, balance = 0x1234, sequence = 1))!!
        assertEquals(0x1234, entry.balanceAfter)
    }

    @Test
    fun `連番は3バイトのビッグエンディアン`() {
        val entry = FelicaHistoryParser.parseBlock(block(0x16, 0x01, balance = 100, sequence = 0x010203))!!
        assertEquals(0x010203, entry.sequence)
    }

    @Test
    fun `チャージを判定できる`() {
        val entry = FelicaHistoryParser.parseBlock(block(0x16, 0x02, balance = 1360, sequence = 2))!!
        assertEquals(HistoryType.CHARGE, entry.type)
        assertNull(entry.boarding) // 鉄道以外は駅を持たない
    }

    @Test
    fun `オートチャージもチャージとして扱う`() {
        val entry = FelicaHistoryParser.parseBlock(block(0x16, 0x14, balance = 3000, sequence = 2))!!
        assertEquals(HistoryType.CHARGE, entry.type)
    }

    @Test
    fun `物販を判定できる`() {
        val entry = FelicaHistoryParser.parseBlock(block(0xC7, 0x46, balance = 1200, sequence = 3))!!
        assertEquals(HistoryType.PURCHASE, entry.type)
    }

    @Test
    fun `バスを判定できる`() {
        val entry = FelicaHistoryParser.parseBlock(block(0x05, 0x0F, balance = 500, sequence = 4))!!
        assertEquals(HistoryType.BUS, entry.type)
    }

    @Test
    fun `未知の端末と処理はOTHER`() {
        val entry = FelicaHistoryParser.parseBlock(block(0x7F, 0x7F, balance = 500, sequence = 4))!!
        assertEquals(HistoryType.OTHER, entry.type)
    }

    @Test
    fun `入出場の駅コードが0なら駅なし`() {
        val entry = FelicaHistoryParser.parseBlock(block(0x16, 0x01, balance = 100, sequence = 1))!!
        assertNull(entry.boarding)
        assertNull(entry.alighting)
    }

    @Test
    fun `全部0の未使用ブロックは無視する`() {
        assertNull(FelicaHistoryParser.parseBlock(ByteArray(16)))
    }

    @Test
    fun `不正な日付のブロックは無視する`() {
        assertNull(FelicaHistoryParser.parseBlock(block(0x16, 0x01, month = 13, balance = 100, sequence = 1)))
        assertNull(FelicaHistoryParser.parseBlock(block(0x16, 0x01, month = 2, day = 31, balance = 100, sequence = 1)))
    }

    @Test
    fun `長さが16バイトでないブロックは無視する`() {
        assertNull(FelicaHistoryParser.parseBlock(ByteArray(15)))
        assertNull(FelicaHistoryParser.parseBlock(ByteArray(17)))
    }

    @Test
    fun `複数ブロックから金額を計算する(画面イメージと同じ並び)`() {
        // 新しい順: 物販(残高1200) / チャージ(残高1360) / 鉄道(残高360)
        val result = FelicaHistoryParser.parse(
            listOf(
                block(0xC7, 0x46, balance = 1200, sequence = 3),
                block(0x16, 0x02, balance = 1360, sequence = 2),
                block(0x16, 0x01, inLine = 1, inStation = 2, outLine = 3, outStation = 4, balance = 360, sequence = 1),
                ByteArray(16) // 未使用ブロックは取り除かれる
            )
        )
        assertEquals(3, result.size)
        assertEquals(-160, result[0].amount)  // 物販 ¥160(支出)
        assertEquals(1000, result[1].amount)  // チャージ +¥1000
        assertNull(result[2].amount)          // 最も古い履歴は「-」
    }
}
