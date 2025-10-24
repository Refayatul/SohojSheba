package com.bonfire.shohojsheba.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.ui.screens.DepartmentsScreen
import com.bonfire.shohojsheba.ui.screens.FavoritesScreen
import com.bonfire.shohojsheba.ui.screens.HistoryScreen
import com.bonfire.shohojsheba.ui.screens.HomeScreen
import com.bonfire.shohojsheba.ui.screens.ServiceDetailScreen
import com.bonfire.shohojsheba.ui.screens.ServiceListScreen
import com.bonfire.shohojsheba.ui.screens.SettingsScreen

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
        composable(Routes.DEPARTMENTS) {
            DepartmentsScreen(navController = navController)
        }
        composable(Routes.CITIZEN_SERVICES) {
            ServiceListScreen(navController = navController, category = "citizen", title = R.string.category_citizen)
        }
        composable(Routes.FARMER_SERVICES) {
            ServiceListScreen(navController = navController, category = "farmer", title = R.string.category_farmer)
        }
        composable(Routes.ENTREPRENEUR_SERVICES) {
            ServiceListScreen(navController = navController, category = "entrepreneur", title = R.string.category_entrepreneur)
        }
        composable(Routes.GOVT_OFFICE_SERVICES) {
            ServiceListScreen(navController = navController, category = "govt_office", title = R.string.category_govt_office)
        }
        composable("${Routes.SERVICE_DETAIL}/{serviceId}") {
            ServiceDetailScreen(navController = navController, serviceId = it.arguments?.getString("serviceId"))
        }
        composable(Routes.HISTORY) {
            HistoryScreen(navController = navController)
        }
        composable(Routes.FAVORITES) {
            FavoritesScreen(navController = navController)
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(navController = navController)
        }
    }
}
