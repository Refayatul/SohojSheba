package com.bonfire.shohojsheba.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 1. Define the Light Scheme (Inspired by DiaBite)
private val LightColors = lightColorScheme(
    primary = PrimaryBlue, // Main buttons, focus state
    secondary = SupportingTeal, // Secondary buttons, icons, highlights
    tertiary = PrimaryBlue, // Fallback
    background = BackgroundLightGray, // Soft background
    surface = NeutralWhite, // Card background
    onPrimary = NeutralWhite, // Text on primary blue buttons
    onSecondary = NeutralWhite, // Text on supporting teal
    onBackground = TextDarkGray, // Body text color
    onSurface = TextDarkGray, // Text on white cards
)

// 2. Define the Dark Scheme
private val DarkColors = darkColorScheme(
    primary = DarkPrimaryBlue,
    secondary = DarkSupportingTeal,
    tertiary = DarkPrimaryBlue,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkBackground, // Dark text on light primary
    onSecondary = DarkBackground,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface
)

@Composable
fun ShohojShebaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Default to false to enforce our custom theme for now
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            
            // Use transparent status bar for edge-to-edge look
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()

            // Logic for Status Bar Icons
            // If darkTheme is true, we want light icons (so isAppearanceLightStatusBars = false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}