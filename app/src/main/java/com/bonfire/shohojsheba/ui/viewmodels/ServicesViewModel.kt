package com.bonfire.shohojsheba.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonfire.shohojsheba.data.database.entities.Service
import com.bonfire.shohojsheba.data.repositories.Repository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

sealed class ServicesUiState {
    object Loading : ServicesUiState()
    data class Success(val services: List<Service>) : ServicesUiState()
    data class Error(val message: String) : ServicesUiState()
}

@OptIn(FlowPreview::class)
class ServicesViewModel(private val repository: Repository, private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow<ServicesUiState>(ServicesUiState.Loading)
    val uiState: StateFlow<ServicesUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        val allServicesFlow = repository.getAllServices()

        _searchQuery
            .debounce(300)
            .combine(allServicesFlow) { query, services ->
                if (query.isBlank()) {
                    services // Return all services if query is blank
                } else {
                    services.filter { service ->
                        val title = context.getString(service.titleRes).lowercase()
                        val subtitle = context.getString(service.subtitleRes).lowercase()
                        val q = query.lowercase()
                        title.contains(q) || subtitle.contains(q)
                    }
                }
            }
            .onEach { filteredServices ->
                _uiState.value = ServicesUiState.Success(filteredServices)
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
    
    fun loadServicesByCategory(category: String) {
        repository.getServicesByCategory(category)
            .onEach { services ->
                _uiState.value = ServicesUiState.Success(services)
            }
            .launchIn(viewModelScope)
    }
}
