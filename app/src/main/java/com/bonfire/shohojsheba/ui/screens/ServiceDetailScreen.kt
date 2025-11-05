package com.bonfire.shohojsheba.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bonfire.shohojsheba.data.repositories.RepositoryProvider
import com.bonfire.shohojsheba.ui.viewmodels.ServiceDetailUiState
import com.bonfire.shohojsheba.ui.viewmodels.ServiceDetailViewModel
import com.bonfire.shohojsheba.ui.viewmodels.ServiceDetailViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    navController: NavController,
    serviceId: String
) {
    val context = LocalContext.current
    val repository = RepositoryProvider.getRepository(context)

    // Remember ViewModel only once to prevent flickering
    val viewModel: ServiceDetailViewModel = remember(serviceId) {
        ServiceDetailViewModelFactory(serviceId, repository)
            .create(ServiceDetailViewModel::class.java)
    }

    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is ServiceDetailUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ServiceDetailUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = (uiState as ServiceDetailUiState.Error).message)
            }
        }
        is ServiceDetailUiState.Success -> {
            val service = (uiState as ServiceDetailUiState.Success).service
            val detail = (uiState as ServiceDetailUiState.Success).serviceDetail
            val isFavorite = (uiState as ServiceDetailUiState.Success).isFavorite

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(text = context.getString(service.titleRes)) },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.toggleFavorite() }) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else LocalContentColor.current
                                )
                            }
                        }
                    )
                },
                content = { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                            .padding(paddingValues)
                    ) {
                        // Subtitle
                        Text(
                            text = context.getString(service.subtitleRes),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        detail?.let {
                            val images = it.imageRes.split(",")

                            images.forEachIndexed { index, imageName ->
                                val resId =
                                    context.resources.getIdentifier(imageName, "drawable", context.packageName)
                                if (resId != 0) {
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Show instruction per image
                                    val instructionText = it.instructions.split("\n").getOrNull(index)
                                        ?: it.instructions
                                    Text(
                                        text = instructionText,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }

                            Text(text = "Required Documents", style = MaterialTheme.typography.titleMedium)
                            Text(text = it.requiredDocuments, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(text = "Processing Time", style = MaterialTheme.typography.titleMedium)
                            Text(text = it.processingTime, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(text = "Contact Info", style = MaterialTheme.typography.titleMedium)
                            Text(text = it.contactInfo, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            )
        }
    }
}
