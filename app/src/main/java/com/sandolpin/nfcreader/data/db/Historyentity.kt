package com.sandolpin.nfcreader.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 保存済みカードの利用履歴。カードが削除されると、外部キーの CASCADE で一緒に消える。
 *
 * - position: 新しい順の並び(0 が最新)。同じ日の取引の順序を保つため、日付とは別に持つ。
 * - 駅は駅コードだけを保存する。駅名は表示するときに駅名コード表から解決する。
 * - 保存は「そのカードの履歴を消して入れ直す」方式なので、重複判定用の一意制約は置かない。
 */
@Entity(
    tableName = "histories",
    foreignKeys = [
        ForeignKey(
            entity = CardEntity::class,
            parentColumns = ["cardId"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("cardId")]
)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cardId: String,
    val position: Int,
    val type: String,
    val epochDay: Long,
    val balanceAfter: Int,
    val amount: Int?,
    val sequence: Int,
    val boardRegion: Int?,
    val boardLine: Int?,
    val boardStation: Int?,
    val alightRegion: Int?,
    val alightLine: Int?,
    val alightStation: Int?
)