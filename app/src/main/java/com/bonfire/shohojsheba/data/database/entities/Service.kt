package com.bonfire.shohojsheba.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "services")
data class Service(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val iconName: String,   // e.g., "ic_citizen_apply_nid"
    val category: String,
    val versionAdded: Int,
    val lastUpdated: Long
)
