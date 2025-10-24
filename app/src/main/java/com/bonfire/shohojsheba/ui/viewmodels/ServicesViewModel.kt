package com.bonfire.shohojsheba.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonfire.shohojsheba.BuildConfig
import com.bonfire.shohojsheba.data.database.entities.Service
import com.bonfire.shohojsheba.data.repositories.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

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
                val url =
                    URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=$apiKey")

                val jsonRequest = JSONObject().apply {
                    put(
                        "contents",
                        listOf(
                            mapOf(
                                "parts" to listOf(
                                    mapOf(
                                        "text" to "Find Bangladesh government or citizen services related to: $query"
                                    )
                                )
                            )
                        )
                    )
                }

                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    connectTimeout = 7000
                    readTimeout = 7000
                }

                connection.outputStream.use { os ->
                    os.write(jsonRequest.toString().toByteArray())
                }

                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw IOException("AI API error: $responseCode")
                }

                val response =
                    connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val text = json.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")
                    ?: "No AI response available."

                _aiResponse.value = text
                _uiState.value = ServicesUiState.Success(emptyList())

            } catch (e: IOException) {
                _aiResponse.value = "🌐 No internet connection. Try again later."
                _uiState.value = ServicesUiState.Success(emptyList())
            } catch (e: Exception) {
                _aiResponse.value = "⚠️ Something went wrong: ${e.localizedMessage ?: "Unknown error"}"
                _uiState.value = ServicesUiState.Success(emptyList())
            }
        }
    }
}
