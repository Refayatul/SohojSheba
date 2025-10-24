package com.bonfire.shohojsheba.utils

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

val LocalLocale = staticCompositionLocalOf<Locale> { error("No Locale provided") }
val LocalOnLocaleChange = staticCompositionLocalOf<(Locale) -> Unit> { error("No onLocaleChange provided") }

@Composable
fun ProvideLocale(locale: Locale, content: @Composable () -> Unit) {
    val rememberLocale = remember { locale }
    val configuration = LocalConfiguration.current
    configuration.setLocale(rememberLocale)
    val localContext = LocalConfiguration.current
    localContext.setLocale(rememberLocale)

    CompositionLocalProvider(LocalLocale provides rememberLocale) {
        content()
    }
}
