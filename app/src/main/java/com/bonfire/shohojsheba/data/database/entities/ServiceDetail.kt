package com.bonfire.shohojsheba.data.database.entities

data class ServiceDetail(
    val serviceId: String = "",
    var instructions: String = "",
    // We will store image URLs from Firebase Storage, not local drawables
    var imageUrls: List<String> = emptyList(),
    val youtubeLink: String? = null,
    var requiredDocuments: String = "",
    var processingTime: String = "",
    var contactInfo: String = "",
    val lastUpdated: Int = 1
)