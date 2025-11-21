package com.bonfire.shohojsheba.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonfire.shohojsheba.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


sealed class AiResponseState {
    object Idle : AiResponseState()
    object Loading : AiResponseState()
    data class Success(val responseText: String) : AiResponseState()
    data class Error(val errorMessage: String) : AiResponseState()
}

class ChatViewModel : ViewModel() {

    private val _aiResponse = MutableStateFlow<AiResponseState>(AiResponseState.Idle)
    val aiResponse = _aiResponse.asStateFlow()

    fun searchWithAI(query: String) {
        viewModelScope.launch {

            _aiResponse.value = AiResponseState.Loading

            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank()) {
                _aiResponse.value = AiResponseState.Error("⚠️ Gemini AI key not found.")
                return@launch
            }

            try {

                val generativeModel = GenerativeModel("gemini-2.5-flash-lite", apiKey)

                val prompt = "Provide a detailed, step-by-step guide for the following service: '$query'. Assume the service is for Bangladesh unless another country is specified. Respond in the same language as the query. Do not use any markdown formatting like asterisks. Use new lines for clear spacing on a mobile screen. Do not introduce yourself as an AI. Just provide the steps."

                val response = generativeModel.generateContent(prompt)
                _aiResponse.value = AiResponseState.Success(response.text ?: "Sorry, I couldn't generate a response.")

            } catch (e: Exception) {
                _aiResponse.value = AiResponseState.Error("⚠️ Something went wrong: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    // Call this to reset the state after the response has been displayed
    fun clearResponseState() {
        _aiResponse.value = AiResponseState.Idle
    }
}