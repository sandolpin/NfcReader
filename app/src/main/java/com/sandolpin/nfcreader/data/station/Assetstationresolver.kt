package com.sandolpin.nfcreader.data.station

import android.content.Context
import com.sandolpin.nfcreader.domain.felica.StationResolver
import com.sandolpin.nfcreader.domain.felica.StationTable

/**
 * assets/stations.csv から駅名を引く。ファイルが無い・読めない場合は空の表になり、駅はコード表示になる。
 * CSV の形式は [StationTable] を参照。初回の呼び出しで一度だけ読み込む。
 */
class AssetStationResolver(private val context: Context) : StationResolver {

    private val table: StationTable by lazy {
        runCatching {
            context.assets.open(ASSET_NAME).bufferedReader(Charsets.UTF_8).useLines { lines ->
                // BOM 付きUTF-8のCSVでも、先頭行が読めるように取り除く
                StationTable.parse(lines.map { it.removePrefix("﻿") })
            }
        }.getOrDefault(StationTable.EMPTY)
    }

    override fun resolve(regionCode: Int, lineCode: Int, stationCode: Int): String? =
        table.resolve(regionCode, lineCode, stationCode)

    private companion object {
        const val ASSET_NAME = "stations.csv"
    }
}