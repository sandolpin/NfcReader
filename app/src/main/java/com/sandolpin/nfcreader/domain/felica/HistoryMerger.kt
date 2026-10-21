package com.sandolpin.nfcreader.domain.felica

import com.sandolpin.nfcreader.domain.model.HistoryEntry

/**
 * 保存済みの履歴と、新しく読み取った履歴を統合する。Android に依存しない。
 *
 * - 同じ取引は [HistoryEntry.identityKey] で判定して 1 件にまとめる(新しく読み取った側を採用)。
 * - 統合後に新しい順へ並べ直し、利用金額を残高の差から再計算する。
 * - 最も古い履歴と、連番が連続していない(間が抜けた)履歴の金額は null にする。
 */
object HistoryMerger {

    private const val SEQUENCE_MASK = 0xFFFFFF // 連番は 24bit

    /** [saved] と [incoming] はどちらも新しい順。結果も新しい順。 */
    fun merge(saved: List<HistoryEntry>, incoming: List<HistoryEntry>): List<HistoryEntry> {
        // 同じキーが複数ある場合に備えて、出現回数ぶんだけ相殺する
        val remaining = HashMap<String, Int>()
        incoming.forEach { remaining.merge(it.identityKey, 1, Int::plus) }

        val savedOnly = saved.filter { entry ->
            val count = remaining[entry.identityKey] ?: 0
            if (count > 0) {
                remaining[entry.identityKey] = count - 1
                false
            } else {
                true
            }
        }

        // 安定ソート。日付・連番が同じなら、読み取った側が先になる
        val sorted = (incoming + savedOnly)
            .sortedWith(compareByDescending<HistoryEntry> { it.date }.thenByDescending { it.sequence })
        return recalculateAmounts(sorted)
    }

    /** 新しい順の履歴について、利用金額(符号付き)を残高の差から計算し直す。 */
    fun recalculateAmounts(entries: List<HistoryEntry>): List<HistoryEntry> =
        entries.mapIndexed { index, newer ->
            val older = entries.getOrNull(index + 1)
            val amount = if (older != null && isContiguous(newer, older)) {
                newer.balanceAfter - older.balanceAfter
            } else {
                null
            }
            newer.copy(amount = amount)
        }

    /** 連番が取れていれば、連続している(間が抜けていない)ときだけ true。取れていなければ連続とみなす。 */
    private fun isContiguous(newer: HistoryEntry, older: HistoryEntry): Boolean =
        if (newer.sequence != 0 && older.sequence != 0) {
            ((older.sequence + 1) and SEQUENCE_MASK) == newer.sequence
        } else {
            true
        }
}
