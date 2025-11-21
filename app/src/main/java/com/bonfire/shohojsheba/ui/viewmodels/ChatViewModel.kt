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

    private var chat: Chat? = null

    // UPDATED: Accepts pdfBytes
    fun searchWithAI(query: String, image: Bitmap? = null, pdfBytes: ByteArray? = null) {
        viewModelScope.launch {
            _aiResponse.value = AiResponseState.Loading

            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank()) {
                _aiResponse.value = AiResponseState.Error("⚠️ Gemini AI key not found.")
                return@launch
            }

            try {
                if (chat == null) {
                    val generativeModel = GenerativeModel(
                        modelName = "gemini-2.5-flash-lite",
                        apiKey = apiKey,
                        systemInstruction = content {
                            text("""You are Shohoj Sheba, a helpful assistant dedicated to making Bangladesh government services accessible to everyone.  Do not introduce yourself as an AI

                                    YOUR BEHAVIOR:
                                1. **Small Talk & Greetings:** If the user says "Hi", "Hello", "Salam", "Thanks", or asks "How are you?", respond naturally, politely, and briefly. Ask how you can help them with government services.
                                2. **Service Requests:** If the user asks about a service (e.g., "NID", "Passport", "Birth Certificate", "Trade License" etc), providing a guide is your PRIORITY. You must provide a structured, step-by-step guide.
3. If the user sends a PDF/Image, analyze it, summarize it, or explain the steps inside it

                            FORMATTING RULES FOR SERVICES (Make it Accessible):
                            - **Do NOT use Markdown** (like **bold** or ## headers) because the app cannot display them.
                            - **Use Emojis** to act as headers and bullet points. This makes it visually clear.
                            - **Use new lines for clear spacing on a mobile screen
                                - **Structure:**
                            📢 [Service Name]

                            📋 **Requirements:**
                            • [Item 1]
                            • [Item 2]

                            👣 **Steps:**
                            1. [Step 1]
                            2. [Step 2]

                            💰 **Cost & Time:**
                            • Fee: [Amount]
                            • Time: [Duration]

                           ⚠️ **Note:** [Important Warning]

                            LANGUAGE:
                            - Respond in the exact same language as the user (Bangla or English).
                            - Keep the language SIMPLE (Class 8 reading level) for accessibility.

                            """.trimIndent())
                        }


                    )
                    chat = generativeModel.startChat()
                }

                // Construct the message
                val inputContent = content {
                    if (image != null) {
                        image(image)
                    }
                    if (pdfBytes != null) {
                        // SEND PDF AS BLOB
                        blob("application/pdf", pdfBytes)
                    }
                    text(query)
                }

                val response = chat?.sendMessage(inputContent)
                _aiResponse.value = AiResponseState.Success(response?.text ?: "Sorry, I couldn't generate a response.")

            } catch (e: Exception) {
                _aiResponse.value = AiResponseState.Error("⚠️ AI Error: ${e.message?.take(50) ?: "Unknown error"}")
                chat = null
            }
        }
    }

    fun clearResponseState() {
        _aiResponse.value = AiResponseState.Idle
    }
}