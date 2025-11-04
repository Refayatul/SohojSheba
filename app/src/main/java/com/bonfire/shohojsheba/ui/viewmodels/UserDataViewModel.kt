package com.bonfire.shohojsheba.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonfire.shohojsheba.data.database.entities.Service
import com.bonfire.shohojsheba.data.repositories.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed class UserDataUiState {
    object Loading : UserDataUiState()
    data class Favorites(val favorites: List<Service>) : UserDataUiState()
    data class History(val history: List<Service>) : UserDataUiState()
    data class Error(val message: String) : UserDataUiState()
}

class UserDataViewModel(private val repository: Repository) : ViewModel() {

    private val _favoritesUiState = MutableStateFlow<UserDataUiState>(UserDataUiState.Loading)
    val favoritesUiState: StateFlow<UserDataUiState> = _favoritesUiState.asStateFlow()

    private val _historyUiState = MutableStateFlow<UserDataUiState>(UserDataUiState.Loading)
    val historyUiState: StateFlow<UserDataUiState> = _historyUiState.asStateFlow()

    init {
        loadFavorites()
        loadHistory()
    }

    private fun loadFavorites() {
        repository.getFavorites()
            .flatMapLatest { favorites ->
                val serviceIds = favorites.map { it.serviceId }
                repository.getServicesByIds(serviceIds)
            }
            .onEach { services ->
                _favoritesUiState.value = UserDataUiState.Favorites(services)
            }
            .launchIn(viewModelScope)
    }

    private fun loadHistory() {
        repository.getRecentHistory(20)
            .flatMapLatest { history ->
                val serviceIds = history.map { it.serviceId }
                repository.getServicesByIds(serviceIds)
            }
            .onEach { services ->
                _historyUiState.value = UserDataUiState.History(services)
            }
            .launchIn(viewModelScope)
    }

    fun clearOldHistory(cutoffDate: Long) {
        viewModelScope.launch {
            repository.clearOldHistory(cutoffDate)
        }
    }
}
