package com.bonfire.shohojsheba.ui.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonfire.shohojsheba.BuildConfig
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
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

    // FIXED: Changed class name from 'ChatSession' to 'Chat'
    private var chat: Chat? = null

    fun searchWithAI(query: String, image: Bitmap? = null) {
        viewModelScope.launch {
            _aiResponse.value = AiResponseState.Loading

            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank()) {
                _aiResponse.value = AiResponseState.Error("⚠️ Gemini AI key not found.")
                return@launch
            }

            try {
                // Initialize Chat if it doesn't exist
                if (chat == null) {
                    val generativeModel = GenerativeModel(
                        modelName = "gemini-2.5-flash-lite",

                        apiKey = apiKey,
                        systemInstruction = content {
                            text("You are Shohoj Sheba, a helpful AI assistant for Bangladesh government services. " +
                                    "Answer naturally and conversationally. " +
                                    "If the user asks for a process, provide a clear guide. " +
                                    "If they send an image of a document, explain what it is or translate it if needed. " +
                                    "Do not use markdown formatting like asterisks.")
                        }
                    )
                    // FIXED: startChat returns a 'Chat' object
                    chat = generativeModel.startChat()
                }

                // Send Message (Text + Optional Image)
                val response = if (image != null) {
                    // FIXED: sendMessage on 'Chat' object
                    chat?.sendMessage(content {
                        image(image)
                        text(query)
                    })
                } else {
                    chat?.sendMessage(query)
                }

                _aiResponse.value = AiResponseState.Success(response?.text ?: "Sorry, I couldn't generate a response.")

            } catch (e: Exception) {
                _aiResponse.value = AiResponseState.Error("⚠️ Error: ${e.localizedMessage ?: "Unknown error"}")
                chat = null // Reset chat on error
            }
        }
    }

    fun clearResponseState() {
        _aiResponse.value = AiResponseState.Idle
    }
}