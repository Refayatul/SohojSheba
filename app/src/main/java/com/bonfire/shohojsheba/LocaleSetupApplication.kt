package com.bonfire.shohojsheba

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.bonfire.shohojsheba.util.NetworkStatusTracker

class LocaleSetupApplication : Application() {

    lateinit var networkStatusTracker: NetworkStatusTracker
        private set

    override fun onCreate() {
        super.onCreate()

        // Start the network status tracker
        // This runs once when the app starts and keeps watching for internet connection changes.
        networkStatusTracker = NetworkStatusTracker(this)
        networkStatusTracker.start()

        // Language setup code
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val savedLang = sharedPreferences.getString("language", "en") // Default to English

        if (savedLang != null) {
            val appLocale = LocaleListCompat.forLanguageTags(savedLang)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }

    override fun onTerminate() {
        networkStatusTracker.stop()
        super.onTerminate()
    }
}
