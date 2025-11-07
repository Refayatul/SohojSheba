package com.bonfire.shohojsheba.data.database.entities

data class UserHistory(
    // We can let Firestore auto-generate the ID for history items
    val serviceId: String = "",
    val accessedDate: Long = 0L
)