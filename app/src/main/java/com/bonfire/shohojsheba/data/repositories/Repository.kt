package com.bonfire.shohojsheba.data.repositories

import com.bonfire.shohojsheba.data.database.dao.MetadataDao
import com.bonfire.shohojsheba.data.database.dao.ServiceDao
import com.bonfire.shohojsheba.data.database.dao.UserDataDao
import com.bonfire.shohojsheba.data.database.entities.*
import com.bonfire.shohojsheba.data.mappers.toEntity
import com.bonfire.shohojsheba.data.remote.FirestoreApi
import kotlinx.coroutines.flow.Flow

class Repository(
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

    suspend fun ensureDetail(serviceId: String) {
        if (serviceDao.getServiceDetailOnce(serviceId) == null) {
            FirestoreApi.detailById(serviceId)?.toEntity()?.let { serviceDao.insertServiceDetails(listOf(it)) }
        }
    }

    suspend fun refreshIfNeeded() {
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