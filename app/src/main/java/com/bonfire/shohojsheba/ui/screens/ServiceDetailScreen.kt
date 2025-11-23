package com.bonfire.shohojsheba.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
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
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    navController: NavController,
    serviceId: String
) {
    val context = LocalContext.current
    val locale = LocalLocale.current
    val viewModel: ServiceDetailViewModel = viewModel(
        factory = ViewModelFactory(context, serviceId = serviceId)
    )

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
                contentDescription = "Back",
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
                contentDescription = "Favorite",
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (subtitle.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun ServiceDetailContent(service: Service, detail: ServiceDetail, locale: Locale) {
    val imageUrls = detail.images.split(",").filter { it.isNotBlank() }
    val instructionBlocks = (if (locale.language == "bn") detail.instructions.bn else detail.instructions.en)
        .split("\n\n").filter { it.isNotBlank() }

    val stepCount = max(instructionBlocks.size, imageUrls.size)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        (0 until stepCount).forEach { index ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(2.dp, RoundedCornerShape(16.dp)), // Reduced shadow for cleaner look
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    if (index < imageUrls.size) {
                        AsyncImage(
                            model = imageUrls[index],
                            contentDescription = "Step ${index + 1} Image",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (index < instructionBlocks.size) {
                        val blockText = instructionBlocks[index].trim()
                        val annotatedText = buildAnnotatedString {
                            val stepKeyword = if (locale.language == "bn") "ধাপ" else "Step"
                            val delimiter = "—"
                            val stepRegex = Regex("^($stepKeyword\\s*\\d+\\s*[$delimiter:-])") // Added colon and hyphen support
                            val match = stepRegex.find(blockText)

                            if (match != null) {
                                val stepHeader = match.value
                                val restOfText = blockText.substring(stepHeader.length)
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)) {
                                    append(stepHeader)
                                }
                                appendMarkdown(restOfText)
                            } else {
                                appendMarkdown(blockText)
                            }
                        }
                        Text(
                            text = annotatedText,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge.copy( // Increased text size for readability
                                lineHeight = 26.sp
                            )
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Bottom Info Section
    val requiredDocuments = if (locale.language == "bn") detail.requiredDocuments.bn else detail.requiredDocuments.en
    val processingTime = if (locale.language == "bn") detail.processingTime.bn else detail.processingTime.en
    val contactInfo = if (locale.language == "bn") detail.contactInfo.bn else detail.contactInfo.en

    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp) // Reduced vertical padding as items have own padding
    ) {
        InfoRow(
            icon = Icons.Default.Description,
            title = "Required Documents",
            content = requiredDocuments
        )
        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
        
        InfoRow(
            icon = Icons.Default.Schedule,
            title = "Processing Time",
            content = processingTime
        )
        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
        
        InfoRow(
            icon = Icons.Default.Phone,
            title = "Contact Info",
            content = contactInfo
        )
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    title: String,
    content: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            // Parse Markdown for content here too
            val annotatedContent = buildAnnotatedString {
                 appendMarkdown(content)
            }
            
            Text(
                text = annotatedContent,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
        }
    }
}

// Helper function to parse Markdown bold (**text**)
fun AnnotatedString.Builder.appendMarkdown(text: String) {
    val parts = text.split("**")
    parts.forEachIndexed { index, part ->
        if (index % 2 == 1) { // Inside ** **
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Black)) { // Ensure bold text is distinct
                // Check for dark mode color adjustment if needed, but usually bold is enough.
                // Let's use current content color logic but just bold.
            }
            // Actually, span style inherits color unless specified. 
            // Let's just force bold.
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(part)
            }
        } else {
            append(part)
        }
    }
}
