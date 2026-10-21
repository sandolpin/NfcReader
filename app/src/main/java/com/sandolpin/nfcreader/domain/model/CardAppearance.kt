package com.sandolpin.nfcreader.domain.model

/** カードの外観。プリセットか、ユーザーがアップロードした画像(アプリ内部ストレージ上のパス)。 */
sealed interface CardAppearance {
    data class Preset(val style: CardStyle) : CardAppearance
    data class Custom(val imagePath: String) : CardAppearance
}
