package com.sandolpin.nfcreader.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// 画面イメージの青(ナビの選択中、保存ボタンなど)を基準にした配色。
// 端末の壁紙色(ダイナミックカラー)は使わず、デザインどおりの色に固定する。

val LightColors = lightColorScheme(
    primary = Color(0xFF0B63F6),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD9E2FF),
    onPrimaryContainer = Color(0xFF001A43),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF111111),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111111),
    surfaceVariant = Color(0xFFE3E3E3),
    onSurfaceVariant = Color(0xFF44474E),
    outline = Color(0xFFBDBDBD)
)

val DarkColors = darkColorScheme(
    primary = Color(0xFF8AB4FF),
    onPrimary = Color(0xFF002F6C),
    primaryContainer = Color(0xFF1B4DB3),
    onPrimaryContainer = Color(0xFFD9E2FF),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE6E6EA),
    surface = Color(0xFF111318),
    onSurface = Color(0xFFE6E6EA),
    surfaceVariant = Color(0xFF3A3C42),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099)
)