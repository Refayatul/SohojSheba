package com.bonfire.shohojsheba.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
                Spacer(modifier = Modifier.height(72.dp)) // 48dp button + 16dp padding + extra 8dp

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
                .background(Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(12.dp))
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
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
                .background(Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(12.dp))
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.Black
            )
        }
    }
}

@Composable
private fun ServiceDetailContent(service: Service, detail: ServiceDetail, locale: Locale) {
    val imageUrls = detail.images.split(",").filter { it.isNotBlank() }
    // Correctly split instructions into logical blocks
    val instructionBlocks = (if (locale.language == "bn") detail.instructions.bn else detail.instructions.en)
        .split("\n\n").filter { it.isNotBlank() }

    val stepCount = max(instructionBlocks.size, imageUrls.size)

    // Interleaved Instructions and Images
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        (0 until stepCount).forEach { index ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // --- IMAGE FIRST ---
                    if (index < imageUrls.size) {
                        AsyncImage(
                            model = imageUrls[index],
                            contentDescription = "Step ${index + 1} Image",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .shadow(2.dp, RoundedCornerShape(16.dp))
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // --- THEN ENTIRE INSTRUCTION BLOCK ---
                    if (index < instructionBlocks.size) {
                        Text(
                            text = instructionBlocks[index].trim(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 16.sp,
                                lineHeight = 24.sp
                            ),
                            fontWeight = FontWeight.Medium
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
            .background(Color.White, RoundedCornerShape(16.dp))
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Text("Required Documents", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(requiredDocuments, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))

        Text("Processing Time", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(processingTime, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))

        Text("Contact Info", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(contactInfo, style = MaterialTheme.typography.bodyMedium)
    }
}
