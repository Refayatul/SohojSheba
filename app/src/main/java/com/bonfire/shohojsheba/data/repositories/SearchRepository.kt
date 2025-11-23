package com.bonfire.shohojsheba.data.repositories

import com.bonfire.shohojsheba.data.database.dao.ServiceDao
import com.bonfire.shohojsheba.data.database.entities.Service
import com.bonfire.shohojsheba.data.mappers.toEntity
import com.bonfire.shohojsheba.data.remote.FirestoreApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchRepository(
    private val serviceDao: ServiceDao,
    private val appScope: CoroutineScope,
    private val geminiRepository: GeminiRepository
) {
    fun searchServicesSmart(query: String): Flow<List<Service>> {
        val local = serviceDao.searchServices(query)
        return local.onEach { hits ->
            if (hits.isEmpty()) {
                appScope.launch(Dispatchers.IO) {
                    val remoteDtos = FirestoreApi.searchServicesRemote(query)
                    val remote = remoteDtos.map { it.toEntity() }
                    
                    // Check if any remote result is actually relevant
                    val isRelevant = remote.any { service ->
                        service.title.en.contains(query, ignoreCase = true) ||
                        service.title.bn.contains(query, ignoreCase = true) ||
                        service.searchKeywords.contains(query, ignoreCase = true)
                    }

                    if (isRelevant) {
                        // Save Firestore results to local DB for display
                        serviceDao.insertServices(remote)
                    } else {
                        // Gemini Fallback - Trigger if no relevant results found
                        geminiRepository.generateService(query).collect { result ->
                            if (result != null) {
                                val (service, detail) = result
                                // Save to Local DB only (for display)
                                // Will be saved to Firestore when user opens it
                                // Note: REPLACE strategy prevents true duplicates
                                serviceDao.insertServices(listOf(service))
                                serviceDao.insertServiceDetails(listOf(detail))
                            }
                        }
                    }
                }
            }
        }
    }
}