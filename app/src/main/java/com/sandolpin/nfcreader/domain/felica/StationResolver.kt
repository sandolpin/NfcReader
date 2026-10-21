package com.sandolpin.nfcreader.domain.felica

import com.sandolpin.nfcreader.domain.model.HistoryEntry
import com.sandolpin.nfcreader.domain.model.Station

/** 駅コード → 駅名。コード表がない、または未収録の駅は null を返す。 */
fun interface StationResolver {
    fun resolve(regionCode: Int, lineCode: Int, stationCode: Int): String?
}

/** 駅名を解決した [Station] を返す。 */
fun StationResolver.resolve(station: Station): Station =
    station.copy(name = resolve(station.regionCode, station.lineCode, station.stationCode))

/** 履歴の乗降駅に駅名を入れる。DBには駅コードだけを保存し、駅名は表示するときに解決する。 */
fun HistoryEntry.withStationNames(resolver: StationResolver): HistoryEntry =
    copy(
        boarding = boarding?.let { resolver.resolve(it) },
        alighting = alighting?.let { resolver.resolve(it) }
    )

/** コード表が見つかるまでの既定実装。常に未解決(コード表示になる)。 */
object NoStationResolver : StationResolver {
    override fun resolve(regionCode: Int, lineCode: Int, stationCode: Int): String? = null
}