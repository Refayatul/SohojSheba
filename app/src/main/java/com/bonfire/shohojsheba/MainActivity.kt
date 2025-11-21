package com.bonfire.shohojsheba

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bonfire.shohojsheba.navigation.AppNavGraph
import com.bonfire.shohojsheba.navigation.BottomNavBar
import com.bonfire.shohojsheba.navigation.Routes
import com.bonfire.shohojsheba.ui.theme.ShohojShebaTheme
import com.bonfire.shohojsheba.utils.LocalLocale
import com.bonfire.shohojsheba.utils.LocalOnLocaleChange
import com.bonfire.shohojsheba.utils.ProvideLocale
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

            // ------------------------------------------------------------
            // 1. LANGUAGE LOGIC
            // ------------------------------------------------------------
            val savedLang = remember { sharedPreferences.getString("language", "bn") ?: "bn" }
            val (locale, setLocale) = remember { mutableStateOf(Locale(savedLang)) }
            val onLocaleChange: (Locale) -> Unit = { newLocale ->
                setLocale(newLocale)
                sharedPreferences.edit().putString("language", newLocale.language).apply()
            }

            // ------------------------------------------------------------
            // 2. THEME LOGIC (NEW)
            // ------------------------------------------------------------
            // Values: "light", "dark", "system"
            val savedThemeMode = remember {
                mutableStateOf(sharedPreferences.getString("theme_mode", "system") ?: "system")
            }

            // Check what the phone system is currently doing
            val systemInDarkTheme = isSystemInDarkTheme()

            // Decide whether to show Dark Mode based on preference
            val useDarkTheme = when (savedThemeMode.value) {
                "light" -> false
                "dark" -> true
                else -> systemInDarkTheme // "system" (default)
            }

            // Function to switch theme (passed down to SettingsScreen)
            val onThemeChange: (String) -> Unit = { newMode ->
                savedThemeMode.value = newMode
                sharedPreferences.edit().putString("theme_mode", newMode).apply()
            }

            // ------------------------------------------------------------
            // 3. APP CONTENT
            // ------------------------------------------------------------
            var searchQuery by remember { mutableStateOf("") }
            val context = LocalContext.current
            val navController = rememberNavController()

            val voiceLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                val data: Intent? = result.data
                val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                results?.firstOrNull()?.let {
                    searchQuery = it
                }
            }

            // Pass the calculated 'useDarkTheme' here
            ShohojShebaTheme(darkTheme = useDarkTheme) {
                ProvideLocale(locale = locale) {
                    CompositionLocalProvider(
                        LocalLocale provides locale,
                        LocalOnLocaleChange provides onLocaleChange
                    ) {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        // Hide bottom nav on Settings and Service Detail screens
                        val showBottomNav = currentRoute != "settings" && currentRoute?.startsWith("service_detail") != true

                        Scaffold(
                            topBar = {
                                if (currentRoute == "home") {
                                    CenterAlignedTopAppBar(
                                        title = {
                                            Text(
                                                text = stringResource(id = R.string.app_name),
                                                color = MaterialTheme.colorScheme.onBackground,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        actions = {
                                            IconButton(onClick = { navController.navigate("settings") }) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Settings,
                                                    contentDescription = stringResource(id = R.string.settings),
                                                    tint = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        },
                                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                            // Use 'surface' so it adapts to dark/light mode
                                            containerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                }
                            },
                            bottomBar = { if (showBottomNav) BottomNavBar(navController = navController) },
                            floatingActionButton = {
                                // We only want to show this button on the home screen
                                if (currentRoute == Routes.HOME) {
                                    FloatingActionButton(
                                        onClick = { navController.navigate(Routes.CHAT) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Chat,
                                            contentDescription = stringResource(id = R.string.ai_assistant)
                                        )
                                    }
                                }
                            }
                        ) { paddingValues ->
                            // IMPORTANT: Your AppNavGraph must be updated to accept
                            // currentThemeMode and onThemeChange params!
                            AppNavGraph(
                                modifier = Modifier.padding(paddingValues),
                                navController = navController,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                onVoiceSearchClick = {
                                    try {
                                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(
                                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                            )
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                                        }
                                        voiceLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "Voice input not supported on this device",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                // Pass these to AppNavGraph -> SettingsScreen
                                currentThemeMode = savedThemeMode.value,
                                onThemeChange = onThemeChange
                            )
                        }
                    }
                }
            }
        }
    }
}