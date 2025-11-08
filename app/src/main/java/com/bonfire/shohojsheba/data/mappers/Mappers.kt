package com.bonfire.shohojsheba.data.mappers

import com.bonfire.shohojsheba.data.database.entities.Service
import com.bonfire.shohojsheba.data.database.entities.ServiceDetail
import com.bonfire.shohojsheba.data.remote.ServiceDetails
import com.bonfire.shohojsheba.data.remote.ServiceSummary

fun ServiceSummary.toEntity() = Service(
    id = id,
    title = title,
    subtitle = subtitle,
    iconName = iconName,
    category = category,
    versionAdded = versionAdded,
    lastUpdated = lastUpdated,
    images = images.joinToString(","),
    imageNames = imageNames.joinToString(","),
    searchKeywords = searchKeywords.joinToString(",")
)

fun ServiceDetails.toEntity() = ServiceDetail(
    serviceId = serviceId,
    instructions = instructions,
    requiredDocuments = requiredDocuments,
    processingTime = processingTime,
    contactInfo = contactInfo,
    youtubeLink = youtubeLink,
    lastUpdated = lastUpdated,
    imageNames = imageNames.joinToString(","),
    images = images.joinToString(",")
)
