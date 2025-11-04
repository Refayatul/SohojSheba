package com.bonfire.shohojsheba.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.ui.components.HtmlText
import com.bonfire.shohojsheba.data.repositories.RepositoryProvider
import com.bonfire.shohojsheba.ui.viewmodels.ServiceDetailUiState
import com.bonfire.shohojsheba.ui.viewmodels.ServiceDetailViewModel
import com.bonfire.shohojsheba.ui.viewmodels.ServiceDetailViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(navController: NavController, serviceId: String?) {
    if (serviceId == null) {
        // Handle error: serviceId is null
        return
    }

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
                            text = stringResource(id = state.service.titleRes),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = state.service.subtitleRes),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        state.serviceDetail?.let { detail ->
                            Text("Instructions:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            // Using the dynamic data from the ViewModel
                            HtmlText(html = detail.instructions)
                            Spacer(modifier = Modifier.height(16.dp))

                            val imageResNames = detail.imageRes.split(",").map { it.trim() }
                            imageResNames.forEach { resName ->
                                val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                                if (resId != 0) {
                                    Image(painter = painterResource(id = resId), contentDescription = null, modifier = Modifier.fillMaxWidth())
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }

                            if (!detail.youtubeLink.isNullOrBlank()) {
                                Button(onClick = { 
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(detail.youtubeLink))
                                    context.startActivity(intent)
                                }) {
                                    Text("Watch on YouTube")
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            Text("Required Documents:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            // Using the dynamic data from the ViewModel
                            HtmlText(html = detail.requiredDocuments)
                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Processing Time:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(detail.processingTime, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Contact Info:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
