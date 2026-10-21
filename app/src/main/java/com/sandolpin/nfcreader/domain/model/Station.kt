package com.sandolpin.nfcreader.domain.model

/**
 * 駅。カードに入っているのは数値コードのみで、[name] は駅名コード表から解決できたときだけ入る。
 */
data class Station(
    val regionCode: Int,
    val lineCode: Int,
    val stationCode: Int,
    val name: String? = null
) {
    /** 駅名が解決できないときの表示用(コード表示)。 */
    val displayName: String
        get() = name ?: "%02X-%02X".format(lineCode, stationCode)

    /** 重複判定などに使う、コードだけから作るキー。 */
    val codeKey: String
        get() = "$regionCode-$lineCode-$stationCode"
}
