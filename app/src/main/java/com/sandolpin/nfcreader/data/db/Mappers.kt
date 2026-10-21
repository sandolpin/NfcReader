package com.sandolpin.nfcreader.data.db

import com.sandolpin.nfcreader.domain.model.CardAppearance
import com.sandolpin.nfcreader.domain.model.CardStyle
import com.sandolpin.nfcreader.domain.model.CardSummary
import com.sandolpin.nfcreader.domain.model.HistoryEntry
import com.sandolpin.nfcreader.domain.model.HistoryType
import com.sandolpin.nfcreader.domain.model.SavedCard
import com.sandolpin.nfcreader.domain.model.Station
import java.time.Instant
import java.time.LocalDate

private const val APPEARANCE_PRESET = "PRESET"
private const val APPEARANCE_CUSTOM = "CUSTOM"

// ---- 外観 ----

fun CardEntity.toAppearance(): CardAppearance =
    if (appearanceType == APPEARANCE_CUSTOM) {
        CardAppearance.Custom(appearanceValue)
    } else {
        // 保存値が壊れていても落とさず、既定のスタイルにする
        CardAppearance.Preset(runCatching { CardStyle.valueOf(appearanceValue) }.getOrDefault(CardStyle.GREEN))
    }

/** (appearanceType, appearanceValue) の組に変換する。 */
fun CardAppearance.toColumns(): Pair<String, String> = when (this) {
    is CardAppearance.Preset -> APPEARANCE_PRESET to style.name
    is CardAppearance.Custom -> APPEARANCE_CUSTOM to imagePath
}

// ---- カード ----

fun CardEntity.toSummary(): CardSummary = CardSummary(
    cardId = cardId,
    name = name,
    appearance = toAppearance(),
    balance = balance,
    lastUpdated = Instant.ofEpochMilli(lastUpdated)
)

fun CardEntity.toSavedCard(history: List<HistoryEntry>): SavedCard = SavedCard(
    cardId = cardId,
    name = name,
    appearance = toAppearance(),
    balance = balance,
    lastUpdated = Instant.ofEpochMilli(lastUpdated),
    history = history
)

// ---- 履歴 ----

fun HistoryEntity.toDomain(): HistoryEntry = HistoryEntry(
    type = runCatching { HistoryType.valueOf(type) }.getOrDefault(HistoryType.OTHER),
    date = LocalDate.ofEpochDay(epochDay),
    balanceAfter = balanceAfter,
    amount = amount,
    sequence = sequence,
    boarding = station(boardRegion, boardLine, boardStation),
    alighting = station(alightRegion, alightLine, alightStation)
)

fun HistoryEntry.toEntity(cardId: String, position: Int): HistoryEntity = HistoryEntity(
    cardId = cardId,
    position = position,
    type = type.name,
    epochDay = date.toEpochDay(),
    balanceAfter = balanceAfter,
    amount = amount,
    sequence = sequence,
    boardRegion = boarding?.regionCode,
    boardLine = boarding?.lineCode,
    boardStation = boarding?.stationCode,
    alightRegion = alighting?.regionCode,
    alightLine = alighting?.lineCode,
    alightStation = alighting?.stationCode
)

private fun station(region: Int?, line: Int?, station: Int?): Station? =
    if (region == null || line == null || station == null) null else Station(region, line, station)