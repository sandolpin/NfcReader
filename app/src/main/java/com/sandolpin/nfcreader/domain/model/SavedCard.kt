package com.sandolpin.nfcreader.domain.model

import java.time.Instant

/** 保存済みカード。履歴は蓄積されたもので、新しい順。 */
data class SavedCard(
    val cardId: String,
    val name: String,
    val appearance: CardAppearance,
    val balance: Int,
    val lastUpdated: Instant,
    val history: List<HistoryEntry>
)
