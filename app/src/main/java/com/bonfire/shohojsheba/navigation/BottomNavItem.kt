package com.bonfire.shohojsheba.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.bonfire.shohojsheba.R

data class BottomNavItem(
    @StringRes val title: Int,
    val route: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(R.string.nav_home, Routes.HOME, Icons.Outlined.Home),
    BottomNavItem(R.string.nav_departments, Routes.DEPARTMENTS, Icons.Outlined.Apps),
    BottomNavItem(R.string.nav_history, Routes.HISTORY, Icons.Outlined.History),
    BottomNavItem(R.string.nav_favorites, Routes.FAVORITES, Icons.Outlined.BookmarkBorder)
)
