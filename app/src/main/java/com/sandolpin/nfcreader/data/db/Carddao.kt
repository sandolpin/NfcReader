package com.sandolpin.nfcreader.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CardDao {

    @Query("SELECT * FROM cards ORDER BY lastUpdated DESC")
    abstract fun observeCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE cardId = :cardId")
    abstract fun observeCard(cardId: String): Flow<CardEntity?>

    @Query("SELECT * FROM cards WHERE cardId = :cardId")
    abstract suspend fun getCard(cardId: String): CardEntity?

    @Query("SELECT * FROM histories WHERE cardId = :cardId ORDER BY position ASC")
    abstract fun observeHistory(cardId: String): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM histories WHERE cardId = :cardId ORDER BY position ASC")
    abstract suspend fun getHistory(cardId: String): List<HistoryEntity>

    // 既存のカードは UPDATE になる(REPLACE ではないので、CASCADE で履歴が消えることはない)
    @Upsert
    abstract suspend fun upsertCard(card: CardEntity)

    @Insert
    abstract suspend fun insertHistories(items: List<HistoryEntity>)

    @Query("DELETE FROM histories WHERE cardId = :cardId")
    abstract suspend fun deleteHistories(cardId: String)

    /** 履歴ごとカードを削除する(履歴は CASCADE でも消える)。 */
    @Query("DELETE FROM cards WHERE cardId = :cardId")
    abstract suspend fun deleteCard(cardId: String)

    /** カードと履歴を1つのトランザクションで入れ替える。途中で失敗したら全体が元に戻る。 */
    @Transaction
    open suspend fun replaceCard(card: CardEntity, histories: List<HistoryEntity>) {
        upsertCard(card)
        deleteHistories(card.cardId)
        insertHistories(histories)
    }
}