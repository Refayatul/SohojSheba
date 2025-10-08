package com.bonfire.shohojsheba

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.bonfire.shohojsheba.navigation.AppNavGraph
import com.bonfire.shohojsheba.ui.theme.ShohojShebaTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val savedLang = remember { sharedPreferences.getString("language", "bn") ?: "bn" }
            val (locale, setLocale) = remember { mutableStateOf(Locale(savedLang)) }

            val onLocaleChange: (Locale) -> Unit = { newLocale ->
                setLocale(newLocale)
                sharedPreferences.edit().putString("language", newLocale.language).apply()
            }

            CompositionLocalProvider(
                LocalLocale provides locale,
                LocalOnLocaleChange provides onLocaleChange
            ) {
                ProvideLocale(locale = locale) {
                    ShohojShebaTheme {
                        AppNavGraph()
                    }
                }
            }
        }
    }
}
