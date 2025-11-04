package com.bonfire.shohojsheba.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_favorites",
    foreignKeys = [
        ForeignKey(
            entity = Service::class,
            parentColumns = ["id"],
            childColumns = ["serviceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["serviceId"])]
)
data class UserFavorite(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serviceId: String,
    val addedDate: Long
)
