package com.bonfire.shohojsheba.data.database.entities

data class Service(
    val id: String = "",
    val title: String = "",         // Changed from titleRes: Int
    val subtitle: String = "",      // Changed from subtitleRes: Int
    val iconName: String = "",      // Changed from iconRes: Int
    val category: String = "",
    val versionAdded: Int = 1
)