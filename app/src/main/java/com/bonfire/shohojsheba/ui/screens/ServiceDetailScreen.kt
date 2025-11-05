package com.bonfire.shohojsheba.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.data.repositories.RepositoryProvider
import com.bonfire.shohojsheba.ui.components.HtmlText
import com.bonfire.shohojsheba.ui.viewmodels.ServiceDetailUiState
import com.bonfire.shohojsheba.ui.viewmodels.ServiceDetailViewModel
import com.bonfire.shohojsheba.ui.viewmodels.ServiceDetailViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(navController: NavController, serviceId: String?) {
    if (serviceId == null) return

    val context = LocalContext.current
    val repository = RepositoryProvider.getRepository(context)
    val viewModel: ServiceDetailViewModel = viewModel(
        factory = ServiceDetailViewModelFactory(serviceId, repository)
    )

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val isFavorite = (uiState as? ServiceDetailUiState.Success)?.isFavorite == true
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is ServiceDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ServiceDetailUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(
                            text = context.getString(state.service.titleRes),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = context.getString(state.service.subtitleRes),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        state.serviceDetail?.let { detail ->
                            Log.d("DEBUG_IMAGE", "Stored imageRes in DB = ${detail.imageRes}")

                            Text(
                                "Instructions:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            HtmlText(html = detail.instructions)
                            Spacer(modifier = Modifier.height(16.dp))

                            // Load images dynamically
                            val imageResNames = detail.imageRes.split(",").map { it.trim() }
                            imageResNames.forEach { resName ->
                                val resId =
                                    context.resources.getIdentifier(resName, "drawable", context.packageName)
                                if (resId != 0) {
                                    AsyncImage(
                                        model = resId,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        onSuccess = { Log.d("DEBUG_IMAGE", "Loaded image successfully: $resName") },
                                        onError = { Log.d("DEBUG_IMAGE", "Failed to load image: $resName") }
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                } else {
                                    Log.d("DEBUG_IMAGE", "Drawable resource not found: $resName")
                                }
                            }

                            // YouTube Button
                            if (!detail.youtubeLink.isNullOrBlank()) {
                                Button(onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(detail.youtubeLink))
                                    context.startActivity(intent)
                                }) {
                                    Text("Watch on YouTube")
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            Text(
                                "Required Documents:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            HtmlText(html = detail.requiredDocuments)
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "Processing Time:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(detail.processingTime, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "Contact Info:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(detail.contactInfo, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                is ServiceDetailUiState.Error -> {
                    Text(state.message, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}
