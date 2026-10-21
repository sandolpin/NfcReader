package com.sandolpin.nfcreader.di

import android.content.Context
import com.sandolpin.nfcreader.data.db.AppDatabase
import com.sandolpin.nfcreader.data.image.CardImageStorage
import com.sandolpin.nfcreader.data.haptic.TagVibrator
import com.sandolpin.nfcreader.data.nfc.FelicaCommand
import com.sandolpin.nfcreader.data.nfc.IsoDepCommand
import com.sandolpin.nfcreader.data.nfc.NfcReader
import com.sandolpin.nfcreader.data.nfc.NfcStatusProvider
import com.sandolpin.nfcreader.data.repository.CardRepository
import com.sandolpin.nfcreader.data.repository.NfcRepository
import com.sandolpin.nfcreader.data.repository.SettingsRepository
import com.sandolpin.nfcreader.data.settings.SettingsDataStore
import com.sandolpin.nfcreader.data.sound.SoundPlayer
import com.sandolpin.nfcreader.data.sound.SoundPoolPlayer
import com.sandolpin.nfcreader.data.station.AssetStationResolver
import com.sandolpin.nfcreader.domain.felica.StationResolver

/**
 * 手動DI。アプリ全体で共有する部品をここで1回だけ作る([com.sandolpin.nfcreader.NfcReaderApp] が保持)。
 * ViewModel は、ここから必要な Repository を受け取る(Factory 経由)。
 * 後から Hilt や Koin に移すときも、このクラスの中身が定義の一覧になる。
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database: AppDatabase by lazy { AppDatabase.build(appContext) }

    val stationResolver: StationResolver = AssetStationResolver(appContext)
    val imageStorage = CardImageStorage(appContext)

    val nfcStatusProvider = NfcStatusProvider(appContext)
    val nfcReader = NfcReader(nfcStatusProvider)
    val nfcRepository = NfcRepository(FelicaCommand(), IsoDepCommand(), stationResolver)

    val cardRepository: CardRepository by lazy {
        CardRepository(database.cardDao(), imageStorage, stationResolver)
    }
    val settingsRepository = SettingsRepository(SettingsDataStore(appContext))

    val tagVibrator = TagVibrator(appContext)

    val soundPlayer: SoundPlayer by lazy { SoundPoolPlayer(appContext) }
}