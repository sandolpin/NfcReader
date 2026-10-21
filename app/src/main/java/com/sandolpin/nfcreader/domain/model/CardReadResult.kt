package com.sandolpin.nfcreader.domain.model

/** 読み取り直後の結果(まだ保存されていない)。履歴は新しい順。 */
data class CardReadResult(
    val cardId: String,
    val balance: Int,
    val history: List<HistoryEntry>
)
