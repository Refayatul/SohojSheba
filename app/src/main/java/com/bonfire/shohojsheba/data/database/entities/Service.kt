package com.bonfire.shohojsheba.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bonfire.shohojsheba.data.remote.LocalizedString

// @Entity: Defines this class as a table in the Room database.
// 'tableName' specifies the name of the table in SQLite.
@Entity(tableName = "services")
data class Service(
    // @PrimaryKey: Uniquely identifies each service.
    @PrimaryKey val id: String,
    
    // LocalizedString: Helper class to store both English and Bangla text.
    val title: LocalizedString,
    val subtitle: LocalizedString,
    
    val iconName: String, // Name of the icon resource (mapped in UI)
    val category: String, // e.g., "citizen", "farmer"
    
    val versionAdded: Int, // For database migration/updates
    val lastUpdated: Long, // Timestamp for cache invalidation
    
    val images: String, // Stored as comma-separated URLs (Room doesn't support Lists directly)
    val imageNames: String, // Stored as comma-separated names
    val searchKeywords: String // Stored as comma-separated keywords for search indexing
)
