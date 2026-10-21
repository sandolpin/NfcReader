package com.sandolpin.nfcreader.data.repository

import com.sandolpin.nfcreader.data.db.CardDao
import com.sandolpin.nfcreader.data.db.CardEntity
import com.sandolpin.nfcreader.data.db.toAppearance
import com.sandolpin.nfcreader.data.db.toColumns
import com.sandolpin.nfcreader.data.db.toDomain
import com.sandolpin.nfcreader.data.db.toEntity
import com.sandolpin.nfcreader.data.db.toSavedCard
import com.sandolpin.nfcreader.data.db.toSummary
import com.sandolpin.nfcreader.data.image.CardImageStorage
import com.sandolpin.nfcreader.domain.felica.HistoryMerger
import com.sandolpin.nfcreader.domain.felica.StationResolver
import com.sandolpin.nfcreader.domain.felica.withStationNames
import com.sandolpin.nfcreader.domain.model.CardAppearance
import com.sandolpin.nfcreader.domain.model.CardReadResult
import com.sandolpin.nfcreader.domain.model.CardStyle
import com.sandolpin.nfcreader.domain.model.CardSummary
import com.sandolpin.nfcreader.domain.model.SavedCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant

/**
 * 保存済みカードの管理。
 *
 * - 保存: 同じカードIDは上書きし、履歴は [HistoryMerger] で蓄積する(古い履歴を消さない)。
 * - 削除: カードと履歴(CASCADE)に加えて、アップロード画像のファイルも消す。
 * - 画像ファイルの後始末は「DBの更新が成功してから」行う。失敗時に画像だけ消えてカードが残る状態を避ける。
 */
class CardRepository(
    private val dao: CardDao,
    private val imageStorage: CardImageStorage,
    private val stations: StationResolver,
    private val now: () -> Instant = Instant::now
) {

    fun observeSummaries(): Flow<List<CardSummary>> =
        dao.observeCards().map { cards -> cards.map { it.toSummary() } }

    /** 詳細画面用。カードと履歴のどちらが変わっても流れてくる。削除されたら null。 */
    fun observeCard(cardId: String): Flow<SavedCard?> =
        combine(dao.observeCard(cardId), dao.observeHistory(cardId)) { card, history ->
            card?.toSavedCard(history.map { it.toDomain().withStationNames(stations) })
        }

    /** 保存済みなら、その要約。保存ダイアログの初期値(名前・外観)に使う。 */
    suspend fun findSummary(cardId: String): CardSummary? = dao.getCard(cardId)?.toSummary()

    /** 読み取った結果を保存する。保存済みのカードなら上書きし、履歴は統合する。 */
    suspend fun save(result: CardReadResult, name: String, appearance: AppearanceInput) {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "名前が空です" }

        val existing = dao.getCard(result.cardId)
        val newAppearance = resolveAppearance(existing, appearance)

        try {
            val saved = dao.getHistory(result.cardId).map { it.toDomain() }
            val merged = HistoryMerger.merge(saved, result.history)
            val (type, value) = newAppearance.toColumns()
            val card = CardEntity(
                cardId = result.cardId,
                name = trimmed,
                appearanceType = type,
                appearanceValue = value,
                balance = result.balance,
                lastUpdated = now().toEpochMilli()
            )
            dao.replaceCard(card, merged.mapIndexed { index, entry -> entry.toEntity(result.cardId, index) })
        } catch (e: Throwable) {
            discardNewImage(appearance, newAppearance) // 取り込んだ画像だけが残らないようにする
            throw e
        }
        deleteReplacedImage(existing, newAppearance)
    }

    /** 保存済みカードの名前と外観を変える(編集)。履歴や残高は変えない。 */
    suspend fun update(cardId: String, name: String, appearance: AppearanceInput) {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "名前が空です" }

        val existing = dao.getCard(cardId) ?: return
        val newAppearance = resolveAppearance(existing, appearance)

        try {
            val (type, value) = newAppearance.toColumns()
            // 編集では「最終更新」(カードを読み取った日時)は変えない
            dao.upsertCard(existing.copy(name = trimmed, appearanceType = type, appearanceValue = value))
        } catch (e: Throwable) {
            discardNewImage(appearance, newAppearance)
            throw e
        }
        deleteReplacedImage(existing, newAppearance)
    }

    /** カードを削除する。再表示するにはもう一度読み取りが必要(蓄積した古い履歴は戻らない)。 */
    suspend fun delete(cardId: String) {
        val existing = dao.getCard(cardId) ?: return
        dao.deleteCard(cardId)
        (existing.toAppearance() as? CardAppearance.Custom)?.let { imageStorage.delete(it.imagePath) }
    }

    // ---- 外観の解決と画像の後始末 ----

    private suspend fun resolveAppearance(existing: CardEntity?, input: AppearanceInput): CardAppearance =
        when (input) {
            is AppearanceInput.Preset -> CardAppearance.Preset(input.style)
            is AppearanceInput.Image -> CardAppearance.Custom(imageStorage.import(input.uri))
            AppearanceInput.Keep -> existing?.toAppearance() ?: CardAppearance.Preset(CardStyle.GREEN)
        }

    /** 保存に失敗したとき、今回取り込んだ画像(Image 指定のとき)を消す。 */
    private fun discardNewImage(input: AppearanceInput, resolved: CardAppearance) {
        if (input is AppearanceInput.Image && resolved is CardAppearance.Custom) {
            imageStorage.delete(resolved.imagePath)
        }
    }

    /** 外観が変わって使われなくなった古い画像を消す。 */
    private fun deleteReplacedImage(existing: CardEntity?, newAppearance: CardAppearance) {
        val old = existing?.toAppearance() as? CardAppearance.Custom ?: return
        val stillUsed = (newAppearance as? CardAppearance.Custom)?.imagePath == old.imagePath
        if (!stillUsed) imageStorage.delete(old.imagePath)
    }
}