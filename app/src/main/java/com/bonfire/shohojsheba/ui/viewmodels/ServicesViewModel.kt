package com.bonfire.shohojsheba.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonfire.shohojsheba.BuildConfig
import com.bonfire.shohojsheba.data.database.entities.Service // Your new data class
import com.bonfire.shohojsheba.data.repositories.FirebaseRepository // New Repository
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ServicesUiState {
    object Loading : ServicesUiState()
    data class Success(val services: List<Service>) : ServicesUiState()
    data class Error(val message: String) : ServicesUiState()
}

class ServicesViewModel(
    private val repository: FirebaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ServicesUiState>(ServicesUiState.Success(emptyList()))
    val uiState: StateFlow<ServicesUiState> = _uiState

    private val _aiResponse = MutableStateFlow<String?>(null)
    val aiResponse: StateFlow<String?> = _aiResponse

    // We will cache all services here to avoid re-fetching from Firestore for every search query
    private val _allServices = MutableStateFlow<List<Service>>(emptyList())

    fun clearSearch() {
        _uiState.value = ServicesUiState.Success(emptyList())
        _aiResponse.value = null
    }

    // Call this once to load all services for the search screen
    fun loadAllServicesForSearch() {
        if (_allServices.value.isNotEmpty()) return // Don't reload if we already have them

        viewModelScope.launch {
            repository.getAllServices().fold(
                onSuccess = { services -> _allServices.value = services },
                onFailure = { _uiState.value = ServicesUiState.Error(it.message ?: "Failed to load services") }
            )
        }
    }

    fun loadServicesByCategory(category: String) {
        viewModelScope.launch {
            _uiState.value = ServicesUiState.Loading
            repository.getServicesByCategory(category).fold(
                onSuccess = { services ->
                    if (services.isEmpty()) {
                        _uiState.value = ServicesUiState.Error("No services found for this category.")
                    } else {
                        _uiState.value = ServicesUiState.Success(services)
                    }
                },
                onFailure = { e ->
                    _uiState.value = ServicesUiState.Error(e.message ?: "An unknown error occurred")
                }
            )
        }
    }

    // Search now filters the cached list instead of hitting the database
    fun searchServices(query: String) {
        _aiResponse.value = null
        val filteredList = if (query.isBlank()) {
            emptyList()
        } else {
            val q = query.lowercase()
            _allServices.value.filter { service ->
                // Search in both English and Bengali titles DIRECTLY. NO context.getString()
                val englishTitle = service.title["en"]?.lowercase()
                val bengaliTitle = service.title["bn"]?.lowercase()

                (englishTitle?.contains(q) == true) || (bengaliTitle?.contains(q) == true)
            }
        }
        _uiState.value = ServicesUiState.Success(filteredList)
    }

    // This function does not change much as it doesn't use our repository
    fun searchWithAI(query: String) {
        viewModelScope.launch {
            _uiState.value = ServicesUiState.Loading
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank()) {
                _aiResponse.value = "⚠️ Gemini AI key not found. Please configure it in local.properties."
                _uiState.value = ServicesUiState.Success(emptyList())
                return@launch
            }

            try {
                val generativeModel = GenerativeModel(
                    modelName = "gemini-1.5-flash-latest",
                    apiKey = apiKey
                )
                val prompt = "Provide a detailed, step-by-step guide for the following service: '$query'. Assume the service is for Bangladesh unless another country is specified. Respond in the same language as the query. Do not use any markdown formatting like asterisks. Use new lines for clear spacing on a mobile screen. Do not introduce yourself as an AI. Just provide the steps."
                val response = generativeModel.generateContent(prompt)
                _aiResponse.value = response.text
                _uiState.value = ServicesUiState.Success(emptyList())
            } catch (e: Exception) {
                _aiResponse.value = "⚠️ Something went wrong: ${e.localizedMessage ?: "Unknown error"}"
                _uiState.value = ServicesUiState.Success(emptyList())
            }
        }
    }
}