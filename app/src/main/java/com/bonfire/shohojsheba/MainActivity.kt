package com.bonfire.shohojsheba

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bonfire.shohojsheba.data.repositories.RepositoryProvider
import com.bonfire.shohojsheba.navigation.AppNavGraph
import com.bonfire.shohojsheba.navigation.BottomNavBar
import com.bonfire.shohojsheba.navigation.Routes
import com.bonfire.shohojsheba.ui.theme.ShohojShebaTheme
import com.bonfire.shohojsheba.ui.viewmodels.AuthViewModel
import com.bonfire.shohojsheba.ui.viewmodels.AuthUiState
import com.bonfire.shohojsheba.ui.viewmodels.ViewModelFactory
import com.bonfire.shohojsheba.utils.AppLocaleManager
import com.bonfire.shohojsheba.utils.LocalLocale
import com.bonfire.shohojsheba.utils.LocalOnLocaleChange
import com.bonfire.shohojsheba.utils.ProvideLocale
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

import android.content.res.Configuration

/**
 * =========================================================================================
 *                                    MAIN ACTIVITY
 * =========================================================================================
 * 
 * HOW IT WORKS:
 * 1.  **App Entry Point**:
 *     -   This is the root Activity where the app starts.
 *     -   It sets up the Jetpack Compose environment (`setContent`).
 * 
 * 2.  **Global Configuration**:
 *     -   **Locale Management**: Uses `AppLocaleManager` to handle language switching (English/Bangla).
 *         It forces an activity recreation when the language changes to apply resources correctly.
 *     -   **Theme Management**: Reads the saved theme preference (Light/Dark/System) from `SharedPreferences`
 *         and applies it via `ShohojShebaTheme`.
 * 
 * 3.  **Authentication Setup**:
 *     -   Configures the `GoogleSignInClient` for OAuth 2.0.
 *     -   Initializes `AuthViewModel` to manage login/registration state globally.
 *     -   Observes `authState` to automatically navigate to the Home screen upon successful login.
 * 
 * 4.  **Navigation Structure**:
 *     -   Sets up the main `Scaffold` which holds the `TopAppBar`, `BottomNavBar`, and the content area.
 *     -   Uses `AppNavGraph` to handle screen transitions.
 *     -   Conditionally shows/hides UI elements (like the Bottom Bar) based on the current route (e.g., hidden on Login screen).
 * 
 * 5.  **Feature Integration**:
 *     -   **Voice Search**: Initializes the `ActivityResultLauncher` for speech recognition and passes it to the Home screen.
 * =========================================================================================
 */

// Material3 API is experimental, so we need to opt-in to use it
@OptIn(ExperimentalMaterial3Api::class)
// MainActivity is the entry point of our app - it's created when the app launches
class MainActivity : androidx.appcompat.app.AppCompatActivity() {

