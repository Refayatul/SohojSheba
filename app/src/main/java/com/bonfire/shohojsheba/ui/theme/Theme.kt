package com.bonfire.shohojsheba.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 1. Define the Light Scheme
private val LightColors = lightColorScheme(
    primary = AccentBlue,
    background = PrimaryBackground,
    onBackground = PrimaryTextColor,
    surface = Color.White,
    onSurface = PrimaryTextColor,
    secondary = SecondaryTextColor
)

// 2. Define the Dark Scheme
private val DarkColors = darkColorScheme(
    primary = DarkAccentBlue,     // Lighter blue for dark mode
    background = DarkBackground,  // Dark Grey/Black
    onBackground = DarkPrimaryText, // White text
    surface = DarkSurface,        // Dark Grey for cards/bars
    onSurface = DarkPrimaryText,  // White text on surfaces
    secondary = DarkSecondaryText
)

@Composable
fun ShohojShebaTheme(
    // 3. Add this parameter to detect system setting
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // 4. Select the correct color scheme
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()

            // 5. Logic for Status Bar Icons (Battery, Time, etc.)
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