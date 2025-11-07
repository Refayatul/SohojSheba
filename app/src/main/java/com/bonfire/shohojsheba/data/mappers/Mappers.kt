package com.bonfire.shohojsheba.data.mappers

import com.bonfire.shohojsheba.data.database.entities.Service
import com.bonfire.shohojsheba.data.database.entities.ServiceDetail
import com.bonfire.shohojsheba.data.remote.ServiceDTO
import com.bonfire.shohojsheba.data.remote.ServiceDetailDTO

fun ServiceDTO.toEntity() = Service(
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

fun ServiceDetailDTO.toEntity() = ServiceDetail(
    serviceId = serviceId,
    instructions = instructions,
    imageNames = imageNames.joinToString(","),
    images = images.joinToString(","),
    youtubeLink = youtubeLink,
    requiredDocuments = requiredDocuments,
    processingTime = processingTime,
    contactInfo = contactInfo,
    lastUpdated = lastUpdated
)
