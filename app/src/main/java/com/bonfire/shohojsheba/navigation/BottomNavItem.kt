package com.bonfire.shohojsheba.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("Home", "home", Icons.Default.Home),
    BottomNavItem("Service Guide", "service_guide", Icons.Outlined.MenuBook),
    BottomNavItem("Offline", "offline", Icons.Outlined.CloudOff),
    BottomNavItem("Settings", "settings", Icons.Default.Settings)
)
