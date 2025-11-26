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
    /**
     * The "Smart Search" function.
     * It follows a 3-step strategy to ensure the user always finds something:
     * 1. Local DB: Fast, offline-ready.
     * 2. Remote (Firestore): If local is empty, check the cloud.
     * 3. AI (Gemini): If cloud is empty/irrelevant, ask AI to generate a result.
     */
    fun searchServicesSmart(query: String): Flow<List<Service>> {
        // Step 1: Start observing the Local Database.
        // This returns a 'Flow' which is a live stream of data.
        // Whenever the DB changes (e.g., we insert new results later), this updates automatically.
        val local = serviceDao.searchServices(query)
        
        return local.onEach { hits ->
            // 'onEach' lets us perform side-effects without changing the data stream.
            // Here, we check if the local search failed (empty results).
            if (hits.isEmpty()) {
                // Launch a background task (Coroutine) to check remote sources
                appScope.launch(Dispatchers.IO) {
                    // Step 2: Check Firestore (The Cloud)
                    val remoteDtos = FirestoreApi.searchServicesRemote(query)
                    val remote = remoteDtos.map { it.toEntity() }
                    
                    // Check if the remote results are actually good matches.
                    // Sometimes cloud search might return loose matches we don't want.
                    val isRelevant = remote.any { service ->
                        service.title.en.contains(query, ignoreCase = true) ||
                        service.title.bn.contains(query, ignoreCase = true) ||
                        service.searchKeywords.contains(query, ignoreCase = true)
                    }

                    if (isRelevant) {
                        // Found good results in Cloud! 
                        // Save them to Local DB. This will trigger the 'local' Flow above
                        // to emit the new data to the UI instantly.
                        serviceDao.insertServices(remote)
                    } else {
                        // Step 3: Gemini AI Fallback
                        // If Cloud failed, we ask the AI to "dream up" or find the service.
                        geminiRepository.generateService(query).collect { result ->
                            if (result != null) {
                                val (service, detail) = result
                                // Save the AI-generated result to Local DB.
                                // Again, this updates the UI automatically via the 'local' Flow.
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