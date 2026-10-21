package com.sandolpin.nfcreader.ui.home

import com.sandolpin.nfcreader.domain.model.NfcStatus

data class HomeUiState(
    val nfcStatus: NfcStatus = NfcStatus.ENABLED,
    val isHelpVisible: Boolean = false
)