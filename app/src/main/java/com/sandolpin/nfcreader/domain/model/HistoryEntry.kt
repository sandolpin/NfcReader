package com.sandolpin.nfcreader.domain.model

import java.time.LocalDate

/**
 * 利用履歴1件。
 *
 * @property amount 利用金額(符号付き)。チャージは正、支出は負。
 *   前後の履歴の残高差から求めるため、最も古い履歴や、前の履歴との間が抜けている履歴は null(UI では「-」)。
 * @property sequence カード内の連番(0 は取得できなかったことを表す)。
 */
data class HistoryEntry(
    val type: HistoryType,
    val date: LocalDate,
    val balanceAfter: Int,
    val amount: Int?,
    val sequence: Int,
    val boarding: Station? = null,
    val alighting: Station? = null
) {
    /**
     * 同じ取引かどうかを判定するキー。
     * 連番が取れる場合はそれを優先し、取れない場合は日付・種別・残高・駅コードの組み合わせで代用する。
     */
    val identityKey: String
        get() = if (sequence != 0) {
            "s$sequence|$balanceAfter|$date"
        } else {
            "d$date|$type|$balanceAfter|${boarding?.codeKey}|${alighting?.codeKey}"
        }
}
