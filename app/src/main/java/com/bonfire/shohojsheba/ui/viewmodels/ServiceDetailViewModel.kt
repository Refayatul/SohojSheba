package com.bonfire.shohojsheba.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonfire.shohojsheba.data.database.entities.Service
import com.bonfire.shohojsheba.data.database.entities.ServiceDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.bonfire.shohojsheba.data.repositories.FirebaseRepository

sealed class ServiceDetailUiState {
    object Loading : ServiceDetailUiState()
    data class Success(
        val service: Service,
        val serviceDetail: ServiceDetail?,
        val isFavorite: Boolean
    ) : ServiceDetailUiState()
    data class Error(val message: String) : ServiceDetailUiState()
}
class ServiceDetailViewModel(
    private val serviceId: String,
    private val repository: FirebaseRepository // <-- Changed to FirebaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ServiceDetailUiState>(ServiceDetailUiState.Loading)
    val uiState: StateFlow<ServiceDetailUiState> = _uiState.asStateFlow()

    init {
        loadServiceDetails()
        addToHistory()
    }

    private fun loadServiceDetails() {
        viewModelScope.launch {
            _uiState.value = ServiceDetailUiState.Loading

            // Await all results
            val serviceResult = repository.getServiceById(serviceId)
            val detailResult = repository.getServiceDetail(serviceId)
            val isFavoriteResult = repository.isFavorite(serviceId)

            // Process results
            val service = serviceResult.getOrNull()
            if (service != null) {
                _uiState.value = ServiceDetailUiState.Success(
                    service = service,
                    serviceDetail = detailResult.getOrNull(),
                    isFavorite = isFavoriteResult.getOrDefault(false)
                )
            } else {
                _uiState.value = ServiceDetailUiState.Error(
                    serviceResult.exceptionOrNull()?.message ?: "Service not found"
                )
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val currentState = (_uiState.value as? ServiceDetailUiState.Success) ?: return@launch
            if (currentState.isFavorite) {
                repository.removeFavorite(serviceId)
            } else {
                repository.addFavorite(serviceId)
            }
            // Reload to reflect the change
            loadServiceDetails()
        }
    }

    private fun addToHistory() {
        viewModelScope.launch {
            repository.addHistory(serviceId)
        }
    }
}