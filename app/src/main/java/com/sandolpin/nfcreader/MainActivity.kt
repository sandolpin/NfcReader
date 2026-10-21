package com.sandolpin.nfcreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sandolpin.nfcreader.ui.AppRoot
import com.sandolpin.nfcreader.ui.MainViewModel
import com.sandolpin.nfcreader.ui.theme.NfcReaderTheme
import kotlinx.coroutines.launch

/**
 * 単一Activity。
 *
 * - スプラッシュは、設定(ダークモード)の読み込みが終わるまで維持する。読み込み前の既定テーマで一瞬描画されて、
 *   画面がちらつくのを防ぐため。
 * - edge-to-edge を有効にして、背景をステータスバー/ナビゲーションバーの下まで広げる。
 *   中身が被らないようにする処理は、AppRoot の Scaffold と各画面で行う。
 * - NFCのリーダーモードは、画面が前面にある間(RESUMED)だけ有効にする。
 */
class MainActivity : ComponentActivity() {

    private val container get() = (application as NfcReaderApp).container

    private val viewModel: MainViewModel by viewModels {
        viewModelFactory {
            initializer {
                MainViewModel(
                    settingsRepository = container.settingsRepository,
                    nfcRepository = container.nfcRepository,
                    soundPlayer = container.soundPlayer,
                    vibrator = container.tagVibrator
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        splashScreen.setKeepOnScreenCondition { viewModel.settings.value == null }

        // 前面にある間だけ収集する。バックグラウンドに回ると自動でリーダーモードが解除される
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                container.nfcReader.tags(this@MainActivity).collect { tag ->
                    viewModel.onTagDiscovered(tag)
                }
            }
        }

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val current = settings
            if (current != null) {
                NfcReaderTheme(themeMode = current.themeMode) {
                    AppRoot(container = container, mainViewModel = viewModel)
                }
            }
        }
    }
}