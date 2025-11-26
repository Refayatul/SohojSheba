package com.bonfire.shohojsheba.ui.screens

import android.util.Log

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
import java.util.Locale

/**
 * =========================================================================================
 *                                SERVICE LIST SCREEN
 * =========================================================================================
 * 
 * HOW IT WORKS:
 * 1.  **Dynamic Data Loading**:
 *     -   Fetches services based on the `category` parameter (e.g., "citizen", "farmer").
 *     -   **Locale Awareness**: Re-fetches data whenever the `locale` changes to ensure the correct language is displayed.
 *     -   Uses a unique `viewModelKey` ("$category-${locale.language}") to ensure separate ViewModel instances for different categories/languages.
 * 
 * 2.  **State Management**:
 *     -   `uiState`: Tracks Loading, Success (with list of services), and Error states.
 *     -   `dataSource`: Debug info showing where data came from (e.g., "Cache", "Network").
 * 
 * 3.  **UI Layout**:
 *     -   **Loading**: Shows a centered `CircularProgressIndicator`.
 *     -   **Success**: Displays a `LazyColumn` of `ServiceRow` items.
 *     -   **Error**: Shows an error message and a "Retry" button.
 * 
 * 4.  **Navigation**:
 *     -   Clicking a service navigates to `ServiceDetailScreen` with the service ID.
 * =========================================================================================
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceListScreen(
    navController: NavController,
    category: String,
    title: Int,
    locale: Locale  // Add locale parameter
) {
    // DEBUG: Log when screen is composed
    Log.d("ServiceListScreen", "=== COMPOSING ServiceListScreen ===")
    Log.d("ServiceListScreen", "Category: $category")
    Log.d("ServiceListScreen", "Locale: ${locale.language} (${locale.displayLanguage})")
    
    val context = LocalContext.current
    val viewModelKey = "$category-${locale.language}"
    Log.d("ServiceListScreen", "ViewModel Key: $viewModelKey")
    
    val viewModel: ServicesViewModel = viewModel(
        key = viewModelKey,
        factory = ViewModelFactory(context)
    )
    
    Log.d("ServiceListScreen", "ViewModel instance: ${viewModel.hashCode()}")

    LaunchedEffect(key1 = Unit) {
        viewModel.toastMessage.collectLatest {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    // Re-fetch services when locale changes
    // 'LaunchedEffect' with keys (category, locale) means this code runs whenever category OR locale changes.
    // This ensures the list updates if the user switches language while on this screen.
    LaunchedEffect(category, locale) {
        Log.d("ServiceListScreen", "LaunchedEffect triggered - Category: $category, Locale: ${locale.language}")
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
                                // 'items' creates a row for each service in the list.
                                // 'key' is crucial for performance and correct animations. 
                                // It tells Compose which item is which, so it doesn't redraw everything when one item changes.
                                items(
                                    items = state.services,
                                    key = { it.id + locale.language }
                                ) { service ->
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
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }
        }
    }
}
