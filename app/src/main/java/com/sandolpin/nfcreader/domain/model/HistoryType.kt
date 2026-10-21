package com.sandolpin.nfcreader.domain.model

/** 履歴1件の種別。判定は FelicaHistoryParser で行う。 */
enum class HistoryType {
    RAIL,      // 鉄道(改札機などでの入出場・運賃支払)
    BUS,       // バス
    PURCHASE,  // 物販
    CHARGE,    // チャージ(オートチャージ含む)
    OTHER      // 上記以外・判定不能
}
