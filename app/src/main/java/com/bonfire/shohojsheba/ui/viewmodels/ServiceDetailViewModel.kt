package com.bonfire.shohojsheba.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonfire.shohojsheba.data.database.entities.Service
import com.bonfire.shohojsheba.data.database.entities.ServiceDetail
import com.bonfire.shohojsheba.data.repositories.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

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
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ServiceDetailUiState>(ServiceDetailUiState.Loading)
    val uiState: StateFlow<ServiceDetailUiState> = _uiState.asStateFlow()

    init {
        loadServiceDetails()
        addToHistory()
    }

    private fun loadServiceDetails() {
        val serviceFlow = repository.getServiceById(serviceId)
        val serviceDetailFlow = repository.getServiceDetail(serviceId)
        val isFavoriteFlow = repository.isFavorite(serviceId)

        combine(serviceFlow, serviceDetailFlow, isFavoriteFlow) { service, serviceDetail, isFavorite ->
            if (service != null) {
                ServiceDetailUiState.Success(service, serviceDetail, isFavorite)
            } else {
                ServiceDetailUiState.Error("Service not found")
            }
        }
            .onEach { _uiState.value = it }
            .launchIn(viewModelScope)
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val isFavorite = (_uiState.value as? ServiceDetailUiState.Success)?.isFavorite ?: return@launch
            if (isFavorite) {
                repository.removeFavorite(serviceId)
            } else {
                repository.addFavorite(serviceId)
            }
        }
    }

    private fun addToHistory() {
        viewModelScope.launch {
            repository.addHistory(serviceId)
        }
    }
}
