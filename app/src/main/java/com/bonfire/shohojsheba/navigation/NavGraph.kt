package com.bonfire.shohojsheba.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bonfire.shohojsheba.ui.screens.HomeScreen
import com.bonfire.shohojsheba.ui.screens.OfflineContentScreen
import com.bonfire.shohojsheba.ui.screens.ServiceGuideScreen
import com.bonfire.shohojsheba.ui.screens.SettingsScreen
import com.bonfire.shohojsheba.ui.screens.SplashScreen

@Composable
fun AppNavGraph() {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("service_guide") { ServiceGuideScreen(navController) }
        composable("offline") { OfflineContentScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
    }
}
