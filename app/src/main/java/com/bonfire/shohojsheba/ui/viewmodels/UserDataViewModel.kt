package com.bonfire.shohojsheba.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonfire.shohojsheba.data.database.entities.Service
import com.bonfire.shohojsheba.data.repositories.FirebaseRepository // Make sure this is the only repository import
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UserDataUiState {
    object Loading : UserDataUiState()
    data class Favorites(val favorites: List<Service>) : UserDataUiState()
    data class History(val history: List<Service>) : UserDataUiState()
    data class Error(val message: String) : UserDataUiState()
}

class UserDataViewModel(private val repository: FirebaseRepository) : ViewModel() {

    private val _favoritesUiState = MutableStateFlow<UserDataUiState>(UserDataUiState.Loading)
    val favoritesUiState: StateFlow<UserDataUiState> = _favoritesUiState.asStateFlow()

    private val _historyUiState = MutableStateFlow<UserDataUiState>(UserDataUiState.Loading)
    val historyUiState: StateFlow<UserDataUiState> = _historyUiState.asStateFlow()

    init {
        loadFavorites()
        loadHistory()
    }

    private fun loadFavorites() {
        // We now launch a single coroutine to handle the entire data fetching process.
        viewModelScope.launch {
            _favoritesUiState.value = UserDataUiState.Loading

            // Step 1: Get the list of favorite objects (which only contain serviceIds).
            val favoritesResult = repository.getFavorites()

            favoritesResult.fold(
                onSuccess = { userFavorites ->
                    val serviceIds = userFavorites.map { it.serviceId }

                    // If there are no favorites, show an empty list and stop.
                    if (serviceIds.isEmpty()) {
                        _favoritesUiState.value = UserDataUiState.Favorites(emptyList())
                        return@fold
                    }

                    // Step 2: Use the IDs to get the full service details.
                    val servicesResult = repository.getServicesByIds(serviceIds)
                    servicesResult.fold(
                        onSuccess = { services ->
                            _favoritesUiState.value = UserDataUiState.Favorites(services)
                        },
                        onFailure = { error ->
                            _favoritesUiState.value = UserDataUiState.Error(error.message ?: "Failed to load favorite services")
                        }
                    )
                },
                onFailure = { error ->
                    _favoritesUiState.value = UserDataUiState.Error(error.message ?: "Failed to load favorites")
                }
            )
        }
    }

    private fun loadHistory() {
        // The same pattern is applied here for loading history.
        viewModelScope.launch {
            _historyUiState.value = UserDataUiState.Loading

            // Step 1: Get the list of history objects.
            val historyResult = repository.getRecentHistory(20)

            historyResult.fold(
                onSuccess = { userHistory ->
                    val serviceIds = userHistory.map { it.serviceId }

                    // If history is empty, show an empty list.
                    if (serviceIds.isEmpty()) {
                        _historyUiState.value = UserDataUiState.History(emptyList())
                        return@fold
                    }

                    // Step 2: Use the IDs to get the full service details.
                    val servicesResult = repository.getServicesByIds(serviceIds)
                    servicesResult.fold(
                        onSuccess = { services ->
                            _historyUiState.value = UserDataUiState.History(services)
                        },
                        onFailure = { error ->
                            _historyUiState.value = UserDataUiState.Error(error.message ?: "Failed to load history services")
                        }
                    )
                },
                onFailure = { error ->
                    _historyUiState.value = UserDataUiState.Error(error.message ?: "Failed to load history")
                }
            )
        }
    }

    // Note: The clearOldHistory function has been removed. Deleting multiple documents
    // in Firestore is a more advanced operation that typically requires batched writes
    // or a Cloud Function for efficiency. We can add this back later if needed.
}