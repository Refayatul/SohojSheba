package com.bonfire.shohojsheba.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.ui.viewmodels.AiResponseState
import com.bonfire.shohojsheba.ui.viewmodels.ChatViewModel

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean
)

// --- HELPER: Recursive Activity Finder ---
fun Context.findActivity(): ComponentActivity? {
    return when (this) {
        is ComponentActivity -> this
        is ContextWrapper -> this.baseContext.findActivity()
        else -> null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = viewModel()
) {
    val context = LocalContext.current
    val view = LocalView.current

    // Try to find the Activity using both Context and View
    val activity = remember(context, view) {
        context.findActivity() ?: view.context.findActivity()
    }

    if (activity != null) {
        CompositionLocalProvider(LocalActivityResultRegistryOwner provides activity) {
            ChatScreenContent(navController, viewModel)
        }
    } else {
        ChatScreenContent(navController, viewModel)
    }
}

// --- ADDED OptIn HERE TO FIX THE ERROR ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatScreenContent(
    navController: NavController,
    viewModel: ChatViewModel
) {
    val context = LocalContext.current
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val aiState by viewModel.aiResponse.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            results?.firstOrNull()?.let { spokenText ->
                textInput = spokenText
            }
        }
    }

    val onVoiceInputClick: () -> Unit = {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
            }
            voiceLauncher.launch(intent)
        } catch (_: Exception) { // Changed 'e' to '_' to ignore unused warning
            Toast.makeText(context, "Voice input not available", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(messages.size, aiState) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(aiState) {
        when (val state = aiState) {
            is AiResponseState.Success -> {
                messages.add(ChatMessage(text = state.responseText, isFromUser = false))
                viewModel.clearResponseState()
            }
            is AiResponseState.Error -> {
                messages.add(ChatMessage(text = state.errorMessage, isFromUser = false))
                viewModel.clearResponseState()
            }
            else -> {}
        }
    }

    val onSendMessage: (String) -> Unit = { messageText ->
        if (messageText.isNotBlank() && aiState !is AiResponseState.Loading) {
            messages.add(ChatMessage(text = messageText, isFromUser = true))
            viewModel.searchWithAI(messageText)
            textInput = ""
            keyboardController?.hide()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.SmartToy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.ai_assistant),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (messages.isEmpty()) {
                    EmptyChatState(onSuggestionClick = { onSendMessage(it) })
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(messages, key = { it.id }) { message ->
                            ChatBubble(message = message)
                        }
                        if (aiState is AiResponseState.Loading) {
                            item { TypingIndicator() }
                        }
                    }
                }
            }

            ChatInputBar(
                input = textInput,
                onInputChange = { textInput = it },
                onSendClick = { onSendMessage(textInput) },
                onVoiceClick = onVoiceInputClick,
                isLoading = aiState is AiResponseState.Loading
            )
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.isFromUser
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            if (!isUser) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SmartToy,
                        contentDescription = "AI",
                        modifier = Modifier.padding(4.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Column {
                    SelectionContainer {
                        Text(
                            text = message.text,
                            modifier = Modifier.padding(12.dp),
                            color = if (isUser)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp)
                        )
                    }

                    if (!isUser) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                            thickness = 1.dp
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(message.text))
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatInputBar(
    input: String,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onVoiceClick: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 50.dp, max = 120.dp),
                placeholder = { Text(stringResource(id = R.string.type_your_question)) },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                enabled = !isLoading,
                maxLines = 4,
                trailingIcon = {
                    IconButton(onClick = onVoiceClick) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            FilledIconButton(
                onClick = onSendClick,
                enabled = input.isNotBlank() && !isLoading,
                modifier = Modifier.size(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send"
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyChatState(onSuggestionClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.SmartToy,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
        Text(
            text = "How can I help you today?",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        val suggestions = listOf(
            "Renew Passport",
            "Trade License Fee",
            "Birth Certificate",
            "Emergency Numbers"
        )

        WrapContent(suggestions, onSuggestionClick)
    }
}

@Composable
fun WrapContent(suggestions: List<String>, onClick: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        suggestions.forEach { text ->
            SuggestionChip(
                onClick = { onClick(text) },
                label = { Text(text) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.SmartToy,
                contentDescription = null,
                modifier = Modifier.padding(4.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Thinking...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )
    }
}