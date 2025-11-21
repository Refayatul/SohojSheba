package com.bonfire.shohojsheba.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background // Added this just in case
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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.ui.viewmodels.AiResponseState
import com.bonfire.shohojsheba.ui.viewmodels.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean,
    val attachmentName: String? = null,
    val isImage: Boolean = false
)

// --- HELPERS ---

fun Context.findActivity(): ComponentActivity? {
    return when (this) {
        is ComponentActivity -> this
        is ContextWrapper -> this.baseContext.findActivity()
        else -> null
    }
}

fun getFileName(context: Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    result = cursor.getString(index)
                }
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result ?: "attachment"
}

suspend fun uriToBytes(context: Context, uri: Uri): ByteArray? {
    return withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (_: Exception) {
            null
        }
    }
}

suspend fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (_: Exception) {
            null
        }
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
    val activity = remember(context, view) { context.findActivity() ?: view.context.findActivity() }

    if (activity != null) {
        CompositionLocalProvider(LocalActivityResultRegistryOwner provides activity) {
            ChatScreenContent(navController, viewModel)
        }
    } else {
        ChatScreenContent(navController, viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatScreenContent(
    navController: NavController,
    viewModel: ChatViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var textInput by remember { mutableStateOf("") }

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var isImageAttachment by remember { mutableStateOf(false) }

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedPdfBytes by remember { mutableStateOf<ByteArray?>(null) }

    val listState = rememberLazyListState()
    val aiState by viewModel.aiResponse.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (spokenText != null) {
                textInput = spokenText
            }
        }
    }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            selectedFileName = "Image"
            isImageAttachment = true

            scope.launch {
                selectedBitmap = uriToBitmap(context, uri)
                selectedPdfBytes = null
            }
        }
    }

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            selectedFileName = getFileName(context, uri)
            isImageAttachment = false

            scope.launch {
                selectedPdfBytes = uriToBytes(context, uri)
                selectedBitmap = null
            }
        }
    }

    val onVoiceInputClick: () -> Unit = {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
            }
            voiceLauncher.launch(intent)
        } catch (_: Exception) {
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
        if ((messageText.isNotBlank() || selectedUri != null) && aiState !is AiResponseState.Loading) {
            messages.add(ChatMessage(
                text = messageText,
                isFromUser = true,
                attachmentName = selectedFileName,
                isImage = isImageAttachment
            ))

            viewModel.searchWithAI(messageText, selectedBitmap, selectedPdfBytes)

            textInput = ""
            selectedUri = null
            selectedFileName = null
            selectedBitmap = null
            selectedPdfBytes = null
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
                            text = stringResource(id = R.string.chat_screen_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).imePadding()
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
                attachmentName = selectedFileName,
                isImage = isImageAttachment,
                onInputChange = { textInput = it },
                onSendClick = { onSendMessage(textInput) },
                onVoiceClick = onVoiceInputClick,
                onPickImage = {
                    imageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onPickPdf = {
                    pdfLauncher.launch("application/pdf")
                },
                onRemoveAttachment = {
                    selectedUri = null
                    selectedFileName = null
                    selectedBitmap = null
                    selectedPdfBytes = null
                },
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
                    Icon(Icons.Filled.SmartToy, "AI", Modifier.padding(4.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Column {
                    if (message.attachmentName != null) {
                        Row(
                            modifier = Modifier.padding(top = 8.dp, start = 12.dp, end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val icon = if (message.isImage) Icons.Default.Image else Icons.Default.Description
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isUser) MaterialTheme.colorScheme.onPrimary.copy(0.8f) else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = message.attachmentName,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isUser) MaterialTheme.colorScheme.onPrimary.copy(0.8f) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    SelectionContainer {
                        Text(
                            text = message.text.ifBlank { if (message.isImage) "Sent an image" else "Sent a PDF" },
                            modifier = Modifier.padding(12.dp),
                            color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp)
                        )
                    }

                    if (!isUser) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.1f), thickness = 1.dp)
                        Row(Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.End) {
                            IconButton(onClick = { clipboardManager.setText(AnnotatedString(message.text)) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.ContentCopy, "Copy", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f))
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
    attachmentName: String?,
    isImage: Boolean,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onPickImage: () -> Unit,
    onPickPdf: () -> Unit,
    onRemoveAttachment: () -> Unit,
    isLoading: Boolean
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(tonalElevation = 2.dp, color = MaterialTheme.colorScheme.surface) {
        Column {
            if (attachmentName != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(50.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            val icon = if (isImage) Icons.Default.Image else Icons.Default.Description
                            Icon(icon, contentDescription = null)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = attachmentName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(text = stringResource(id = R.string.ready_to_send_status), style = MaterialTheme.typography.labelSmall)
                    }
                    IconButton(onClick = onRemoveAttachment) {
                        Icon(Icons.Default.Close, "Remove")
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.2f))
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    IconButton(onClick = { showMenu = true }, enabled = !isLoading) {
                        Icon(Icons.Default.AttachFile, "Attach", tint = MaterialTheme.colorScheme.secondary)
                    }
                    // --- FIX IS HERE: Used containerColor instead of Modifier.background ---
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        DropdownMenuItem(
                            text = { Text("Photo") },
                            leadingIcon = { Icon(Icons.Default.Image, null) },
                            onClick = {
                                showMenu = false
                                onPickImage()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("PDF Document") },
                            leadingIcon = { Icon(Icons.Default.Description, null) },
                            onClick = {
                                showMenu = false
                                onPickPdf()
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = input,
                    onValueChange = onInputChange,
                    modifier = Modifier.weight(1f).heightIn(min = 50.dp, max = 120.dp),
                    placeholder = { Text(stringResource(id = R.string.type_your_question)) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    enabled = !isLoading,
                    maxLines = 4,
                    trailingIcon = {
                        IconButton(onClick = onVoiceClick) {
                            Icon(Icons.Default.Mic, "Voice", tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilledIconButton(
                    onClick = onSendClick,
                    enabled = (input.isNotBlank() || attachmentName != null) && !isLoading,
                    modifier = Modifier.size(50.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, "Send")
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyChatState(onSuggestionClick: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.SmartToy, null, Modifier.size(80.dp).padding(bottom = 16.dp), tint = MaterialTheme.colorScheme.primary.copy(0.4f))
        Text(
            text = stringResource(id = R.string.chat_greeting),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(0.8f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        val suggestions = listOf(
            stringResource(id = R.string.sugg_passport),
            stringResource(id = R.string.sugg_trade_license),
            stringResource(id = R.string.sugg_birth_cert),
            stringResource(id = R.string.sugg_emergency)
        )
        WrapContent(suggestions, onSuggestionClick)
    }
}

@Composable
fun WrapContent(suggestions: List<String>, onClick: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        suggestions.forEach { text ->
            SuggestionChip(
                onClick = { onClick(text) },
                label = { Text(text) },
                colors = SuggestionChipDefaults.suggestionChipColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(0.3f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.3f))
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Filled.SmartToy, null, Modifier.padding(4.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text("Thinking...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
    }
}