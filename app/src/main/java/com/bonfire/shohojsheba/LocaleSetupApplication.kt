package com.bonfire.shohojsheba

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class LocaleSetupApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // This code runs once when the application starts.
        // It reads the saved language and applies it.
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val savedLang = sharedPreferences.getString("language", "en") // Default to English
        
        if (savedLang != null) {
            val appLocale = LocaleListCompat.forLanguageTags(savedLang)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }
}
