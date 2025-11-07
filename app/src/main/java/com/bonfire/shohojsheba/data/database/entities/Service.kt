package com.bonfire.shohojsheba.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "services")
data class Service(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val iconName: String,
    val category: String,
    val versionAdded: Int,
    val lastUpdated: Long,
    val images: String, // Stored as comma-separated URLs
    val imageNames: String, // Stored as comma-separated names
    val searchKeywords: String // Stored as comma-separated keywords
)
