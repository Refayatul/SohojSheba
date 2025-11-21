package com.bonfire.shohojsheba.navigation

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.ui.screens.*
import com.bonfire.shohojsheba.ui.viewmodels.AuthViewModel
import com.bonfire.shohojsheba.ui.viewmodels.ViewModelFactory
import androidx.compose.ui.platform.LocalContext

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onVoiceSearchClick: () -> Unit,
    currentThemeMode: String,
    onThemeChange: (String) -> Unit,
    googleSignInLauncher: ActivityResultLauncher<Intent>? = null,
    authViewModel: AuthViewModel // Added parameter
) {
    // Removed local AuthViewModel creation. Using passed instance.
    val currentUser by authViewModel.currentUser.collectAsState()

    // Determine startDestination based on auth state - this will update when user logs in/out
    // Using remember to ensure stable destination reference while allowing updates
    val startDestination = remember(currentUser) {
        if (currentUser != null) Routes.HOME else Routes.LOGIN
    }

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                navController = navController, 
                googleSignInLauncher = googleSignInLauncher,
                authViewModel = authViewModel
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                navController = navController, 
                googleSignInLauncher = googleSignInLauncher,
                authViewModel = authViewModel
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                navController = navController,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onVoiceSearchClick = onVoiceSearchClick
            )
        }

        composable(Routes.DEPARTMENTS) { DepartmentsScreen(navController = navController) }
        composable(Routes.CITIZEN_SERVICES) {
            ServiceListScreen(navController, "citizen", R.string.category_citizen)
        }
        composable(Routes.FARMER_SERVICES) {
            ServiceListScreen(navController, "farmer", R.string.category_farmer)
        }
        composable(Routes.ENTREPRENEUR_SERVICES) {
            ServiceListScreen(navController, "entrepreneur", R.string.category_entrepreneur)
        }
        composable(Routes.GOVT_OFFICE_SERVICES) {
            ServiceListScreen(navController, "govt_office", R.string.category_govt_office)
        }

        composable("${Routes.SERVICE_DETAIL}/{serviceId}") { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getString("serviceId")
            if (!serviceId.isNullOrBlank()) {
                ServiceDetailScreen(
                    navController = navController,
                    serviceId = serviceId
                )
            }
        }
        composable(Routes.CHAT) {
            ChatScreen(navController = navController)
        }

        composable(Routes.HISTORY) { HistoryScreen(navController) }
        composable(Routes.FAVORITES) { FavoritesScreen(navController) }

        // --- Updated Settings Route to pass theme data ---
        composable(Routes.SETTINGS) {
            SettingsScreen(
                navController = navController,
                currentThemeMode = currentThemeMode,
                onThemeChange = onThemeChange
            )
        }
    }
}
