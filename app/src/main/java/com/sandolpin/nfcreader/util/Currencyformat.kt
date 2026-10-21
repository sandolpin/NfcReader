package com.sandolpin.nfcreader.util

import java.text.NumberFormat
import java.util.Locale

/** 金額の整形。 */
object CurrencyFormat {

    /** 例: ¥1,200 */
    fun yen(amount: Int): String = "¥" + NumberFormat.getIntegerInstance(Locale.JAPAN).format(amount)

    /** 利用金額(符号付き)の表示。チャージは「+ ¥1,000」、支出は「¥160」。金額が不明(null)なら「-」。 */
    fun signedAmount(amount: Int?): String = when {
        amount == null -> "-"
        amount > 0 -> "+ " + yen(amount)
        else -> yen(-amount)
    }
}