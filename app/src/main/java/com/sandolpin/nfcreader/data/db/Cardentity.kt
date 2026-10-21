package com.sandolpin.nfcreader.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 保存済みカード。カードIDが主キーなので、同じカードを保存すると上書きされる。
 *
 * 外観は列を2つに分けて持つ(型変換クラスを避けるため)。
 * appearanceType は "PRESET"(appearanceValue は CardStyle の名前)か "CUSTOM"(appearanceValue は画像のパス)。
 */
@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey val cardId: String,
    val name: String,
    val appearanceType: String,
    val appearanceValue: String,
    val balance: Int,
    val lastUpdated: Long // エポックミリ秒
)