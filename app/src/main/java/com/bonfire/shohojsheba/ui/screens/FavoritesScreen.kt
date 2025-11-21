package com.bonfire.shohojsheba.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.navigation.NavController
import com.bonfire.shohojsheba.LocalLocale
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.ui.components.ServiceRow
import com.bonfire.shohojsheba.ui.viewmodels.ServicesViewModel
import com.bonfire.shohojsheba.ui.viewmodels.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(navController: NavController) {
    val context = LocalContext.current
    val locale = LocalLocale.current
    val viewModel: ServicesViewModel = viewModel(
        factory = ViewModelFactory(context)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.favorites_page_title), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val allServices by viewModel.allServices.collectAsState(initial = emptyList())
        val favorites by viewModel.favorites.collectAsState(initial = emptyList())

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                            navController.navigate("service_detail/${service.id}")
                        }
                    }
                }
            }
        }
    }
}