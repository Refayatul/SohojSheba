package com.bonfire.shohojsheba.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bonfire.shohojsheba.data.database.entities.Metadata

@Dao
interface MetadataDao {
    @Query("SELECT value FROM metadata WHERE key = :k")
    suspend fun get(k: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(m: Metadata)
}
