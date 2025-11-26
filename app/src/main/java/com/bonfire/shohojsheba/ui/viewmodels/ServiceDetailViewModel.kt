package com.bonfire.shohojsheba.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonfire.shohojsheba.data.database.entities.UserFavorite
import com.bonfire.shohojsheba.data.database.entities.UserHistory
import com.bonfire.shohojsheba.data.mappers.toRemote
import com.bonfire.shohojsheba.data.remote.FirestoreApi
import com.bonfire.shohojsheba.data.repositories.Repository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ServiceDetailViewModel(private val repository: Repository, private val serviceId: String) : ViewModel() {

    val service = repository.getServiceById(serviceId)
    val serviceDetail = repository.getServiceDetail(serviceId)
    val isFavorite = repository.isFavorite(serviceId)

    init {
        viewModelScope.launch {
            // Ensure we have the full details (instructions, etc.) for this service.
            // If not locally available, it will try to fetch from Firestore.
            repository.ensureDetail(serviceId)
            
            // Add this service to the user's history
            repository.addHistory(UserHistory(serviceId = serviceId, accessedDate = System.currentTimeMillis()))
            
            // Sync AI-generated service to Firestore if it exists locally
            syncServiceToFirestore()
        }
    }
    
    private suspend fun syncServiceToFirestore() {
        try {
            // Get the service from local database
            val serviceEntity = service.first()
            val detailEntity = serviceDetail.first()
            
            if (serviceEntity != null && detailEntity != null) {
                // Check if this is an AI-generated service (has recent timestamp)
                // Save to Firestore so it's available globally
                FirestoreApi.saveService(serviceEntity.toRemote())
                FirestoreApi.saveServiceDetails(detailEntity.toRemote())
            }
        } catch (e: Exception) {
            // Silently fail - not critical
            e.printStackTrace()
        }
    }

    fun addFavorite(favorite: UserFavorite) {
        viewModelScope.launch {
            repository.addFavorite(favorite)
        }
    }

    fun removeFavorite(serviceId: String) {
        viewModelScope.launch {
            repository.removeFavorite(serviceId)
        }
    }
}
