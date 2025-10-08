package com.bonfire.shohojsheba.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.bonfire.shohojsheba.R

data class BottomNavItem(
    @StringRes val title: Int,
    val route: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(R.string.nav_home, "home", Icons.Outlined.Home),
    BottomNavItem(R.string.nav_departments, "departments", Icons.Outlined.Apps),
    BottomNavItem(R.string.nav_offline, "offline", Icons.Outlined.CloudOff),
    BottomNavItem(R.string.nav_saved, "saved", Icons.Outlined.BookmarkBorder)
)
