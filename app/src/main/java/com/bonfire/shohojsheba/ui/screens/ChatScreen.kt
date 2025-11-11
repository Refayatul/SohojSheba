package com.bonfire.shohojsheba.ui.screens



import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.ui.viewmodels.AiResponseState
import com.bonfire.shohojsheba.ui.viewmodels.ChatViewModel
import kotlinx.coroutines.launch






data class ChatMessage(val text: String, val isFromUser: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable


fun ChatScreen(navController: NavController,
               viewModel: ChatViewModel = viewModel()) {
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val aiState by viewModel.aiResponse.collectAsState()


    LaunchedEffect(aiState) {
        when (val state = aiState) {
            is AiResponseState.Success -> {
                messages.add(ChatMessage(state.responseText, isFromUser = false))
                viewModel.clearResponseState() // Reset state to prevent re-adding
            }
            is AiResponseState.Error -> {
                messages.add(ChatMessage(state.errorMessage, isFromUser = false))
                viewModel.clearResponseState() // Reset state
            }
            else -> { /* Do nothing for Idle or Loading here */ }
        }

        // Auto-scroll to the new message
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val onSendMessage: (String) -> Unit = { messageText ->
        if (messageText.isNotBlank() && aiState !is AiResponseState.Loading) {
            messages.add(ChatMessage(messageText, isFromUser = true))
            // --- THIS IS THE KEY ---
            // Trigger the AI logic in the ViewModel
            viewModel.searchWithAI(messageText)
            textInput = ""
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.ai_assistant)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages) { msg ->
                    // You can customize the chat bubble appearance here later
                    Text(text = msg.text, modifier = Modifier.fillMaxWidth().padding(8.dp))
                }

                // Show a loading indicator while the AI is thinking
                if (aiState is AiResponseState.Loading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }


            // Input Field and Send Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(id = R.string.type_your_question)) },
                    // Disable input while loading
                    enabled = aiState !is AiResponseState.Loading
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { onSendMessage(textInput) },
                    // Disable button while loading
                    enabled = aiState !is AiResponseState.Loading
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}