    // onCreate is called when the activity is first created
    // savedInstanceState contains data from a previous instance if the app was killed by the system
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 'setContent' is the doorway to Jetpack Compose.
        // Everything inside here is UI code written in Kotlin, not XML.
        setContent {
            // We use SharedPreferences to save simple settings like Theme and Language.
            val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            
            // 'remember' keeps this object alive across recompositions (screen updates).
            val appLocaleManager = remember { AppLocaleManager(applicationContext) }

            // ------------------------------------------------------------
            // 1. LANGUAGE LOGIC - Using AppLocaleManager
            // ------------------------------------------------------------
            val currentLang = appLocaleManager.getCurrentLanguageCode()
            Log.d("MainActivity", "=== LOCALE SETUP ===")
            Log.d("MainActivity", "Current language code: $currentLang")
            
            // Derive locale directly from currentLang - no remember/mutableStateOf!
            // This ensures locale updates when currentLang changes after activity recreation
            val locale = if (currentLang == "bn") Locale("bn", "BD") else Locale("en", "US")
            
            Log.d("MainActivity", "Locale object: ${locale.language} (${locale.displayLanguage})")
            
            
            // Track current route for restoration after locale change
            var currentRouteBeforeLocaleChange by remember { mutableStateOf<String?>(null) }
            
            val onLocaleChange: (Locale, String?) -> Unit = { newLocale, currentRoute ->
                Log.d("MainActivity", "=== LOCALE CHANGE REQUESTED ===")
                Log.d("MainActivity", "New locale: ${newLocale.language} (${newLocale.displayLanguage})")
                Log.d("MainActivity", "Current route: $currentRoute")
                
                // Save current route to SharedPreferences so we can restore it after recreation
                sharedPreferences.edit().putString("route_before_locale_change", currentRoute).apply()
                
                // AppCompatDelegate.setApplicationLocales() will recreate the activity
                // and the new locale will be read when onCreate runs again
                appLocaleManager.changeLanguage(newLocale.language)
                Log.d("MainActivity", "Activity will now recreate...")
            }

            // Check if we're returning from a locale change and restore the route
            val savedRoute = remember { 
                val route = sharedPreferences.getString("route_before_locale_change", null)
                // Clear the saved route so it doesn't affect future navigations
                if (route != null) {
                    sharedPreferences.edit().remove("route_before_locale_change").apply()
                }
                route
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
            // State for search query - 'by remember' makes it persist across recompositions
            // 'mutableStateOf' means this value can change and trigger UI updates
            var searchQuery by remember { mutableStateOf("") }
            // Get the Android context (needed for accessing system resources, services, etc.)
            val context = LocalContext.current
            // Navigation controller - manages screen transitions and back stack
            val navController = rememberNavController()

            // --- Google Sign-In Configuration ---
            // This sets up Google Sign-In with our OAuth 2.0 client ID
            // The ID token is used to authenticate with Firebase
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("187859520695-03dpjg2k339oi3if24ts12ioip830a79.apps.googleusercontent.com") // Our Google Cloud Project ID
                .requestEmail() // We need email for user identification
                .build()
            // Create the Google Sign-In client with our configuration
            val googleSignInClient = GoogleSignIn.getClient(context, gso)

            // --- ViewModel Setup ---
            // AuthViewModel handles all authentication logic (login, register, Google Sign-In)
            // We create it at the Activity level so it persists across navigation
            val authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory(context))

            // --- Observing Authentication State ---
            // These 'collectAsState()' calls convert Kotlin Flows to Compose State
            // When the Flow emits a new value, the UI automatically recomposes
            val authState by authViewModel.authState.collectAsState() // Email/password auth state
            val googleSignInState by authViewModel.googleSignInState.collectAsState() // Google Sign-In state
            val isAuthCheckComplete by authViewModel.isAuthCheckComplete.collectAsState() // Has initial auth check finished?



            // --- Auto-Navigation After Successful Login ---
            // Navigate when authentication succeeds
            // 'LaunchedEffect' runs a side-effect (navigation) when 'authState' changes.
            LaunchedEffect(authState, googleSignInState) {
                val isAuthSuccess = authState is AuthUiState.Success || googleSignInState is AuthUiState.Success
                
                if (isAuthSuccess) {
                    // Navigate to Home and clear the back stack so user can't go back to Login.
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) // Pop entire back stack
                    }
                    authViewModel.clearAuthState()
                    
                    // Sync user data from Firestore
                    val repository = RepositoryProvider.getRepository(context)
                    repository.syncUserDataFromFirestore()
                }
            }

            // --- Google Sign-In Result Handler ---
            // This launcher starts the Google Sign-In flow and handles the result
            // Created at MainActivity level to avoid ActivityResultRegistryOwner issues
            val googleSignInLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult() // Contract for starting an activity and getting result
            ) { result ->
                Log.d("GoogleSignIn", "Result code: ${result.resultCode}")
                Log.d("GoogleSignIn", "Result data: ${result.data}")
                
                try {
                    // Extract the Google account from the result intent
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    
                    // Check if task was successful
                    if (task.isSuccessful) {
                        val account = task.result
                        Log.d("GoogleSignIn", "Account: ${account?.email}")
                        
                        if (account != null) {
                            // Get the ID token (JWT) that Firebase needs for authentication
                            val idToken = account.idToken
                            Log.d("GoogleSignIn", "ID Token present: ${idToken != null}")
                            
                            if (idToken != null) {
                                // Send the ID token to our backend (Firebase) to create a session
                                authViewModel.googleSignIn(idToken)
                            } else {
                                // No ID token means authentication failed
                                Log.e("GoogleSignIn", "ID token is null")
                                Toast.makeText(context, context.getString(R.string.google_signin_failed), Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // User cancelled or sign-in failed
                            Log.e("GoogleSignIn", "Account is null")
                            Toast.makeText(context, context.getString(R.string.google_signin_failed), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Task failed - log the exception
                        val exception = task.exception
                        Log.e("GoogleSignIn", "Sign-in failed", exception)
                        Toast.makeText(context, "Sign-in failed: ${exception?.message}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    // Handle any unexpected errors
                    Log.e("GoogleSignIn", "Unexpected error", e)
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            // --- Voice Input Setup ---
            // Flag to track if the current search query came from voice input
            // This helps us provide appropriate feedback to the user
            var isVoiceInput by remember { mutableStateOf(false) }

            // Launcher for voice recognition (speech-to-text)
            val voiceLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                val data: Intent? = result.data
                // Extract the recognized text from the speech recognizer
                val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                results?.firstOrNull()?.let {
                    searchQuery = it // Update search bar with spoken text
                    isVoiceInput = true // Mark this as voice input so we can trigger AI search automatically
                }
            }

            // Pass the calculated 'useDarkTheme' here
            // 'ShohojShebaTheme' wraps the whole app to apply colors and fonts.
            ShohojShebaTheme(darkTheme = useDarkTheme) {
                ProvideLocale(locale = locale) {
                    CompositionLocalProvider(
                        LocalOnLocaleChange provides onLocaleChange
                    ) {
                        val localizedContext = LocalContext.current
                        // Listen for toast messages from AuthViewModel using localized context
                        LaunchedEffect(Unit) {
                            authViewModel.toastMessage.collectLatest { message ->
                                Toast.makeText(localizedContext, message.asString(localizedContext), Toast.LENGTH_SHORT).show()
                            }
                        }
                        // --- Initial Auth Check ---
                        // While checking if user is already logged in, show a loading indicator
                        // This prevents flickering between login and home screens
                        if (!isAuthCheckComplete) {
                            // Show Splash Screen / Loading Indicator
                            Box(
                                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            // --- Navigation State ---
                            // Track which screen we're currently on
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = navBackStackEntry?.destination?.route

                            // Check if user is logged in
                            val currentUser by authViewModel.currentUser.collectAsState()

                            // --- Conditional UI Elements ---
                            // We show/hide bottom navigation and top bar based on which screen is active
                            // Hide bottom nav and top bar on auth screens (login, register), settings, and service detail screens
                            val isAuthScreen = currentRoute == Routes.LOGIN || currentRoute == Routes.REGISTER || currentRoute == Routes.FORGOT_PASSWORD
                            val isSettingsScreen = currentRoute == Routes.SETTINGS
                            val isServiceDetailScreen = currentRoute?.startsWith(Routes.SERVICE_DETAIL) == true
                            val isHomeScreen = currentRoute == Routes.HOME

                            // Only show bottom nav when logged in and not on special screens
                            val showBottomNav = !isAuthScreen && !isSettingsScreen && !isServiceDetailScreen && currentUser != null
                            // Only show top bar on home screen
                            val showTopBar = isHomeScreen

                            Scaffold(
                                topBar = {
                                    if (showTopBar) {
                                        CenterAlignedTopAppBar(
                                            title = {
                                                Text(
                                                    text = stringResource(id = R.string.app_name),
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            },
                                            actions = {
                                                IconButton(onClick = { navController.navigate(Routes.SETTINGS) }) {
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
                                    isVoiceInput = isVoiceInput,
                                    onVoiceInputReset = { isVoiceInput = false },
                                    onVoiceSearchClick = {
                                        try {
                                            // Use current app locale for voice search
                                            val voiceLocale = if (locale.language == "bn") "bn-BD" else "en-US"
                                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                putExtra(
                                                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                                )
                                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, voiceLocale)
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
                                    onThemeChange = onThemeChange,
                                    locale = locale,  // Pass locale to navigation graph
                                    googleSignInLauncher = googleSignInLauncher,
                                    authViewModel = authViewModel // Pass shared AuthViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
