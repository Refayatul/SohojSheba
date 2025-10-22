package com.bonfire.shohojsheba.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bonfire.shohojsheba.data.database.entities.UserFavorite
import com.bonfire.shohojsheba.data.database.entities.UserHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDataDao {

    @Insert
    suspend fun addFavorite(userFavorite: UserFavorite): Long

    @Query("DELETE FROM user_favorites WHERE serviceId = :serviceId")
    suspend fun removeFavorite(serviceId: String): Int

    @Query("SELECT * FROM user_favorites ORDER BY addedDate DESC")
    fun getFavorites(): Flow<List<UserFavorite>>

    @Query("SELECT EXISTS(SELECT 1 FROM user_favorites WHERE serviceId = :serviceId)")
    fun isFavorite(serviceId: String): Flow<Boolean>

    @Insert
    suspend fun addHistory(userHistory: UserHistory): Long

    @Query("SELECT * FROM user_history ORDER BY accessedDate DESC LIMIT :limit")
    fun getRecentHistory(limit: Int): Flow<List<UserHistory>>

    @Query("DELETE FROM user_history WHERE accessedDate < :cutoffDate")
    suspend fun clearOldHistory(cutoffDate: Long): Int
}
