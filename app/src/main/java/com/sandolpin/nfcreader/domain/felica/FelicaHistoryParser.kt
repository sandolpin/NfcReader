package com.sandolpin.nfcreader.domain.felica

import com.sandolpin.nfcreader.domain.model.HistoryEntry
import com.sandolpin.nfcreader.domain.model.HistoryType
import com.sandolpin.nfcreader.domain.model.Station
import java.time.DateTimeException
import java.time.LocalDate

/**
 * 交通系ICの利用履歴ブロック(サービスコード 0x090F、1ブロック16バイト)の解析。
 *
 * ブロックの構成:
 * ```
 * [0]     端末種
 * [1]     処理種別
 * [2..3]  (未使用)
 * [4..5]  日付(ビッグエンディアン)  年7bit(2000年起点) / 月4bit / 日5bit
 * [6]     入場 線区コード    [7]  入場 駅順コード
 * [8]     出場 線区コード    [9]  出場 駅順コード
 * [10..11] 残高(リトルエンディアン)
 * [12..14] 連番(ビッグエンディアン)
 * [15]    リージョン
 * ```
 * 端末種・処理種別の対応は、公開されている解析情報にもとづく。実機のカードで確認してから確定すること。
 * Android に依存しないので、単体テストでバイト列から検証できる。
 */
object FelicaHistoryParser {

    const val BLOCK_SIZE = 16

    // 処理種別
    private const val PROCESS_RAIL_FARE = 0x01
    private const val PROCESS_CHARGE = 0x02
    private const val PROCESS_RAIL_ADJUST = 0x03
    private const val PROCESS_RAIL_EXIT_ONLY = 0x04
    private const val PROCESS_BUS_PRICE = 0x0D
    private const val PROCESS_BUS = 0x0F
    private const val PROCESS_AUTO_CHARGE = 0x14
    private const val PROCESS_PURCHASE = 0x46

    // 端末種
    private const val TERMINAL_BUS = 0x05
    private const val TERMINAL_PURCHASE = 0xC7
    private const val TERMINAL_VENDING = 0xC8
    private val RAIL_TERMINALS = setOf(0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1F)

    /**
     * 複数ブロックを解析する。入力はカードから読んだ順(新しい順)。
     * 空ブロック・不正なブロックは除き、利用金額を計算して返す。
     */
    fun parse(blocks: List<ByteArray>): List<HistoryEntry> =
        HistoryMerger.recalculateAmounts(blocks.mapNotNull(::parseBlock))

    /** 1ブロックを解析する。空・不正なブロックは null。金額はここでは決まらない(null)。 */
    fun parseBlock(block: ByteArray): HistoryEntry? {
        if (block.size != BLOCK_SIZE) return null

        val terminal = block.u(0)
        val process = block.u(1)

        val rawDate = (block.u(4) shl 8) or block.u(5)
        if (rawDate == 0) return null // 未使用ブロック
        val date = try {
            LocalDate.of(2000 + (rawDate shr 9), (rawDate shr 5) and 0xF, rawDate and 0x1F)
        } catch (e: DateTimeException) {
            return null
        }

        val type = classify(terminal, process)
        val region = block.u(15)
        val stations = type == HistoryType.RAIL

        return HistoryEntry(
            type = type,
            date = date,
            balanceAfter = (block.u(11) shl 8) or block.u(10),
            amount = null,
            sequence = (block.u(12) shl 16) or (block.u(13) shl 8) or block.u(14),
            boarding = if (stations) station(region, block.u(6), block.u(7)) else null,
            alighting = if (stations) station(region, block.u(8), block.u(9)) else null
        )
    }

    private fun classify(terminal: Int, process: Int): HistoryType = when {
        process == PROCESS_CHARGE || process == PROCESS_AUTO_CHARGE -> HistoryType.CHARGE
        process == PROCESS_PURCHASE || terminal == TERMINAL_PURCHASE || terminal == TERMINAL_VENDING ->
            HistoryType.PURCHASE
        terminal == TERMINAL_BUS || process == PROCESS_BUS || process == PROCESS_BUS_PRICE -> HistoryType.BUS
        terminal in RAIL_TERMINALS ||
            process == PROCESS_RAIL_FARE || process == PROCESS_RAIL_ADJUST || process == PROCESS_RAIL_EXIT_ONLY ->
            HistoryType.RAIL
        else -> HistoryType.OTHER
    }

    /** 線区・駅順がともに 0 のときは「記録なし」として null にする。 */
    private fun station(region: Int, line: Int, station: Int): Station? =
        if (line == 0 && station == 0) null else Station(region, line, station)

    private fun ByteArray.u(index: Int): Int = this[index].toInt() and 0xFF
}
