package com.bonfire.shohojsheba.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.bonfire.shohojsheba.data.remote.LocalizedString

@Entity(
    tableName = "service_details",
    // ForeignKey: Ensures data integrity.
    // If a Service is deleted, its corresponding ServiceDetail is also deleted (CASCADE).
    foreignKeys = [
        ForeignKey(
            entity = Service::class,
            parentColumns = ["id"],
            childColumns = ["serviceId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ServiceDetail(
    @PrimaryKey val serviceId: String, // Same ID as the Service
    
    // Detailed content fields (Localized)
    val instructions: LocalizedString,
    val requiredDocuments: LocalizedString,
    val processingTime: LocalizedString,
    val contactInfo: LocalizedString,
    
    val youtubeLink: String?, // Optional video guide
    val lastUpdated: Long,
    
    val imageNames: String, // Stored as comma-separated names
    val images: String // Stored as comma-separated URLs
)
