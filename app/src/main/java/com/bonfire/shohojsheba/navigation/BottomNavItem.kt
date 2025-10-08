package com.bonfire.shohojsheba.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Home
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("হোম", "home", Icons.Outlined.Home),
    BottomNavItem("বিভাগ", "departments", Icons.Outlined.Apps),
    BottomNavItem("অফলাইন", "offline", Icons.Outlined.CloudOff),
    BottomNavItem("সংরক্ষিত", "saved", Icons.Outlined.BookmarkBorder)
)
