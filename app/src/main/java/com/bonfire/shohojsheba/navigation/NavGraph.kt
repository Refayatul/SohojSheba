package com.bonfire.shohojsheba.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.ui.screens.DepartmentsScreen
import com.bonfire.shohojsheba.ui.screens.FavoritesScreen
import com.bonfire.shohojsheba.ui.screens.HistoryScreen
import com.bonfire.shohojsheba.ui.screens.HomeScreen
import com.bonfire.shohojsheba.ui.screens.ServiceDetailScreen
import com.bonfire.shohojsheba.ui.screens.ServiceListScreen
import com.bonfire.shohojsheba.ui.screens.SettingsScreen
import com.bonfire.shohojsheba.ui.screens.SplashScreen

@Composable
fun AppNavGraph() {
    val navController: NavHostController = rememberNavController()
    var selectedItem by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val showBottomBar = bottomNavItems.any { it.route == currentDestination?.route }

            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 4.dp
                ) {
                    bottomNavItems.forEachIndexed { index, screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(stringResource(id = screen.title)) },
                            selected = selectedItem == index,
                            onClick = {
                                selectedItem = index
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.secondary,
                                unselectedTextColor = MaterialTheme.colorScheme.secondary,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController, 
            startDestination = "splash", 
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splash") { SplashScreen(navController) }
            composable("home") { HomeScreen(navController) }
            composable("departments") { DepartmentsScreen(navController) }
            composable("history") { HistoryScreen(navController) }
            composable("favorites") { FavoritesScreen(navController) }
            composable("settings") { SettingsScreen(navController) }
            composable("citizen_services") { 
                ServiceListScreen(
                    navController = navController, 
                    category = "citizen",
                    title = R.string.category_citizen
                ) 
            }
            composable("farmer_services") { 
                ServiceListScreen(
                    navController = navController, 
                    category = "farmer",
                    title = R.string.category_farmer
                ) 
            }
            composable("entrepreneur_services") { 
                ServiceListScreen(
                    navController = navController, 
                    category = "entrepreneur",
                    title = R.string.category_entrepreneur
                ) 
            }
            composable("govt_office_services") { 
                ServiceListScreen(
                    navController = navController, 
                    category = "govt_office",
                    title = R.string.category_govt_office
                ) 
            }
            composable(
                route = "service_detail/{serviceId}",
                arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
            ) { backStackEntry ->
                ServiceDetailScreen(
                    navController = navController,
                    serviceId = backStackEntry.arguments?.getString("serviceId")
                )
            }
        }
    }
}
