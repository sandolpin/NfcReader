package com.sandolpin.nfcreader.domain.model

/** タグを1回読み取った結果。 */
sealed interface TagReadResult {
    /** 交通系IC。残高と履歴が読み取れた。 */
    data class Transit(val result: CardReadResult) : TagReadResult

    /**
     * 交通系ICとして読めなかったタグ(NDEF、クレジットカード、他のFeliCaなど)。種類とIDだけ分かる。
     * [paymentBrand] が null でなければ、非接触決済(クレジットカードなど)として確認できたもの。
     */
    data class Other(
        val type: TagType,
        val uid: String,
        val paymentBrand: CardBrand? = null
    ) : TagReadResult {
        val isPaymentCard: Boolean get() = paymentBrand != null
    }

    /** 想定外のエラー。 */
    data class Failure(val message: String?) : TagReadResult
}