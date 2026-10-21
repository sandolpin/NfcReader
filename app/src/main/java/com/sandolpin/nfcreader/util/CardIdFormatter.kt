package com.sandolpin.nfcreader.util

/** カードID(IDm などのバイト列)を「01:01:03:12:…」の形式に整える。 */
object CardIdFormatter {
    fun format(id: ByteArray): String = id.joinToString(":") { "%02X".format(it.toInt() and 0xFF) }
}
