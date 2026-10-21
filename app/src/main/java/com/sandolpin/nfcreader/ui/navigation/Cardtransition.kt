package com.sandolpin.nfcreader.ui.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 一覧から詳細へ、カード画像を飛ばして見せる shared element transition の部品。
 *
 * 一覧のカード画像と、詳細のカード画像に、同じキー(card-カードID)の [modifierFor] を付けると、
 * 画面遷移のあいだ、2つの画像がつながって動く。遷移に必要な2つの情報(SharedTransitionScope と
 * AnimatedVisibilityScope)を、画面まで1つにまとめて渡すためのクラス。
 *
 * 付ける位置の注意: sharedElement は、大きさを決める Modifier(size / aspectRatio など)より前に置く。
 * 画像の Modifier の先頭に付けること。
 */
@OptIn(ExperimentalSharedTransitionApi::class)
class CardTransition(
    private val sharedScope: SharedTransitionScope,
    private val visibilityScope: AnimatedVisibilityScope
) {
    @Composable
    fun modifierFor(cardId: String): Modifier = with(sharedScope) {
        // 引数名は Compose のバージョンで違う(sharedContentState / state)ので、位置で渡す
        Modifier.sharedElement(
            rememberSharedContentState(key = "card-$cardId"),
            visibilityScope
        )
    }
}