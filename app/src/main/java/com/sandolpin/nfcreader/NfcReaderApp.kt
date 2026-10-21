package com.sandolpin.nfcreader

import android.app.Application
import com.sandolpin.nfcreader.di.AppContainer

/** アプリ全体の入口。手動DIの [AppContainer] をここで作って保持する。 */
class NfcReaderApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}