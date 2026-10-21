package com.sandolpin.nfcreader.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 画面に出す日付の整形。曜日は日本語(Locale.JAPAN)。 */
object DateFormats {

    private val date = DateTimeFormatter.ofPattern("yyyy年M月d日(E)", Locale.JAPAN)
    private val dateTime = DateTimeFormatter.ofPattern("yyyy年M月d日(E) HH:mm:ss", Locale.JAPAN)

    /** 例: 2025年12月18日(木) */
    fun date(value: LocalDate): String = date.format(value)

    /** 例: 2026年10月15日(木) 17:35:01 。[zone] は端末のタイムゾーンが既定。 */
    fun dateTime(value: Instant, zone: ZoneId = ZoneId.systemDefault()): String =
        dateTime.withZone(zone).format(value)
}