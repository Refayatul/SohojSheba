package com.bonfire.shohojsheba.data.repositories

import android.content.Context
import android.util.Log
import com.bonfire.shohojsheba.data.database.dao.MetadataDao
import com.bonfire.shohojsheba.data.database.dao.ServiceDao
import com.bonfire.shohojsheba.data.database.dao.UserDataDao
import com.bonfire.shohojsheba.data.database.entities.*
import com.bonfire.shohojsheba.data.mappers.toEntity
import com.bonfire.shohojsheba.data.remote.FirestoreApi
import com.bonfire.shohojsheba.util.isNetworkAvailable
import kotlinx.coroutines.flow.Flow

class Repository(
    private val context: Context,
    val serviceDao: ServiceDao,
    private val userDataDao: UserDataDao,
    private val metadataDao: MetadataDao
) {
    fun getAllServices(): Flow<List<Service>> = serviceDao.getAllServices()
    fun getServicesByCategory(category: String): Flow<List<Service>> = serviceDao.getServicesByCategory(category)
    fun getServiceById(id: String): Flow<Service?> = serviceDao.getServiceById(id)
    fun searchServices(q: String): Flow<List<Service>> = serviceDao.searchServices(q)
    fun getServicesByIds(ids: List<String>): Flow<List<Service>> = serviceDao.getServicesByIds(ids)
    fun getServiceDetail(id: String): Flow<ServiceDetail?> = serviceDao.getServiceDetail(id)

    suspend fun hasSyncedOnce(): Boolean = metadataDao.get("has_synced_once") == "true"

    suspend fun ensureDetail(serviceId: String) {
        if (!context.isNetworkAvailable()) {
            return // Do nothing if offline
        }
        try {
            if (serviceDao.getServiceDetailOnce(serviceId) == null) {
                FirestoreApi.detailById(serviceId)?.toEntity()?.let { serviceDao.insertServiceDetails(listOf(it)) }
            }
        } catch (e: Exception) {
            Log.e("Repository", "Failed to ensure service detail: ${e.message}")
        }
    }

    suspend fun refreshIfNeeded(): String {
        if (!context.isNetworkAvailable()) {
            return "Offline"
        }
        return try {
            val localCount = serviceDao.getServiceCount()
            val remoteVer = FirestoreApi.catalogVersion()
            val localVer = metadataDao.get("catalog_version")?.toIntOrNull()

            if (localCount == 0 || localVer == null || localVer != remoteVer) {
                val services = FirestoreApi.allServices().map { it.toEntity() }
                val details = FirestoreApi.allDetails().map { it.toEntity() }
                serviceDao.insertServices(services)
                serviceDao.insertServiceDetails(details)
                metadataDao.put(Metadata("catalog_version", remoteVer.toString()))
                metadataDao.put(Metadata("last_sync_epoch", System.currentTimeMillis().toString()))
                metadataDao.put(Metadata("has_synced_once", "true")) // Set the flag
                "Firebase Sync"
            } else {
                "Cache"
            }
        } catch (e: Exception) {
            Log.e("Repository", "Failed to refresh catalog: ${e.message}")
            "Offline"
        }
    }

    // Favorites/History (unchanged)
    fun getFavorites(): Flow<List<UserFavorite>> = userDataDao.getFavorites()
    fun isFavorite(serviceId: String): Flow<Boolean> = userDataDao.isFavorite(serviceId)
    suspend fun addFavorite(f: UserFavorite) = userDataDao.addFavorite(f)
    suspend fun removeFavorite(serviceId: String) = userDataDao.removeFavorite(serviceId)

    fun getRecentHistory(limit: Int = 20): Flow<List<UserHistory>> = userDataDao.getRecentHistory(limit)
    suspend fun addHistory(h: UserHistory) = userDataDao.addHistory(h)
    suspend fun clearOldHistory(cutoffDate: Long) = userDataDao.clearOldHistory(cutoffDate)
}
