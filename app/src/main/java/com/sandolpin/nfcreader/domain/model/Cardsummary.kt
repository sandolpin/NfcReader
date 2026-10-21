package com.sandolpin.nfcreader.domain.model

import java.time.Instant

/** 履歴画面の一覧に出す要約(履歴の明細は持たない)。 */
data class CardSummary(
    val cardId: String,
    val name: String,
    val appearance: CardAppearance,
    val balance: Int,
    val lastUpdated: Instant
)