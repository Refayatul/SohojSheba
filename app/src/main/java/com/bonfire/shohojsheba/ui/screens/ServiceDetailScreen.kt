package com.bonfire.shohojsheba.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.bonfire.shohojsheba.R
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bonfire.shohojsheba.LocalLocale
import com.bonfire.shohojsheba.data.database.entities.Service
import com.bonfire.shohojsheba.data.database.entities.ServiceDetail
import com.bonfire.shohojsheba.data.database.entities.UserFavorite
import com.bonfire.shohojsheba.ui.viewmodels.ServiceDetailViewModel
import com.bonfire.shohojsheba.ui.viewmodels.ViewModelFactory
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.regex.Pattern
import kotlin.math.max

/**
 * =========================================================================================
 *                                SERVICE DETAIL SCREEN
 * =========================================================================================
 * 
 * HOW IT WORKS:
 * 1.  **Data Loading**:
 *     -   Fetches `Service` (basic info) and `ServiceDetail` (instructions, docs) by ID.
 *     -   Checks if the service is in the user's favorites list.
 *     -   **Locale Aware**: Fetches and displays content in the selected language (English/Bangla).
 * 
 * 2.  **Rich Content Rendering**:
 *     -   **Step-by-Step Instructions**: Splits the instruction text into paragraphs and renders them as cards.
 *     -   **Images**: Loads step-specific images using Coil (`AsyncImage`) if available.
 *     -   **Markdown Support**: Custom logic (`appendMarkdownAndLinks`) parses `**bold**` text and `[Link](url)` patterns.
 *     -   **Auto-Linking**: Automatically detects and links URLs, Email addresses, and Phone numbers.
 * 
 * 3.  **Interaction**:
 *     -   **Favorites**: Floating action button to toggle favorite status.
 *     -   **Clickable Text**: Users can tap on links, emails, or phone numbers to open the respective apps.
 * 
 * 4.  **UI Layout**:
 *     -   **Header**: Title and subtitle.
 *     -   **Body**: List of instruction cards.
 *     -   **Footer**: Required documents, processing time, and contact info.
 * =========================================================================================
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    navController: NavController,
    serviceId: String,
    locale: Locale  // Add locale parameter
) {
    // DEBUG: Log when screen is composed
    Log.d("ServiceDetailScreen", "=== COMPOSING ServiceDetailScreen ===")
    Log.d("ServiceDetailScreen", "Service ID: $serviceId")
    
    val context = LocalContext.current
    
   Log.d("ServiceDetailScreen", "Locale: ${locale.language} (${locale.displayLanguage})")
    
    val viewModelKey = "$serviceId-${locale.language}"
    Log.d("ServiceDetailScreen", "ViewModel Key: $viewModelKey")
    
    val viewModel: ServiceDetailViewModel = viewModel(
        key = viewModelKey,
        factory = ViewModelFactory(context, serviceId = serviceId)
    )
    
    Log.d("ServiceDetailScreen", "ViewModel instance: ${viewModel.hashCode()}")

    val service by viewModel.service.collectAsState(initial = null)
    val detail by viewModel.serviceDetail.collectAsState(initial = null)
    val isFavorite by viewModel.isFavorite.collectAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize()) {
        if (service != null && detail != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Top spacer to avoid overlapping with floating buttons
                Spacer(modifier = Modifier.height(80.dp))

                // Header
                ServiceHeader(service!!, locale)

                ServiceDetailContent(service!!, detail!!, locale)

                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // Floating back button
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(16.dp)
                .size(48.dp)
                .align(Alignment.TopStart)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), shape = RoundedCornerShape(12.dp))
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back_content_desc),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // Floating favorite button
        IconButton(
            onClick = {
                coroutineScope.launch {
                    if(isFavorite){
                        viewModel.removeFavorite(serviceId)
                    } else {
                        viewModel.addFavorite(UserFavorite(serviceId = serviceId, addedDate = System.currentTimeMillis()))
                    }
                }
            },
            modifier = Modifier
                .padding(16.dp)
                .size(48.dp)
                .align(Alignment.TopEnd)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), shape = RoundedCornerShape(12.dp))
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))

        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = stringResource(R.string.favorite_content_desc),
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ServiceHeader(service: Service, locale: Locale) {
    val title = if (locale.language == "bn") service.title.bn else service.title.en
    val subtitle = if (locale.language == "bn") service.subtitle.bn else service.subtitle.en

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        if (subtitle.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.15.sp
                ),
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun ServiceDetailContent(service: Service, detail: ServiceDetail, locale: Locale) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    // We split the instructions into blocks (paragraphs) and filter out empty ones.
    // We also split the image URLs.
    // 'max' ensures we loop enough times to show all text blocks OR all images, whichever is longer.
    val imageUrls = detail.images.split(",").filter { it.isNotBlank() }
    
    // 2. Split instructions into paragraphs (double newline)
    // This allows us to render each step as a separate card or block
    val instructionBlocks = (if (locale.language == "bn") detail.instructions.bn else detail.instructions.en)
        .split("\n\n").filter { it.isNotBlank() }

    // Determine total items to render (whichever list is longer)
    val stepCount = max(instructionBlocks.size, imageUrls.size)

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) { // Increased spacing
        (0 until stepCount).forEach { index ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp) // Increased internal padding
                ) {
                    if (index < imageUrls.size) {
                        AsyncImage(
                            model = imageUrls[index],
                            contentDescription = stringResource(R.string.step_image_desc, index + 1),
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight() // Allow full height
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // --- Text Rendering with Markdown & Links ---
                    if (index < instructionBlocks.size) {
                        val blockText = instructionBlocks[index].trim()
                        
                        // Build a rich text string that supports Bold (**text**) and Links
                        val annotatedText = buildAnnotatedString {
                            // Special handling for "Step X" headers to make them bold and colored
                            val stepKeyword = stringResource(R.string.step_label)
                            val delimiter = "—"
                            val stepRegex = Regex("^($stepKeyword\\s*\\d+\\s*[$delimiter:-])")
                            val match = stepRegex.find(blockText)

                            if (match != null) {
                                val stepHeader = match.value
                                val restOfText = blockText.substring(stepHeader.length)
                                // Style the header (e.g., "Step 1:")
                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    append(stepHeader)
                                }
                                append("\n\n") // Add spacing after header
                                appendMarkdownAndLinks(restOfText.trim())
                            } else {
                                appendMarkdownAndLinks(blockText)
                            }
                        }
                        
                        // 'ClickableText' allows us to detect clicks on specific parts of the text (like links).
                        // We use 'annotatedText' which contains the styling and the URL tags.
                        ClickableText(
                            text = annotatedText,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                                lineHeight = 32.sp, // Increased line height for readability
                                letterSpacing = 0.5.sp
                            ),
                            onClick = { offset ->
                                // When clicked, we check if the click was on a URL, Email, or Phone number
                                annotatedText.handleLinkClick(offset, context, uriHandler)
                            }
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Bottom Info Section
    val requiredDocuments = if (locale.language == "bn") detail.requiredDocuments.bn else detail.requiredDocuments.en
    val processingTime = if (locale.language == "bn") detail.processingTime.bn else detail.processingTime.en
    val contactInfo = if (locale.language == "bn") detail.contactInfo.bn else detail.contactInfo.en

    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            )
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
            .padding(vertical = 8.dp)
    ) {
        InfoRow(
            icon = Icons.Default.Description,
            title = stringResource(R.string.required_documents),
            content = requiredDocuments,
            iconColor = Color(0xFF8E24AA), // Purple
            iconBg = Color(0xFFF3E5F5)
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        )
        
        InfoRow(
            icon = Icons.Default.Schedule,
            title = stringResource(R.string.processing_time),
            content = processingTime,
            iconColor = Color(0xFFF9A825), // Yellow/Orange
            iconBg = Color(0xFFFFFDE7)
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        )
        
        InfoRow(
            icon = Icons.Default.Phone,
            title = stringResource(R.string.contact_info),
            content = contactInfo,
            iconColor = Color(0xFF2E7D32), // Green
            iconBg = Color(0xFFE8F5E9)
        )
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    title: String,
    content: String,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    iconBg: Color = MaterialTheme.colorScheme.primaryContainer
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(iconBg, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(20.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Parse Markdown and Links for content
            val annotatedContent = buildAnnotatedString {
                 appendMarkdownAndLinks(content)
            }
            
            ClickableText(
                text = annotatedContent,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                ),
                onClick = { offset ->
                    annotatedContent.handleLinkClick(offset, context, uriHandler)
                }
            )
        }
    }
}

// --- Link Click Handler ---
// Checks if the clicked character offset corresponds to a URL, Email, or Phone annotation
fun AnnotatedString.handleLinkClick(offset: Int, context: Context, uriHandler: UriHandler) {
    // 1. Check for URL
    getStringAnnotations(tag = "URL", start = offset, end = offset).firstOrNull()?.let { 
        try { uriHandler.openUri(it.item) } catch(e: Exception) { e.printStackTrace() }
    }
    // 2. Check for Email (mailto:)
    getStringAnnotations(tag = "EMAIL", start = offset, end = offset).firstOrNull()?.let { 
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${it.item}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch(e: Exception) { 
            Toast.makeText(context, context.getString(R.string.no_email_app), Toast.LENGTH_SHORT).show()
            e.printStackTrace() 
        }
    }
    // 3. Check for Phone (tel:)
    getStringAnnotations(tag = "PHONE", start = offset, end = offset).firstOrNull()?.let { 
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${it.item}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch(e: Exception) { 
            Toast.makeText(context, context.getString(R.string.no_dialer_app), Toast.LENGTH_SHORT).show()
            e.printStackTrace() 
        }
    }
}

// Helper to append text with markdown bold and auto-linking
fun AnnotatedString.Builder.appendMarkdownAndLinks(text: String) {
    val parts = text.split("**")
    parts.forEachIndexed { index, part ->
        val isBold = index % 2 == 1
        appendWithLinks(part, isBold)
    }
}

fun AnnotatedString.Builder.appendWithLinks(text: String, isBold: Boolean) {
    val urlMatcher = Patterns.WEB_URL.matcher(text)
    val emailMatcher = Patterns.EMAIL_ADDRESS.matcher(text)
    val phoneMatcher = Patterns.PHONE.matcher(text)
    // Regex for Markdown links: [Title](URL)
    val markdownLinkMatcher = Pattern.compile("\\[([^]]+)\\]\\(([^)]+)\\)").matcher(text)

    data class LinkMatch(val start: Int, val end: Int, val type: String, val value: String, val displayText: String? = null)
    val matches = mutableListOf<LinkMatch>()

    while (markdownLinkMatcher.find()) {
        matches.add(LinkMatch(
            start = markdownLinkMatcher.start(), 
            end = markdownLinkMatcher.end(), 
            type = "URL", 
            value = markdownLinkMatcher.group(2) ?: "",
            displayText = markdownLinkMatcher.group(1)
        ))
    }
    while (urlMatcher.find()) {
        matches.add(LinkMatch(urlMatcher.start(), urlMatcher.end(), "URL", urlMatcher.group()))
    }
    while (emailMatcher.find()) {
        matches.add(LinkMatch(emailMatcher.start(), emailMatcher.end(), "EMAIL", emailMatcher.group()))
    }
    while (phoneMatcher.find()) {
        val phone = phoneMatcher.group()
        if (phone.length >= 6) { 
             matches.add(LinkMatch(phoneMatcher.start(), phoneMatcher.end(), "PHONE", phone))
        }
    }

    matches.sortBy { it.start }
    
    val uniqueMatches = mutableListOf<LinkMatch>()
    var lastEnd = 0
    for (m in matches) {
        if (m.start >= lastEnd) {
            uniqueMatches.add(m)
            lastEnd = m.end
        }
    }

    var currentIndex = 0
    for (match in uniqueMatches) {
        if (match.start > currentIndex) {
             val plainText = text.substring(currentIndex, match.start)
             if (isBold) {
                 withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(plainText) }
             } else {
                 append(plainText)
             }
        }
        
        // Use displayText for Markdown links, otherwise substring
        val linkText = match.displayText ?: text.substring(match.start, match.end)
        
        pushStringAnnotation(tag = match.type, annotation = match.value)
        withStyle(SpanStyle(color = Color(0xFF1E88E5), textDecoration = TextDecoration.Underline, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)) {
            append(linkText)
        }
        pop()
        
        currentIndex = match.end
    }
    
    if (currentIndex < text.length) {
        val remaining = text.substring(currentIndex)
        if (isBold) {
             withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(remaining) }
         } else {
             append(remaining)
         }
    }
}
