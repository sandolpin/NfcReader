package com.sandolpin.nfcreader.domain.felica

import com.sandolpin.nfcreader.domain.model.HistoryEntry
import com.sandolpin.nfcreader.domain.model.HistoryType
import com.sandolpin.nfcreader.domain.model.Station
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class HistoryMergerTest {

    private fun entry(
        sequence: Int,
        balance: Int,
        date: LocalDate = LocalDate.of(2025, 12, 1).plusDays(sequence.toLong()),
        type: HistoryType = HistoryType.RAIL,
        boarding: Station? = null
    ) = HistoryEntry(type, date, balance, null, sequence, boarding, null)

    @Test
    fun `保存済みが空なら読み取り結果の金額だけ計算する`() {
        val result = HistoryMerger.merge(
            emptyList(),
            listOf(entry(3, 1200), entry(2, 1360), entry(1, 360))
        )
        assertEquals(listOf(-160, 1000, null), result.map { it.amount })
    }

    @Test
    fun `重複する履歴は1件にまとめ、新しい順に並ぶ`() {
        val saved = listOf(entry(3, 1200), entry(2, 1360), entry(1, 360))
        val incoming = listOf(entry(4, 1000), entry(3, 1200), entry(2, 1360))

        val result = HistoryMerger.merge(saved, incoming)

        assertEquals(listOf(4, 3, 2, 1), result.map { it.sequence })
        assertEquals(listOf(-200, -160, 1000, null), result.map { it.amount })
    }

    @Test
    fun `同じ内容を再度保存しても増えない`() {
        val saved = listOf(entry(3, 1200), entry(2, 1360), entry(1, 360))
        val result = HistoryMerger.merge(saved, saved)
        assertEquals(3, result.size)
    }

    @Test
    fun `保存済みより古い履歴との境目も金額を計算する`() {
        val saved = listOf(entry(2, 1360), entry(1, 360))
        val incoming = listOf(entry(3, 1200), entry(2, 1360))

        val result = HistoryMerger.merge(saved, incoming)

        assertEquals(listOf(-160, 1000, null), result.map { it.amount })
    }

    @Test
    fun `連番が飛んでいる区間の金額は計算しない`() {
        // 保存済みの連番1のあと、連番2〜3の履歴が読み取れていない
        val saved = listOf(entry(1, 360))
        val incoming = listOf(entry(5, 900), entry(4, 1000))

        val result = HistoryMerger.merge(saved, incoming)

        assertEquals(listOf(5, 4, 1), result.map { it.sequence })
        assertEquals(-100, result[0].amount) // 連番4→5は連続
        assertNull(result[1].amount)         // 連番1→4は間が抜けている
        assertNull(result[2].amount)         // 最も古い
    }

    @Test
    fun `連番が24bitで一周して0になった履歴は連番不明として扱い金額を出す`() {
        // 連番0は「取得できなかった」を表すため、0xFFFFFFの次の0は連続とみなされる
        val result = HistoryMerger.recalculateAmounts(
            listOf(entry(0, 800), entry(0xFFFFFF, 1000))
        )
        assertEquals(-200, result[0].amount)
    }

    @Test
    fun `連番が24bitの上限をまたいで1から始まる場合は間が抜けている扱い`() {
        // 0xFFFFFF の次は 0 であり 1 ではないので、連番1との間は抜けている
        val result = HistoryMerger.recalculateAmounts(
            listOf(entry(1, 800), entry(0xFFFFFF, 1000))
        )
        assertNull(result[0].amount)
    }

    @Test
    fun `連番が取れない履歴は日付と残高と駅で重複を判定する`() {
        val station = Station(0, 1, 2)
        val a = entry(0, 1000, date = LocalDate.of(2025, 12, 10), boarding = station)
        val b = entry(0, 800, date = LocalDate.of(2025, 12, 11))

        val result = HistoryMerger.merge(saved = listOf(a), incoming = listOf(b, a))

        assertEquals(2, result.size)
        assertEquals(listOf(800, 1000), result.map { it.balanceAfter })
        assertEquals(-200, result[0].amount)
    }

    @Test
    fun `連番が取れない場合は連続しているものとして金額を出す`() {
        val result = HistoryMerger.recalculateAmounts(
            listOf(
                entry(0, 800, date = LocalDate.of(2025, 12, 11)),
                entry(0, 1000, date = LocalDate.of(2025, 12, 10))
            )
        )
        assertEquals(-200, result[0].amount)
    }

    @Test
    fun `同じキーの履歴が複数あっても保存済みの分を失わない`() {
        // 連番なしで、同日・同残高・同種別の取引が2件ある場合
        val day = LocalDate.of(2025, 12, 10)
        val twin1 = entry(0, 500, date = day)
        val twin2 = entry(0, 500, date = day)

        val result = HistoryMerger.merge(saved = listOf(twin1, twin2), incoming = listOf(twin1, twin2))

        assertEquals(2, result.size)
    }

    @Test
    fun `空同士の統合は空`() {
        assertEquals(emptyList<HistoryEntry>(), HistoryMerger.merge(emptyList(), emptyList()))
    }
}
