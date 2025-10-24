package com.bonfire.shohojsheba.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonfire.shohojsheba.BuildConfig
import com.bonfire.shohojsheba.data.database.entities.Service
import com.bonfire.shohojsheba.data.repositories.Repository
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed class ServicesUiState {
    object Loading : ServicesUiState()
    data class Success(val services: List<Service>) : ServicesUiState()
    data class Error(val message: String) : ServicesUiState()
}

class ServicesViewModel(private val repository: Repository, private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow<ServicesUiState>(ServicesUiState.Success(emptyList()))
    val uiState: StateFlow<ServicesUiState> = _uiState

    private val _aiResponse = MutableStateFlow<String?>(null)
    val aiResponse: StateFlow<String?> = _aiResponse

    fun clearSearch() {
        _uiState.value = ServicesUiState.Success(emptyList())
        _aiResponse.value = null
    }

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
                _uiState.value =
                    ServicesUiState.Error(e.message ?: "An unknown error occurred")
            }
            .launchIn(viewModelScope)
    }

    fun searchServices(query: String) {
        // Clear previous AI response when a new search starts
        _aiResponse.value = null
        repository.getAllServices()
            .onEach { services ->
                val filteredList = if (query.isBlank()) {
                    emptyList()
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
                _uiState.value =
                    ServicesUiState.Error(e.message ?: "An unknown error occurred")
            }
            .launchIn(viewModelScope)
    }

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
                    modelName = "gemini-2.5-flash-lite",
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
