package com.bonfire.shohojsheba.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bonfire.shohojsheba.LocalLocale
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.navigation.Routes
import com.bonfire.shohojsheba.ui.components.DecorativeBackground
import com.bonfire.shohojsheba.ui.components.ServiceRow
import com.bonfire.shohojsheba.ui.viewmodels.ServicesUiState
import com.bonfire.shohojsheba.ui.viewmodels.ServicesViewModel
import com.bonfire.shohojsheba.ui.viewmodels.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceListScreen(
    navController: NavController,
    category: String,
    title: Int
) {
    val context = LocalContext.current
    val locale = LocalLocale.current
    val viewModel: ServicesViewModel = viewModel(
        key = category,
        factory = ViewModelFactory(context)
    )

    LaunchedEffect(key1 = Unit) {
        viewModel.toastMessage.collectLatest {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

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
                actions = {
                    val dataSource by viewModel.dataSource.collectAsState()
                    Text("Source: $dataSource", fontSize = 10.sp, modifier = Modifier.padding(end = 8.dp))
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        DecorativeBackground {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                val uiState by viewModel.uiState.collectAsState()

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
                                    ServiceRow(service = service, locale = locale) {
                                        navController.navigate("${Routes.SERVICE_DETAIL}/${service.id}")
                                    }
                                }
                            }
                        }
                    }
                    is ServicesUiState.Error -> {
                        Text(
                            text = state.message,
                            modifier = Modifier.align(Alignment.Center)
                        )
                        Button(
                            onClick = { viewModel.loadServicesByCategory(category) },
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}
