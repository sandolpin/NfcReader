package com.sandolpin.nfcreader.domain.model

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val soundEnabled: Boolean = true,
    val historyLayout: HistoryLayout = HistoryLayout.LIST
)
