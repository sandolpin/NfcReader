@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.sandolpin.nfcreader.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.sandolpin.nfcreader.domain.model.ThemeMode

/**
 * アプリ全体のテーマ(Material 3 Expressive)。
 *
 * [themeMode] が SYSTEM のときだけ端末の設定に従う。LIGHT / DARK のときは端末と違っていてもその明暗にする。
 * その場合にステータスバー/ナビゲーションバーのアイコン色が合わなくなるので、ここで明暗に合わせて設定する。
 *
 * ※ Expressive の API はアルファ版のライブラリにしかない。解決できない場合は libs.versions.toml の
 *   material3 を新しいアルファ版に更新する。API が変わったときの修正は、このファイルだけで済む。
 */
@Composable
fun NfcReaderTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            // 背景が明るいときは、バーのアイコンを暗い色にする
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
            if (Build.VERSION.SDK_INT >= 29) {
                // 3ボタンナビゲーションでも、システムが半透明の板を重ねないようにする(背景をバーの下まで見せる)
                window.isNavigationBarContrastEnforced = false
            }
        }
    }

    MaterialExpressiveTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        motionScheme = MotionScheme.expressive(),
        content = content
    )
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}