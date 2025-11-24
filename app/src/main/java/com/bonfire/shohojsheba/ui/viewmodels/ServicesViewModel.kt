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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import com.bonfire.shohojsheba.data.database.entities.UserHistory
import com.bonfire.shohojsheba.data.database.entities.UserFavorite
import com.bonfire.shohojsheba.data.database.entities.ServiceDetail

sealed class ServicesUiState {
    object Loading : ServicesUiState()
    data class Success(val services: List<Service>) : ServicesUiState()
    data class Error(val message: String) : ServicesUiState()
}

class ServicesViewModel(
    private val repository: Repository,
    private val networkStatusTracker: NetworkStatusTracker
) : ViewModel() {
    private val _uiState = MutableStateFlow<ServicesUiState>(ServicesUiState.Success(emptyList()))
    val uiState: StateFlow<ServicesUiState> = _uiState.asStateFlow()

    private val _aiResponse = MutableStateFlow<String?>(null)
    val aiResponse: StateFlow<String?> = _aiResponse.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    private var searchJob: Job? = null
    private var currentCategory: String? = null
    
    // Query caching to prevent redundant searches
    private var lastQuery: String? = null
    private var lastResults: List<Service>? = null
    private var currentAiQuery: String? = null // Track active AI search to prevent race conditions
    
    // Temporary storage for AI result (not saved to DB until clicked)
    private var tempAiResult: Pair<Service, ServiceDetail>? = null

    val allServices: Flow<List<Service>> = repository.getAllServices()
    val history: Flow<List<UserHistory>> = repository.getRecentHistory()
    val favorites: Flow<List<UserFavorite>> = repository.getFavorites()
    private val _dataSource = MutableStateFlow("Cache")
    val dataSource = _dataSource.asStateFlow()

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
        searchJob?.cancel()
        currentCategory = null
        lastQuery = null
        lastResults = null
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

    fun searchServices(query: String, enableAI: Boolean = false) {
        // Normalize query for comparison
        val normalizedQuery = query.trim().lowercase()
        
        // Skip if same query as last search (cache hit)
        if (normalizedQuery == lastQuery && !enableAI && lastResults != null) {
            _uiState.value = ServicesUiState.Success(lastResults!!)
            return
        }

        // If AI search is running for this query, ignore local search request
        // This prevents the delayed LaunchedEffect from overwriting the AI Loading state
        if (normalizedQuery == currentAiQuery && !enableAI) {
            return
        }
        
        lastQuery = normalizedQuery
        searchJob?.cancel()
        currentCategory = null
        _aiResponse.value = null
        
        searchJob = repository.getAllServices()
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
                
                // If enableAI is true (user pressed Enter), ALWAYS trigger AI search
                // This allows AI search even when fuzzy results exist but aren't what user wants
                if (enableAI) {
                    searchJob?.cancel()
                    searchWithAI(query)
                } else if (fuzzyResults.isEmpty()) {
                    // No AI requested and no results found
                    lastResults = emptyList()
                    _uiState.value = ServicesUiState.Success(emptyList())
                } else {
                    // No AI requested but we have fuzzy results - show them
                    lastResults = fuzzyResults
                    _uiState.value = ServicesUiState.Success(fuzzyResults)
                }
            }
            .catch { e -> _uiState.value = ServicesUiState.Error(e.message ?: "An unknown error occurred") }
            .launchIn(viewModelScope)
    }

    fun searchWithAI(query: String) {
        val normalizedQuery = query.trim().lowercase()
        currentAiQuery = normalizedQuery
        
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
                        // Store temporarily (do NOT save to DB yet)
                        tempAiResult = service to detail
                        
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
            } finally {
                currentAiQuery = null // Reset flag when done
            }
        }
    }
    fun onServiceClicked(service: Service) {
        viewModelScope.launch {
            // Check if this is the temporary AI result
            if (tempAiResult != null && tempAiResult!!.first.id == service.id) {
                val (aiService, aiDetail) = tempAiResult!!
                // NOW save to DB since user clicked it
                repository.serviceDao.insertServices(listOf(aiService))
                repository.serviceDao.insertServiceDetails(listOf(aiDetail))
                Log.d("ServicesViewModel", "Persisted AI result to DB: ${aiService.title.en}")
                tempAiResult = null // Clear temp storage
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
