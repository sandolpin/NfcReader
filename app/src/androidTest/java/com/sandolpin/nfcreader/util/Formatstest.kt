package com.sandolpin.nfcreader.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class FormatsTest {

    @Test
    fun `日付は曜日つきの日本語表記`() {
        assertEquals("2025年12月18日(木)", DateFormats.date(LocalDate.of(2025, 12, 18)))
        assertEquals("2026年1月5日(月)", DateFormats.date(LocalDate.of(2026, 1, 5)))
    }

    @Test
    fun `日時は指定したタイムゾーンで表示する`() {
        val instant = Instant.parse("2026-10-15T08:35:01Z")
        assertEquals("2026年10月15日(木) 17:35:01", DateFormats.dateTime(instant, ZoneId.of("Asia/Tokyo")))
        assertEquals("2026年10月15日(木) 08:35:01", DateFormats.dateTime(instant, ZoneId.of("UTC")))
    }

    @Test
    fun `金額は3桁区切り`() {
        assertEquals("¥0", CurrencyFormat.yen(0))
        assertEquals("¥1,200", CurrencyFormat.yen(1200))
        assertEquals("¥4,560", CurrencyFormat.yen(4560))
    }

    @Test
    fun `利用金額はチャージに+をつけ、支出は符号なし、不明は-`() {
        assertEquals("+ ¥1,000", CurrencyFormat.signedAmount(1000))
        assertEquals("¥160", CurrencyFormat.signedAmount(-160))
        assertEquals("¥0", CurrencyFormat.signedAmount(0))
        assertEquals("-", CurrencyFormat.signedAmount(null))
    }
}