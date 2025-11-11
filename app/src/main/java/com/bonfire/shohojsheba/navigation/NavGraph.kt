package com.bonfire.shohojsheba.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.ui.screens.*

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onVoiceSearchClick: () -> Unit
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Routes.HOME
    ) {
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
        composable(Routes.SETTINGS) { SettingsScreen(navController) }
    }
}
