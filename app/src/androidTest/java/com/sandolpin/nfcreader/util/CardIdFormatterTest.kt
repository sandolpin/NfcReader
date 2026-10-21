package com.sandolpin.nfcreader.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CardIdFormatterTest {
    @Test
    fun `16進数の大文字をコロンでつなぐ`() {
        val id = byteArrayOf(0x01, 0x01, 0x03, 0x12, 0xAB.toByte(), 0xFF.toByte())
        assertEquals("01:01:03:12:AB:FF", CardIdFormatter.format(id))
    }

    @Test
    fun `空なら空文字`() {
        assertEquals("", CardIdFormatter.format(ByteArray(0)))
    }
}
