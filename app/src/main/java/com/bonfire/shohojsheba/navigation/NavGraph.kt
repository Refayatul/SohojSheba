package com.bonfire.shohojsheba.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bonfire.shohojsheba.R
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
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                navController = navController,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onVoiceSearchClick = onVoiceSearchClick
            )
        }
        composable("citizen_services") {
            ServiceListScreen(navController = navController, category = "citizen", title = R.string.category_citizen)
        }
        composable("farmer_services") {
            ServiceListScreen(navController = navController, category = "farmer", title = R.string.category_farmer)
        }
        composable("entrepreneur_services") {
            ServiceListScreen(navController = navController, category = "entrepreneur", title = R.string.category_entrepreneur)
        }
        composable("govt_office_services") {
            ServiceListScreen(navController = navController, category = "govt_office", title = R.string.category_govt_office)
        }
        composable("service_detail/{serviceId}") {
            ServiceDetailScreen(navController = navController, serviceId = it.arguments?.getString("serviceId"))
        }
        composable("history") {
            HistoryScreen(navController = navController)
        }
        composable("favorites") {
            FavoritesScreen(navController = navController)
        }
        composable("settings") {
            SettingsScreen(navController = navController)
        }
    }
}
