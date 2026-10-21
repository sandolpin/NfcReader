package com.sandolpin.nfcreader.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/** 下部ナビゲーションの3タブ。route は NavHost の宛先名。 */
enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    HOME("home", "読み取り", Icons.Filled.CreditCard),
    HISTORY("history", "履歴", Icons.AutoMirrored.Filled.List),
    SETTINGS("settings", "設定", Icons.Filled.Settings)
}