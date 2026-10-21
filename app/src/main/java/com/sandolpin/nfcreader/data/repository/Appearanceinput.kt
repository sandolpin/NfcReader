package com.sandolpin.nfcreader.data.repository

import android.net.Uri
import com.sandolpin.nfcreader.domain.model.CardStyle

/**
 * 保存・編集のときに、UI が指定するカードの外観。
 * 画像は Uri のまま受け取り、Repository がアプリ内部ストレージへコピーして保管する。
 */
sealed interface AppearanceInput {
    /** プリセットのスタイルを使う。 */
    data class Preset(val style: CardStyle) : AppearanceInput

    /** 選ばれた画像を取り込んで使う。 */
    data class Image(val uri: Uri) : AppearanceInput

    /** すでに保存されている外観をそのまま使う(新規カードなら既定のスタイル)。 */
    data object Keep : AppearanceInput
}