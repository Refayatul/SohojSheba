package com.bonfire.shohojsheba.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "services")
data class Service(
    @PrimaryKey val id: String,
    val titleRes: Int,
    val subtitleRes: Int,
    val iconRes: Int,
    val category: String,
    val versionAdded: Int
)
