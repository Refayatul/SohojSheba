package com.bonfire.shohojsheba

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * A CompositionLocal to hold the current app Locale. This allows us to update the locale
 * for the entire app and have Compose react to the change.
 */
val LocalLocale = staticCompositionLocalOf { Locale.getDefault() }

/**
 * A CompositionLocal to provide a function that allows changing the app locale.
 */
val LocalOnLocaleChange = staticCompositionLocalOf<(Locale) -> Unit> { { /* Default no-op */ } }

/**
 * A Composable that wraps the entire app, providing a context with an updated configuration
 * based on the current value of LocalLocale. This is what forces recomposition with new strings.
 */
@Composable
fun ProvideLocale(locale: Locale, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val newContext = remember(context, locale) {
        val newConfig = Configuration(context.resources.configuration)
        newConfig.setLocale(locale)
        context.createConfigurationContext(newConfig)
    }

    CompositionLocalProvider(
        LocalContext provides newContext,
        content = content
    )
}
