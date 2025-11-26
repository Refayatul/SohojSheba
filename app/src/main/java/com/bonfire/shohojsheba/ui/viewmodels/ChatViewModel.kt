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

// Sealed Class to represent the state of the AI response.
// This helps the UI know exactly what to show (Loading spinner, Error message, or Success text).
sealed class AiResponseState {
    object Idle : AiResponseState()      // Waiting for user input
    object Loading : AiResponseState()   // Thinking...
    data class Success(val responseText: String) : AiResponseState() // Got an answer!
    data class Error(val errorMessage: String) : AiResponseState()   // Something went wrong
}

class ChatViewModel : ViewModel() {

    // --- State Management ---
    // Tracks the current status of the AI request (Idle, Loading, Success, Error)
    private val _aiResponse = MutableStateFlow<AiResponseState>(AiResponseState.Idle)
    val aiResponse = _aiResponse.asStateFlow()

    // Chat Session: Keeps track of the conversation history
    // We store the 'Chat' object so the AI remembers previous messages in the session.
    private var chat: Chat? = null

    // --- AI Search Function ---
    // Handles Text, Image, and PDF inputs for multi-modal interaction
    fun searchWithAI(query: String, image: Bitmap? = null, pdfBytes: ByteArray? = null) {
        viewModelScope.launch {
            _aiResponse.value = AiResponseState.Loading // Show loading spinner
            
            // 1. Check API Key
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank()) {
                _aiResponse.value = AiResponseState.Error("⚠️ Gemini AI key not found.")
                return@launch
            }

            try {
                // 2. Initialize Chat Session (if not already started)
                if (chat == null) {
                    val generativeModel = GenerativeModel(
                        modelName = "gemini-2.5-flash-lite", // Fast and efficient model
                        apiKey = apiKey,
                        // System Instructions: Defines the AI's persona and behavior
                        // We tell it to be "Shohoj Sheba", a helpful government service assistant.
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

                // 3. Construct the Message Content
                // Gemini supports multi-modal input (Text + Image + PDF) in a single message
                val inputContent = content {
                    if (image != null) {
                        image(image) // Attach image if present
                    }
                    if (pdfBytes != null) {
                        // SEND PDF AS BLOB (Binary Large Object)
                        // This allows Gemini to read the PDF content directly
                        blob("application/pdf", pdfBytes)
                    }
                    text(query) // Attach the user's text query
                }

                // 4. Send to Gemini
                val response = chat?.sendMessage(inputContent)
                
                // 5. Update UI with Response
                _aiResponse.value = AiResponseState.Success(response?.text ?: "Sorry, I couldn't generate a response.")

            } catch (e: Exception) {
                // Handle errors (network issues, API quota, etc.)
                _aiResponse.value = AiResponseState.Error("⚠️ AI Error: ${e.message?.take(50) ?: "Unknown error"}")
                chat = null // Reset chat on error so we start fresh next time
            }
        }
    }

    fun clearResponseState() {
        _aiResponse.value = AiResponseState.Idle
    }
}