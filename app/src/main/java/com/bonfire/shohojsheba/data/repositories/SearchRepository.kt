package com.bonfire.shohojsheba.data.repositories

import com.bonfire.shohojsheba.data.database.dao.ServiceDao
import com.bonfire.shohojsheba.data.database.entities.Service
import com.bonfire.shohojsheba.data.mappers.toEntity
import com.bonfire.shohojsheba.data.mappers.toRemote
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
                        serviceDao.insertServices(remote)
                    } else {
                        // Gemini Fallback - Trigger if no relevant results found
                        geminiRepository.generateService(query).collect { result ->
                            if (result != null) {
                                val (service, detail) = result
                                // Save to Firestore
                                FirestoreApi.saveService(service.toRemote())
                                FirestoreApi.saveServiceDetails(detail.toRemote())
                                
                                // Save to Local
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