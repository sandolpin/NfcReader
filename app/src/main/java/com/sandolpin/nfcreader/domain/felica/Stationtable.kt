package com.sandolpin.nfcreader.domain.felica

/**
 * 駅名コード表。CSV を読み込んで、(リージョン, 線区コード, 駅順コード) から駅名を引く。Android に依存しない。
 *
 * CSV の形式(1行1駅、16進数。「0x」は付けても付けなくてもよい):
 * ```
 * # コメント行
 * リージョン,線区コード,駅順コード,駅名
 * 00,E0,01,東京
 * ```
 * 数値として読めない行(見出し行など)は無視する。
 *
 * リージョンが一致する駅がなければ、「線区コード+駅順コード」が全体で1つの駅名にしか対応しない場合に限って、
 * リージョンを無視して引く。複数の駅名にまたがる場合は、誤った駅名を出さないよう null を返す。
 */
class StationTable private constructor(
    private val exact: Map<Triple<Int, Int, Int>, String>,
    private val loose: Map<Pair<Int, Int>, String?>
) {
    fun resolve(regionCode: Int, lineCode: Int, stationCode: Int): String? =
        exact[Triple(regionCode, lineCode, stationCode)] ?: loose[Pair(lineCode, stationCode)]

    companion object {
        val EMPTY = StationTable(emptyMap(), emptyMap())

        fun parse(lines: Sequence<String>): StationTable {
            val exact = HashMap<Triple<Int, Int, Int>, String>()
            val loose = HashMap<Pair<Int, Int>, String?>()

            for (raw in lines) {
                val line = raw.trim()
                if (line.isEmpty() || line.startsWith("#")) continue

                val cols = line.split(',')
                if (cols.size < 4) continue
                val region = cols[0].parseHex() ?: continue
                val lineCode = cols[1].parseHex() ?: continue
                val stationCode = cols[2].parseHex() ?: continue
                val name = cols[3].trim().removeSurrounding("\"")
                if (name.isEmpty()) continue

                exact[Triple(region, lineCode, stationCode)] = name

                val key = Pair(lineCode, stationCode)
                if (!loose.containsKey(key)) {
                    loose[key] = name
                } else if (loose[key] != name) {
                    loose[key] = null // 別の駅名と衝突するので、リージョン無視の検索では使わない
                }
            }
            return StationTable(exact, loose)
        }

        private fun String.parseHex(): Int? =
            trim().removePrefix("0x").removePrefix("0X").toIntOrNull(16)
    }
}