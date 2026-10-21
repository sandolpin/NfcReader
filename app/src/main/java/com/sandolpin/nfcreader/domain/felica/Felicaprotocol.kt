package com.sandolpin.nfcreader.domain.felica

/**
 * FeliCa の「Read Without Encryption」コマンドの組み立てと応答の解析。Android に依存しない。
 *
 * コマンド(1サービス・1ブロック、16バイト):
 * ```
 * [0]      全体の長さ(16)
 * [1]      コマンドコード 0x06
 * [2..9]   IDm
 * [10]     サービス数(1)
 * [11..12] サービスコード(リトルエンディアン)
 * [13]     ブロック数(1)
 * [14..15] ブロックリスト要素(0x80, ブロック番号)
 * ```
 * 応答:
 * ```
 * [0] 長さ  [1] 0x07  [2..9] IDm  [10..11] ステータスフラグ(0,0 で成功)  [12] ブロック数  [13..] データ(16バイト × n)
 * ```
 */
object FelicaProtocol {

    /** 交通系ICの利用履歴のサービスコード。 */
    const val TRANSIT_HISTORY_SERVICE = 0x090F

    /** 交通系ICが保持する履歴の最大件数(ブロック数)。 */
    const val MAX_HISTORY_BLOCKS = 20

    private const val CMD_READ_WITHOUT_ENCRYPTION = 0x06
    private const val RES_READ_WITHOUT_ENCRYPTION = 0x07
    private const val IDM_SIZE = 8
    private const val BLOCK_SIZE = 16

    fun buildReadCommand(idm: ByteArray, serviceCode: Int, blockNumber: Int): ByteArray {
        require(idm.size == IDM_SIZE) { "IDm は8バイトである必要があります" }
        require(blockNumber in 0..0xFF) { "ブロック番号が範囲外です: $blockNumber" }

        val command = ByteArray(16)
        command[0] = 16
        command[1] = CMD_READ_WITHOUT_ENCRYPTION.toByte()
        idm.copyInto(command, destinationOffset = 2)
        command[10] = 1
        command[11] = (serviceCode and 0xFF).toByte()
        command[12] = ((serviceCode shr 8) and 0xFF).toByte()
        command[13] = 1
        command[14] = 0x80.toByte()
        command[15] = blockNumber.toByte()
        return command
    }

    /** 応答から先頭の1ブロック(16バイト)を取り出す。失敗(ステータス異常・短すぎる応答など)は null。 */
    fun parseReadResponse(response: ByteArray?): ByteArray? {
        if (response == null || response.size < 13 + BLOCK_SIZE) return null
        if ((response[1].toInt() and 0xFF) != RES_READ_WITHOUT_ENCRYPTION) return null
        if (response[10].toInt() != 0 || response[11].toInt() != 0) return null
        if ((response[12].toInt() and 0xFF) < 1) return null
        return response.copyOfRange(13, 13 + BLOCK_SIZE)
    }
}