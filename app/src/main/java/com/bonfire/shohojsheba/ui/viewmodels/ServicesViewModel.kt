package com.bonfire.shohojsheba.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonfire.shohojsheba.data.database.entities.Service
import com.bonfire.shohojsheba.data.repositories.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

sealed class ServicesUiState {
    object Loading : ServicesUiState()
    data class Success(val services: List<Service>) : ServicesUiState()
    data class Error(val message: String) : ServicesUiState()
}

class ServicesViewModel(private val repository: Repository, private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow<ServicesUiState>(ServicesUiState.Loading)
    val uiState: StateFlow<ServicesUiState> = _uiState

    // This function will be called by the UI to fetch data for a specific category.
    // It's a clear, single entry point.
    fun loadServicesByCategory(category: String) {
        repository.getServicesByCategory(category)
            .onEach { services ->
                if (services.isEmpty()) {
                    _uiState.value = ServicesUiState.Error("No services found for this category.")
                } else {
                    _uiState.value = ServicesUiState.Success(services)
                }
            }
            .catch { e ->
                _uiState.value = ServicesUiState.Error(e.message ?: "An unknown error occurred")
            }
            .launchIn(viewModelScope)
    }
    
    // Search functionality is kept separate to avoid conflicts.
    fun searchServices(query: String) {
        repository.getAllServices()
            .onEach { services ->
                 val filteredList = if (query.isBlank()) {
                    emptyList() // Clear results if query is empty
                } else {
                    val q = query.lowercase()
                    services.filter {
                        context.getString(it.titleRes).lowercase().contains(q) ||
                        context.getString(it.subtitleRes).lowercase().contains(q)
                    }
                }
                _uiState.value = ServicesUiState.Success(filteredList)
            }
            .catch { e ->
                _uiState.value = ServicesUiState.Error(e.message ?: "An unknown error occurred")
            }
            .launchIn(viewModelScope)
    }
}
