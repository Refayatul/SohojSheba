package com.bonfire.shohojsheba.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonfire.shohojsheba.data.database.entities.Service
import com.bonfire.shohojsheba.data.repositories.Repository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

sealed class ServicesUiState {
    object Loading : ServicesUiState()
    data class Success(val services: List<Service>) : ServicesUiState()
    data class Error(val message: String) : ServicesUiState()
}

@OptIn(FlowPreview::class)
class ServicesViewModel(private val repository: Repository, private val context: Context) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _category = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ServicesUiState> = _category
        .flatMapLatest { category ->
            val servicesFlow = if (category == null) {
                repository.getAllServices()
            } else {
                repository.getServicesByCategory(category)
            }

            _searchQuery.debounce(300).combine(servicesFlow) { query, services ->
                if (query.isBlank()) {
                    ServicesUiState.Success(services)
                } else {
                    val filteredList = services.filter { service ->
                        val title = context.getString(service.titleRes).lowercase()
                        val subtitle = context.getString(service.subtitleRes).lowercase()
                        val q = query.lowercase()
                        title.contains(q) || subtitle.contains(q)
                    }
                    ServicesUiState.Success(filteredList)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ServicesUiState.Loading
        )

    fun loadServicesByCategory(category: String) {
        _category.value = category
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
