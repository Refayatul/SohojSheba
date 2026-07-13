package com.bonfire.shohojsheba.utils

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Helper class to manage per-app language preferences
 * Supports Android 13+ (LocaleManager) and backward compatibility (AppCompatDelegate)
 */
class AppLocaleManager(private val context: Context) {

    /**
     * Change the app's language
     * @param languageCode Language code (e.g., "en", "bn")
     */
    // --- Language Switching Logic ---
    // Android 13 (Tiramisu) introduced a system-level per-app language setting.
    // We must use 'LocaleManager' for API 33+ and 'AppCompatDelegate' for older versions.
    fun changeLanguage(languageCode: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+): Use LocaleManager
            // This persists the setting in the OS settings for the app.
            context.getSystemService(LocaleManager::class.java)
                .applicationLocales = LocaleList.forLanguageTags(languageCode)
        } else {
            // Android 12 and below: Use AppCompatDelegate
            // This stores the preference internally in the app's storage.
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(languageCode)
            )
        }
    }

    /**
     * Get the current app language code
     * @return Language code (e.g., "en", "bn") or default language
     */
    fun getCurrentLanguageCode(): String {
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+: Get from LocaleManager
            context.getSystemService(LocaleManager::class.java)
                .applicationLocales
                .get(0)
        } else {
            // Android 12 and below: Get from AppCompatDelegate
            AppCompatDelegate.getApplicationLocales().get(0)
        }
        return locale?.language ?: getDefaultLanguageCode()
    }

    /**
     * Get the default language code
     * @return Default language code ("en")
     */
    private fun getDefaultLanguageCode(): String {
        return "en"
    }

    /**
     * Reset app language to system default
     */
    fun resetToSystemLanguage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java)
                .applicationLocales = LocaleList.getEmptyLocaleList()
        } else {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.getEmptyLocaleList()
            )
        }
    }
}

/**
 * Data class representing a language option
 */
data class Language(
    val code: String,
    val displayName: String,
    val flag: String
)

/**
 * Available app languages
 */
val appLanguages = listOf(
    Language("en", "English", "🇬🇧"),
    Language("bn", "বাংলা", "🇧🇩")
)
