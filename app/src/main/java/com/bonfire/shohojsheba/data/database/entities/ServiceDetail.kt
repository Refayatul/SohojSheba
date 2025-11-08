package com.bonfire.shohojsheba.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "service_details",
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
    @PrimaryKey val serviceId: String,
    val instructions: String,
    val imageNames: String, // Stored as comma-separated names
    val images: String, // Stored as comma-separated URLs
    val requiredDocuments: String,
    val processingTime: String,
    val contactInfo: String
)
