package com.bonfire.shohojsheba.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonfire.shohojsheba.BuildConfig
import com.bonfire.shohojsheba.data.database.entities.Service
import com.bonfire.shohojsheba.data.repositories.Repository
import com.bonfire.shohojsheba.util.NetworkStatusTracker
import com.bonfire.shohojsheba.utils.fuzzyFilter
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed class ServicesUiState {
    object Loading : ServicesUiState()
    data class Success(val services: List<Service>) : ServicesUiState()
    data class Error(val message: String) : ServicesUiState()
}

class ServicesViewModel(
    private val repository: Repository,
    private val networkStatusTracker: NetworkStatusTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow<ServicesUiState>(ServicesUiState.Loading)
    val uiState: StateFlow<ServicesUiState> = _uiState

    private val _aiResponse = MutableStateFlow<String?>(null)
    val aiResponse: StateFlow<String?> = _aiResponse

    private val _toastMessage = MutableSharedFlow<String>(replay = 1)
    val toastMessage = _toastMessage.asSharedFlow()

    val allServices = repository.getAllServices()
    val history = repository.getRecentHistory()
    val favorites = repository.getFavorites()
    private val _dataSource = MutableStateFlow("Cache")
    val dataSource = _dataSource.asStateFlow()

    private var currentCategory: String? = null

    init {
        Log.d("ServicesViewModel", "=== NEW ViewModel INSTANCE CREATED ===")
        Log.d("ServicesViewModel", "Instance hashCode: ${this.hashCode()}")
        // Initial data load
        viewModelScope.launch {
            val source = repository.refreshIfNeeded()
            _dataSource.value = source
            if (source == "Offline" && !repository.hasSyncedOnce()) {
                _toastMessage.tryEmit("Your internet connection is off. Turn it on to get the latest data.")
            }
        }

        // Observe network changes to auto-refresh
        viewModelScope.launch {
            var wasOffline = !networkStatusTracker.isNetworkAvailable.value
            networkStatusTracker.isNetworkAvailable
                .collect { isAvailable ->
                    if (isAvailable && wasOffline) {
                         _toastMessage.emit("Back online. Fetching fresh data...")
                         refreshData()
                        if (_uiState.value is ServicesUiState.Error && currentCategory != null) {
                            _toastMessage.emit("Retrying to load category...")
                            loadServicesByCategory(currentCategory!!)
                        }
                    }
                    wasOffline = !isAvailable
                }
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            _dataSource.value = repository.refreshIfNeeded()
        }
    }

    fun clearSearch() {
        currentCategory = null
        _uiState.value = ServicesUiState.Success(emptyList())
        _aiResponse.value = null
    }

    fun loadServicesByCategory(category: String) {
        Log.d("ServicesViewModel", "=== loadServicesByCategory CALLED ===")
        Log.d("ServicesViewModel", "Category: $category")
        Log.d("ServicesViewModel", "ViewModel instance: ${this.hashCode()}")
        
        currentCategory = category
        repository.getServicesByCategory(category)
            .onEach { services ->
                Log.d("ServicesViewModel", "Received ${services.size} services for category: $category")
                if (services.isNotEmpty()) {
                    Log.d("ServicesViewModel", "First service title: ${services.first().title}")
                }
                _uiState.value = if (services.isEmpty()) {
                    ServicesUiState.Error("No services found for this category.")
                } else {
                    ServicesUiState.Success(services)
                }
            }
            .catch { e -> _uiState.value = ServicesUiState.Error(e.message ?: "An unknown error occurred") }
            .launchIn(viewModelScope)
    }

    private var searchJob: kotlinx.coroutines.Job? = null

    fun searchServices(query: String, enableAI: Boolean = false) {
        searchJob?.cancel()
        currentCategory = null
        _aiResponse.value = null
        
        searchJob = repository.searchServices(query)
            .onEach { services ->
                // Apply fuzzy matching to rank results by relevance
                val fuzzyResults = services.fuzzyFilter(
                    query = query,
                    minScore = 0.3  // Minimum 30% similarity
                ) { service ->
                    // Search in multiple fields
                    listOf(
                        service.title.en,
                        service.title.bn,
                        service.subtitle.en,
                        service.subtitle.bn,
                        service.searchKeywords
                    )
                }
                
                if (fuzzyResults.isEmpty()) {
                    if (enableAI) {
                        // Auto-trigger AI search when no results found AND AI is enabled
                        // Stop listening to this flow to prevent overwriting AI results with empty list
                        searchJob?.cancel()
                        searchWithAI(query)
                    } else {
                         _uiState.value = ServicesUiState.Success(emptyList())
                    }
                } else {
                    // Return fuzzy-matched and sorted results
                    _uiState.value = ServicesUiState.Success(fuzzyResults)
                }
            }
            .catch { e -> _uiState.value = ServicesUiState.Error(e.message ?: "An unknown error occurred") }
            .launchIn(viewModelScope)
    }

    fun searchWithAI(query: String) {
        viewModelScope.launch {
            _uiState.value = ServicesUiState.Loading
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank()) {
                _aiResponse.value = "⚠️ Gemini AI key not found."
                _uiState.value = ServicesUiState.Success(emptyList())
                return@launch
            }

            try {
                val geminiRepository = com.bonfire.shohojsheba.data.repositories.GeminiRepository()
                geminiRepository.generateService(query).collect { result ->
                    if (result != null) {
                        val (service, detail) = result
                        // Save to local DB only (for display)
                        // Will be saved to Firestore when user opens it
                        repository.serviceDao.insertServices(listOf(service))
                        repository.serviceDao.insertServiceDetails(listOf(detail))
                        // Show the generated service
                        _uiState.value = ServicesUiState.Success(listOf(service))
                        _aiResponse.value = null
                    } else {
                        _aiResponse.value = "⚠️ Could not generate service information."
                        _uiState.value = ServicesUiState.Success(emptyList())
                    }
                }
            } catch (e: Exception) {
                _aiResponse.value = "⚠️ Something went wrong: ${e.localizedMessage ?: "Unknown error"}"
                _uiState.value = ServicesUiState.Success(emptyList())
            }
        }
    }
    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }

    fun clearFavorites() {
        viewModelScope.launch {
            repository.clearAllFavorites()
        }
    }
}
