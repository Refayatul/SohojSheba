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
    var instructions: String,
    var imageRes: String, // Changed to String to store a comma-separated list of drawable names
    val youtubeLink: String?,
    var requiredDocuments: String,
    var processingTime: String,
    var contactInfo: String,
    val lastUpdated: Int
)
