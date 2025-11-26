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
import androidx.compose.material.icons.filled.Delete
import com.bonfire.shohojsheba.ui.components.EnhancedTopAppBar
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bonfire.shohojsheba.ui.components.DecorativeBackground
import androidx.navigation.NavController
import com.bonfire.shohojsheba.LocalLocale
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.navigation.Routes
import com.bonfire.shohojsheba.ui.components.ServiceRow
import com.bonfire.shohojsheba.ui.viewmodels.ServicesViewModel
import com.bonfire.shohojsheba.ui.viewmodels.ViewModelFactory
import java.util.Locale

/**
 * =========================================================================================
 *                                  FAVORITES SCREEN
 * =========================================================================================
 * 
 * HOW IT WORKS:
 * 1.  **Data Management**:
 *     -   Observes `allServices` (list of all available services) and `favorites` (list of saved IDs) from `ServicesViewModel`.
 *     -   Filters `allServices` to show only those whose IDs are present in the `favorites` list.
 * 
 * 2.  **UI Layout**:
 *     -   **Empty State**: Shows "No favorites yet" if the list is empty.
 *     -   **List View**: Uses `LazyColumn` to efficiently display the list of favorite services.
 *     -   **Service Row**: Reuses the `ServiceRow` component for consistent styling with other lists.
 * 
 * 3.  **Actions**:
 *     -   **Clear All**: Provides a trash icon in the top bar to remove all favorites at once.
 *     -   **Navigation**: Clicking a service navigates to its details page.
 * =========================================================================================
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(navController: NavController, locale: Locale) {
    val context = LocalContext.current
    val viewModel: ServicesViewModel = viewModel(
        factory = ViewModelFactory(context)
    )

    val allServices by viewModel.allServices.collectAsState(initial = emptyList())
    val favorites by viewModel.favorites.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            EnhancedTopAppBar(
                title = stringResource(id = R.string.favorites_page_title),
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = { navController.popBackStack() },
                actions = {
                    if (favorites.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearFavorites() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.clear_all_favorites_content_desc),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
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
                if (allServices.isEmpty() || favorites.isEmpty()) {
                    if (favorites.isEmpty()) {
                        Text("No favorites yet.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                } else {
                    val favoriteServiceIds = favorites.map { it.serviceId }.toSet()
                    val favoriteServices = allServices.filter { it.id in favoriteServiceIds }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(favoriteServices) { service ->
                            ServiceRow(service = service, locale = locale) {
                                navController.navigate("${Routes.SERVICE_DETAIL}/${service.id}")
                            }
                        }
                    }
                }
            }
        }
    }
}