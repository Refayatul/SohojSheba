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

    @Query("DELETE FROM user_favorites")
    suspend fun clearAllFavorites()

    @Query("SELECT * FROM user_favorites ORDER BY addedDate DESC")
    fun getFavorites(): Flow<List<UserFavorite>>

    @Query("SELECT EXISTS(SELECT 1 FROM user_favorites WHERE serviceId = :serviceId)")
    fun isFavorite(serviceId: String): Flow<Boolean>
    
    @Query("SELECT EXISTS(SELECT 1 FROM user_favorites WHERE serviceId = :serviceId)")
    suspend fun isFavoriteOnce(serviceId: String): Boolean

    @Insert
    suspend fun addHistory(userHistory: UserHistory): Long

    @Query("SELECT * FROM user_history ORDER BY accessedDate DESC LIMIT :limit")
    fun getRecentHistory(limit: Int): Flow<List<UserHistory>>
    
    @Query("SELECT EXISTS(SELECT 1 FROM user_history WHERE serviceId = :serviceId)")
    suspend fun isInHistoryOnce(serviceId: String): Boolean

    @Query("DELETE FROM user_history WHERE accessedDate < :cutoffDate")
    suspend fun clearOldHistory(cutoffDate: Long): Int

    @Query("DELETE FROM user_history")
    suspend fun clearAllHistory()
}
