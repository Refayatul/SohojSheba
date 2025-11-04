package com.bonfire.shohojsheba.data.repositories

import com.bonfire.shohojsheba.data.database.dao.ServiceDao
import com.bonfire.shohojsheba.data.database.dao.UserDataDao
import com.bonfire.shohojsheba.data.database.entities.Service
import com.bonfire.shohojsheba.data.database.entities.ServiceDetail
import com.bonfire.shohojsheba.data.database.entities.UserFavorite
import com.bonfire.shohojsheba.data.database.entities.UserHistory
import kotlinx.coroutines.flow.Flow

class Repository(private val serviceDao: ServiceDao, private val userDataDao: UserDataDao) {

    // Service methods
    fun getAllServices(): Flow<List<Service>> = serviceDao.getAllServices()

    fun getServicesByCategory(category: String): Flow<List<Service>> = serviceDao.getServicesByCategory(category)

    fun searchServices(query: String): Flow<List<Service>> = serviceDao.searchServices(query)

    fun getServiceById(serviceId: String): Flow<Service?> = serviceDao.getServiceById(serviceId)

    fun getServiceDetail(serviceId: String): Flow<ServiceDetail?> = serviceDao.getServiceDetail(serviceId)

    // User data methods
    suspend fun addFavorite(serviceId: String): Boolean {
        return try {
            userDataDao.addFavorite(UserFavorite(serviceId = serviceId, addedDate = System.currentTimeMillis()))
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun removeFavorite(serviceId: String): Boolean {
        return try {
            userDataDao.removeFavorite(serviceId)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getFavorites(): Flow<List<UserFavorite>> = userDataDao.getFavorites()

    fun isFavorite(serviceId: String): Flow<Boolean> = userDataDao.isFavorite(serviceId)

    suspend fun addHistory(serviceId: String): Boolean {
        return try {
            userDataDao.addHistory(UserHistory(serviceId = serviceId, accessedDate = System.currentTimeMillis()))
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getRecentHistory(limit: Int = 20): Flow<List<UserHistory>> = userDataDao.getRecentHistory(limit)

    suspend fun clearOldHistory(cutoffDate: Long): Boolean {
        return try {
            userDataDao.clearOldHistory(cutoffDate)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun getServicesByIds(serviceIds: List<String>): Flow<List<Service>> = serviceDao.getServicesByIds(serviceIds)
}
