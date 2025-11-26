package com.bonfire.shohojsheba.utils

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

val LocalLocale = staticCompositionLocalOf<Locale> { error("No Locale provided") }
val LocalOnLocaleChange = staticCompositionLocalOf<(Locale, String?) -> Unit> { error("No onLocaleChange provided") }

@Composable
fun ProvideLocale(locale: Locale, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val localizedContext = remember(context, locale) {
        context.createLocaleWrapper(locale)
    }
    CompositionLocalProvider(LocalContext provides localizedContext, LocalLocale provides locale) {
        content()
    }
}

fun Context.createLocaleWrapper(locale: Locale): ContextWrapper {
    val configuration = this.resources.configuration
    configuration.setLocale(locale)
    val context = this.createConfigurationContext(configuration)
    return ContextWrapper(context)
}
