package com.sandolpin.nfcreader.ui

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandolpin.nfcreader.data.haptic.TagVibrator
import com.sandolpin.nfcreader.data.repository.NfcRepository
import com.sandolpin.nfcreader.data.repository.SettingsRepository
import com.sandolpin.nfcreader.data.sound.SoundPlayer
import com.sandolpin.nfcreader.data.sound.SoundType
import com.sandolpin.nfcreader.domain.model.AppSettings
import com.sandolpin.nfcreader.domain.model.CardReadResult
import com.sandolpin.nfcreader.domain.model.TagReadResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 画面側(AppRoot)が受け取る、一度きりの出来事。 */
sealed interface MainEvent {
    /** 交通系ICが読めたので、結果画面を開く。 */
    data object ShowResult : MainEvent

    /** 交通系IC以外(クレジットカード・NFCタグ)が読めたので、その結果画面を開く。 */
    data object ShowOtherResult : MainEvent

    /** 利用者に知らせたいメッセージ(Snackbar で表示)。 */
    data class ShowMessage(val text: String) : MainEvent
}

/**
 * Activity 全体の状態と、タグ読み取りの進行役。
 *
 * - 設定(テーマ)の最初の読み込みが終わるまでは [settings] が null で、その間はスプラッシュを維持する。
 * - タグを受け取ると、読み取り → 効果音 → 結果の保持 → 画面遷移の通知、を一か所で行う。
 *   結果を Activity スコープのここに持つので、画面を回転しても失われない。
 */
class MainViewModel(
    private val settingsRepository: SettingsRepository,
    private val nfcRepository: NfcRepository,
    private val soundPlayer: SoundPlayer,
    private val vibrator: TagVibrator
) : ViewModel() {

    val settings: StateFlow<AppSettings?> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _readResult = MutableStateFlow<CardReadResult?>(null)
    /** 最後に読み取れた交通系ICの結果。結果画面が表示する。 */
    val readResult: StateFlow<CardReadResult?> = _readResult.asStateFlow()

    private val _otherResult = MutableStateFlow<TagReadResult.Other?>(null)
    /** 最後に読み取れた、交通系IC以外のタグ。履歴は読めず、種類とIDだけ分かる。 */
    val otherResult: StateFlow<TagReadResult.Other?> = _otherResult.asStateFlow()

    private val _events = Channel<MainEvent>(Channel.BUFFERED)
    val events: Flow<MainEvent> = _events.receiveAsFlow()

    private var readJob: Job? = null

    fun onTagDiscovered(tag: Tag) {
        // 続けて別のタグがかざされたら、前の読み取りは打ち切って新しい方を優先する
        readJob?.cancel()
        // かざした瞬間に振動で知らせる(読み取りの完了や成否は待たない)
        vibrator.vibrate()
        readJob = viewModelScope.launch {
            when (val result = nfcRepository.read(tag)) {
                is TagReadResult.Transit -> {
                    _readResult.value = result.result
                    playSound(SoundType.TRANSIT)
                    _events.send(MainEvent.ShowResult)
                }
                is TagReadResult.Other -> {
                    _otherResult.value = result
                    playSound(if (result.isPaymentCard) SoundType.CREDIT else SoundType.TAG)
                    _events.send(MainEvent.ShowOtherResult)
                }
                is TagReadResult.Failure -> {
                    // 読み取りの失敗は無音。もう一度かざしてもらう
                    _events.send(MainEvent.ShowMessage("読み取れませんでした。もう一度かざしてください"))
                }
            }
        }
    }

    /** 設定で効果音がオフのときは鳴らさない。設定がまだ読めていない起動直後は、既定値(オン)に従う。 */
    private fun playSound(type: SoundType) {
        if (settings.value?.soundEnabled ?: AppSettings().soundEnabled) {
            soundPlayer.play(type)
        }
    }

    // soundPlayer はアプリ全体で共有しているので、ここでは release しない
    // (Activity が作り直されても使い続けるため。プロセス終了時にまとめて解放される)
}