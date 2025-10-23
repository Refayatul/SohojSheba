package com.bonfire.shohojsheba.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.data.repositories.RepositoryProvider
import com.bonfire.shohojsheba.ui.components.ServiceRow
import com.bonfire.shohojsheba.ui.viewmodels.ServicesUiState
import com.bonfire.shohojsheba.ui.viewmodels.ServicesViewModel
import com.bonfire.shohojsheba.ui.viewmodels.ServicesViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceListScreen(
    navController: NavController, 
    category: String, 
    title: Int
) {
    val context = LocalContext.current
    val repository = RepositoryProvider.getRepository(context)
    val viewModel: ServicesViewModel = viewModel(
        key = category, // Ensures a new ViewModel instance for each category
        factory = ServicesViewModelFactory(repository, context)
    )

    // Load the data for the specific category when the screen is first composed.
    LaunchedEffect(category) {
        viewModel.loadServicesByCategory(category)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val uiState by viewModel.uiState.collectAsState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is ServicesUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ServicesUiState.Success -> {
                    if (state.services.isEmpty()) {
                        Text("No services found.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.services) { service ->
                                ServiceRow(service = service) {
                                    navController.navigate("service_detail/${service.id}")
                                }
                            }
                        }
                    }
                }
                is ServicesUiState.Error -> {
                    Text(state.message, modifier = Modifier.align(Alignment.Center))
                    Button(onClick = { viewModel.loadServicesByCategory(category) }) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}
