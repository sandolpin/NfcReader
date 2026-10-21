package com.sandolpin.nfcreader.domain.model

/**
 * 非接触決済カードのブランド。[displayName] は結果画面にそのまま出す表記。
 * [UNKNOWN] は「決済カードではあるが、ブランドを特定できなかった」場合。
 */
enum class CardBrand(val displayName: String) {
    VISA("VISA"),
    MASTERCARD("Mastercard"),
    AMEX("AMEX"),
    JCB("JCB"),
    DINERS("Diners"),
    DISCOVER("Discover"),
    UNIONPAY("銀聯"),
    UNKNOWN("");
}