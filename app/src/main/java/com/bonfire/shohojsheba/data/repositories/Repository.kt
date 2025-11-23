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

    // Favorites/History (with Firestore sync)
    fun getFavorites(): Flow<List<UserFavorite>> = userDataDao.getFavorites()
    fun isFavorite(serviceId: String): Flow<Boolean> = userDataDao.isFavorite(serviceId)
    suspend fun addFavorite(f: UserFavorite) {
        userDataDao.addFavorite(f)
        // Sync to Firestore
        try {
            FirestoreApi.syncFavoriteToFirestore(f.serviceId)
        } catch (e: Exception) {
            Log.e("Repository", "Failed to sync favorite to Firestore: ${e.message}")
        }
    }
    suspend fun removeFavorite(serviceId: String) {
        userDataDao.removeFavorite(serviceId)
        // Sync to Firestore
        try {
            FirestoreApi.removeFavoriteFromFirestore(serviceId)
        } catch (e: Exception) {
            Log.e("Repository", "Failed to remove favorite from Firestore: ${e.message}")
        }
    }

    fun getRecentHistory(limit: Int = 20): Flow<List<UserHistory>> = userDataDao.getRecentHistory(limit)
    suspend fun addHistory(h: UserHistory) {
        userDataDao.addHistory(h)
        // Sync to Firestore
        try {
            FirestoreApi.syncHistoryToFirestore(h.serviceId)
        } catch (e: Exception) {
            Log.e("Repository", "Failed to sync history to Firestore: ${e.message}")
        }
    }
    suspend fun clearOldHistory(cutoffDate: Long) = userDataDao.clearOldHistory(cutoffDate)
    
    // Sync user data from Firestore on app start
    suspend fun syncUserDataFromFirestore() {
        Log.d("Repository", "syncUserDataFromFirestore: Starting sync...")
        
        if (!context.isNetworkAvailable()) {
            Log.w("Repository", "syncUserDataFromFirestore: No network available")
            return
        }
        
        try {
            // Fetch favorites from Firestore
            val remoteFavorites = FirestoreApi.getUserFavoritesFromFirestore()
            Log.d("Repository", "syncUserDataFromFirestore: Found ${remoteFavorites.size} favorites in Firestore")
            
            remoteFavorites.forEach { serviceId ->
                // FIRST: Fetch the service data from Firestore if not in local DB
                val localService = serviceDao.getServiceByIdOnce(serviceId)
                if (localService == null) {
                    try {
                        Log.d("Repository", "syncUserDataFromFirestore: Fetching service data for $serviceId")
                        // Fetch from Firestore
                        val remoteServices = FirestoreApi.allServices()
                        val service = remoteServices.find { it.id == serviceId }
                        if (service != null) {
                            serviceDao.insertServices(listOf(service.toEntity()))
                            Log.d("Repository", "syncUserDataFromFirestore: Saved service $serviceId")
                        } else {
                            Log.w("Repository", "syncUserDataFromFirestore: Service $serviceId not found in Firestore")
                            return@forEach // Skip this favorite if service doesn't exist
                        }
                        
                        // Also fetch service details
                        val serviceDetail = FirestoreApi.detailById(serviceId)
                        if (serviceDetail != null) {
                            serviceDao.insertServiceDetails(listOf(serviceDetail.toEntity()))
                            Log.d("Repository", "syncUserDataFromFirestore: Saved details for $serviceId")
                        }
                    } catch (e: Exception) {
                        Log.e("Repository", "Failed to fetch service $serviceId: ${e.message}", e)
                        return@forEach // Skip this favorite if fetch failed
                    }
                }
                
                // THEN: Add to favorites (after service exists in DB)
                if (!userDataDao.isFavoriteOnce(serviceId)) {
                    userDataDao.addFavorite(UserFavorite(serviceId = serviceId, addedDate = System.currentTimeMillis()))
                    Log.d("Repository", "syncUserDataFromFirestore: Added favorite $serviceId to local DB")
                }
            }
            
            // Fetch history from Firestore
            val remoteHistory = FirestoreApi.getUserHistoryFromFirestore()
            Log.d("Repository", "syncUserDataFromFirestore: Found ${remoteHistory.size} history items in Firestore")
            
            remoteHistory.forEach { serviceId ->
                // FIRST: Fetch the service data from Firestore if not in local DB
                val localService = serviceDao.getServiceByIdOnce(serviceId)
                if (localService == null) {
                    try {
                        Log.d("Repository", "syncUserDataFromFirestore: Fetching service data for $serviceId")
                        // Fetch from Firestore
                        val remoteServices = FirestoreApi.allServices()
                        val service = remoteServices.find { it.id == serviceId }
                        if (service != null) {
                            serviceDao.insertServices(listOf(service.toEntity()))
                            Log.d("Repository", "syncUserDataFromFirestore: Saved service $serviceId")
                        } else {
                            Log.w("Repository", "syncUserDataFromFirestore: Service $serviceId not found in Firestore")
                            return@forEach // Skip this history item if service doesn't exist
                        }
                        
                        // Also fetch service details
                        val serviceDetail = FirestoreApi.detailById(serviceId)
                        if (serviceDetail != null) {
                            serviceDao.insertServiceDetails(listOf(serviceDetail.toEntity()))
                            Log.d("Repository", "syncUserDataFromFirestore: Saved details for $serviceId")
                        }
                    } catch (e: Exception) {
                        Log.e("Repository", "Failed to fetch service $serviceId: ${e.message}", e)
                        return@forEach // Skip this history item if fetch failed
                    }
                }
                
                // THEN: Add to history (after service exists in DB)
                if (!userDataDao.isInHistoryOnce(serviceId)) {
                    userDataDao.addHistory(UserHistory(serviceId = serviceId, accessedDate = System.currentTimeMillis()))
                    Log.d("Repository", "syncUserDataFromFirestore: Added history $serviceId to local DB")
                }
            }
            
            Log.d("Repository", "Successfully synced user data from Firestore")
        } catch (e: Exception) {
            Log.e("Repository", "Failed to sync user data from Firestore: ${e.message}", e)
        }
    }
}
