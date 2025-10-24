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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.bonfire.shohojsheba.navigation.AppNavGraph
import com.bonfire.shohojsheba.navigation.BottomNavBar
import com.bonfire.shohojsheba.ui.theme.ShohojShebaTheme
import com.bonfire.shohojsheba.utils.LocalLocale
import com.bonfire.shohojsheba.utils.LocalOnLocaleChange
import com.bonfire.shohojsheba.utils.ProvideLocale
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val savedLang = remember { sharedPreferences.getString("language", "bn") ?: "bn" }
            val (locale, setLocale) = remember { mutableStateOf(Locale(savedLang)) }

            val onLocaleChange: (Locale) -> Unit = { newLocale ->
                setLocale(newLocale)
                sharedPreferences.edit().putString("language", newLocale.language).apply()
            }

            var searchQuery by remember { mutableStateOf("") }
            val context = LocalContext.current
            val navController = rememberNavController()

            // Move the launcher here in MainActivity's composable scope
            val voiceLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                val data: Intent? = result.data
                val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                results?.firstOrNull()?.let {
                    searchQuery = it
                }
            }

            ShohojShebaTheme {
                CompositionLocalProvider(
                    LocalLocale provides locale,
                    LocalOnLocaleChange provides onLocaleChange
                ) {
                    ProvideLocale(locale = locale) {
                        Scaffold(
                            bottomBar = { BottomNavBar(navController = navController) }
                        ) { paddingValues ->
                            // Pass the launcher down to HomeScreen via AppNavGraph
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
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
