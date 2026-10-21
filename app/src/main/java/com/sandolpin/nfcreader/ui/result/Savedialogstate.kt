package com.sandolpin.nfcreader.ui.result

import android.net.Uri
import com.sandolpin.nfcreader.data.repository.AppearanceInput
import com.sandolpin.nfcreader.domain.model.CardAppearance
import com.sandolpin.nfcreader.domain.model.CardStyle

/** 保存ダイアログで選ばれている外観。 */
sealed interface AppearanceSelection {
    /** プリセットのスタイル。 */
    data class Preset(val style: CardStyle) : AppearanceSelection

    /** 今回アップロードで選んだ画像(保存するときにアプリ内へ取り込む)。 */
    data class NewImage(val uri: Uri) : AppearanceSelection

    /** 保存済みカードの、いまの外観のまま。 */
    data class KeepExisting(val appearance: CardAppearance) : AppearanceSelection

    fun toInput(): AppearanceInput = when (this) {
        is Preset -> AppearanceInput.Preset(style)
        is NewImage -> AppearanceInput.Image(uri)
        is KeepExisting -> AppearanceInput.Keep
    }
}

/** 「カードを保存」ダイアログの、保存前の一時的な入力。ダイアログを閉じたら破棄する。 */
data class SaveDialogState(
    val name: String = "",
    val selection: AppearanceSelection = AppearanceSelection.Preset(CardStyle.GREEN),
    val isSaving: Boolean = false
) {
    /** 名前の入力が必須。空白だけの名前は不可。保存中は押せない。 */
    val canSave: Boolean get() = name.isNotBlank() && !isSaving

    /** グリッドで選択中のプリセット(画像を選んでいるときは null)。 */
    val selectedStyle: CardStyle?
        get() = when (val s = selection) {
            is AppearanceSelection.Preset -> s.style
            is AppearanceSelection.KeepExisting -> (s.appearance as? CardAppearance.Preset)?.style
            is AppearanceSelection.NewImage -> null
        }
}