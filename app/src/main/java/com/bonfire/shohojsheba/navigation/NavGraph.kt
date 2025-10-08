package com.bonfire.sohojsheba.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bonfire.sohojsheba.ui.screens.*

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
