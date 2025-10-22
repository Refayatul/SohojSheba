package com.bonfire.shohojsheba.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Agriculture
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.data.repositories.RepositoryProvider
import com.bonfire.shohojsheba.ui.components.CategoryCard
import com.bonfire.shohojsheba.ui.components.ServiceListItem
import com.bonfire.shohojsheba.ui.components.ServiceRow
import com.bonfire.shohojsheba.ui.theme.*
import com.bonfire.shohojsheba.ui.viewmodels.ServicesUiState
import com.bonfire.shohojsheba.ui.viewmodels.ServicesViewModel
import com.bonfire.shohojsheba.ui.viewmodels.ServicesViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = RepositoryProvider.getRepository(context)
    val viewModel: ServicesViewModel = viewModel(
        factory = ServicesViewModelFactory(repository, context)
    )

    val searchQuery by viewModel.searchQuery.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold) },
                actions = { IconButton(onClick = { navController.navigate("settings") }) { Icon(imageVector = Icons.Outlined.Settings, contentDescription = stringResource(id = R.string.settings), tint = MaterialTheme.colorScheme.secondary) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                shape = RoundedCornerShape(50),
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.secondary) },
                placeholder = { Text(text = stringResource(id = R.string.search_hint), color = MaterialTheme.colorScheme.secondary) },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, disabledIndicatorColor = Color.Transparent,
                    unfocusedContainerColor = SearchBarBackground, focusedContainerColor = SearchBarBackground
                )
            )

            if (searchQuery.isBlank()) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(text = stringResource(id = R.string.service_categories), color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            CategoryCard(modifier = Modifier.weight(1f), title = stringResource(id = R.string.category_citizen), icon = Icons.Outlined.Person, iconBackgroundColor = IconBgLightGreen, iconTintColor = IconTintDarkGreen, onClick = { navController.navigate("citizen_services") })
                            CategoryCard(modifier = Modifier.weight(1f), title = stringResource(id = R.string.category_farmer), icon = Icons.Outlined.Agriculture, iconBackgroundColor = IconBgLightBlue, iconTintColor = IconTintDarkBlue, onClick = { navController.navigate("farmer_services") })
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            CategoryCard(modifier = Modifier.weight(1f), title = stringResource(id = R.string.category_entrepreneur), icon = Icons.Outlined.Storefront, iconBackgroundColor = IconBgLightPurple, iconTintColor = IconTintDarkPurple, onClick = { navController.navigate("entrepreneur_services") })
                            CategoryCard(modifier = Modifier.weight(1f), title = stringResource(id = R.string.category_govt_office), icon = Icons.Outlined.Apartment, iconBackgroundColor = IconBgLightYellow, iconTintColor = IconTintDarkYellow, onClick = { navController.navigate("govt_office_services") })
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ServiceListItem(title = stringResource(id = R.string.recent_services_title), subtitle = stringResource(id = R.string.recent_services_subtitle), onClick = { navController.navigate("history") })
                        ServiceListItem(title = stringResource(id = R.string.favorite_services_title), subtitle = stringResource(id = R.string.favorite_services_subtitle), onClick = { navController.navigate("favorites") })
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                when(val state = uiState) {
                    is ServicesUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                    is ServicesUiState.Success -> {
                        if (state.services.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize().padding(top = 32.dp), contentAlignment = Alignment.TopCenter) {
                                Text("No services found for \"$searchQuery\"")
                            }
                        } else {
                            LazyColumn(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(state.services) { service ->
                                    ServiceRow(service = service) {
                                        navController.navigate("service_detail/${service.id}")
                                    }
                                }
                            }
                        }
                    }
                    is ServicesUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(state.message, modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }
            }
        }
    }
}
