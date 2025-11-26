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
    // --- State Management ---
    // UI State: Tracks Loading, Success (with list of services), or Error
    private val _uiState = MutableStateFlow<ServicesUiState>(ServicesUiState.Success(emptyList()))
    val uiState: StateFlow<ServicesUiState> = _uiState.asStateFlow()

    // AI Response: Stores error messages or status updates from Gemini AI
    private val _aiResponse = MutableStateFlow<String?>(null)
    val aiResponse: StateFlow<String?> = _aiResponse.asStateFlow()

    // Toast Messages: One-time notifications for the user
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    // --- Search & Caching Variables ---
    private var searchJob: Job? = null // Handle to cancel ongoing search requests
    private var currentCategory: String? = null // Currently selected category filter
    
    // Query caching to prevent redundant searches
    private var lastQuery: String? = null
    private var lastResults: List<Service>? = null
    private var currentAiQuery: String? = null // Track active AI search to prevent race conditions
    
    // Temporary storage for AI result (not saved to DB until clicked)
    // This prevents cluttering the database with every random AI search
    private var tempAiResult: Pair<Service, ServiceDetail>? = null

    // --- Data Streams ---
    // These flows automatically update when the database changes
    val allServices: Flow<List<Service>> = repository.getAllServices()
    val history: Flow<List<UserHistory>> = repository.getRecentHistory()
    val favorites: Flow<List<UserFavorite>> = repository.getFavorites()
    
    // Data Source: Tracks if data is coming from "Cache" (local DB) or "Network" (Firestore)
    private val _dataSource = MutableStateFlow("Cache")
    val dataSource = _dataSource.asStateFlow()

    init {
        Log.d("ServicesViewModel", "=== NEW ViewModel INSTANCE CREATED ===")
        Log.d("ServicesViewModel", "Instance hashCode: ${this.hashCode()}")
        
        // 1. Initial Data Load
        // Try to fetch latest data from Firestore. If offline, use local DB.
        viewModelScope.launch {
            val source = repository.refreshIfNeeded()
            _dataSource.value = source
            // Warn user if they are offline and have never synced before
            if (source == "Offline" && !repository.hasSyncedOnce()) {
                _toastMessage.tryEmit("Your internet connection is off. Turn it on to get the latest data.")
            }
        }

        // 2. Network Connectivity Monitoring
        // Automatically refresh data when internet connection is restored
        viewModelScope.launch {
            var wasOffline = !networkStatusTracker.isNetworkAvailable.value
            networkStatusTracker.isNetworkAvailable
                .collect { isAvailable ->
                    if (isAvailable && wasOffline) {
                        // Connection restored!
                         _toastMessage.emit("Back online. Fetching fresh data...")
                         refreshData()
                         // If we were showing an error on a specific category, retry loading it
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

    // --- Search Logic ---
    fun searchServices(query: String, enableAI: Boolean = false) {
        // Normalize query for comparison (case-insensitive)
        val normalizedQuery = query.trim().lowercase()
        
        // 1. Cache Check: Skip if same query as last search
        if (normalizedQuery == lastQuery && !enableAI && lastResults != null) {
            _uiState.value = ServicesUiState.Success(lastResults!!)
            return
        }

        // 2. Race Condition Prevention
        // If AI search is running for this query, ignore local search request
        // This prevents the delayed LaunchedEffect from overwriting the AI Loading state
        if (normalizedQuery == currentAiQuery && !enableAI) {
            return
        }
        
        // 3. Reset State for New Search
        lastQuery = normalizedQuery
        searchJob?.cancel() // Cancel any previous search
        currentCategory = null
        _aiResponse.value = null
        
        // 4. Perform Search
        searchJob = repository.getAllServices()
            .onEach { services ->
                // Apply fuzzy matching algorithm to rank results by relevance
                val fuzzyResults = services.fuzzyFilter(
                    query = query,
                    minScore = 0.3  // Minimum 30% similarity required
                ) { service ->
                    // Search in multiple fields: Title (EN/BN), Subtitle (EN/BN), Keywords
                    listOf(
                        service.title.en,
                        service.title.bn,
                        service.subtitle.en,
                        service.subtitle.bn,
                        service.searchKeywords
                    )
                }
                
                // 5. AI Search Trigger
                // If enableAI is true (user pressed Enter), ALWAYS trigger AI search
                // This allows AI search even when fuzzy results exist but aren't what user wants
                if (enableAI) {
                    searchJob?.cancel()
                    searchWithAI(query)
                } else if (fuzzyResults.isEmpty()) {
                    // No AI requested and no local results found
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

    // --- AI Search Logic (Gemini) ---
    fun searchWithAI(query: String) {
        val normalizedQuery = query.trim().lowercase()
        currentAiQuery = normalizedQuery // Mark as active AI search
        
        viewModelScope.launch {
            _uiState.value = ServicesUiState.Loading // Show loading spinner
            
            // 1. Check API Key
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank()) {
                _aiResponse.value = "⚠️ Gemini AI key not found."
                _uiState.value = ServicesUiState.Success(emptyList())
                return@launch
            }

            try {
                // 2. Call Gemini API
                val geminiRepository = com.bonfire.shohojsheba.data.repositories.GeminiRepository()
                geminiRepository.generateService(query).collect { result ->
                    if (result != null) {
                        val (service, detail) = result
                        
                        // 3. Store Result Temporarily
                        // We do NOT save to DB yet. We only save if the user clicks the result.
                        // This keeps our database clean.
                        tempAiResult = service to detail
                        
                        // 4. Show the generated service
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
    // --- Persistence Logic ---
    // Called when a user clicks on a service card
    fun onServiceClicked(service: Service) {
        viewModelScope.launch {
            // Check if this is the temporary AI result we generated earlier
            if (tempAiResult != null && tempAiResult!!.first.id == service.id) {
                val (aiService, aiDetail) = tempAiResult!!
                
                // NOW save to DB since user clicked it (Implicit "Save" action)
                // This ensures we only persist useful AI results
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
