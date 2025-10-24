package com.bonfire.shohojsheba.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onVoiceSearchClick: () -> Unit
) {
    val context = LocalContext.current
    val repository = RepositoryProvider.getRepository(context)
    val viewModel: ServicesViewModel = viewModel(
        factory = ServicesViewModelFactory(repository, context)
    )

    val uiState by viewModel.uiState.collectAsState()
    val aiResponse by viewModel.aiResponse.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            viewModel.searchServices(searchQuery)
        } else {
            viewModel.clearSearch()
        }
    }

    LaunchedEffect(aiResponse) {
        if (aiResponse != null) {
            keyboardController?.hide()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // 🔍 Search bar with voice mic
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(50),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.secondary
                )
            },
            trailingIcon = {
                IconButton(onClick = onVoiceSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Search",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            },
            placeholder = {
                Text(
                    text = stringResource(id = R.string.search_hint),
                    color = MaterialTheme.colorScheme.secondary
                )
            },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                unfocusedContainerColor = SearchBarBackground,
                focusedContainerColor = SearchBarBackground
            )
        )

        if (searchQuery.isBlank()) {
            // 🏠 Default content (unchanged)
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = stringResource(id = R.string.service_categories),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CategoryCard(
                            modifier = Modifier.weight(1f),
                            title = stringResource(id = R.string.category_citizen),
                            icon = Icons.Outlined.Person,
                            iconBackgroundColor = IconBgLightGreen,
                            iconTintColor = IconTintDarkGreen,
                            onClick = { navController.navigate("citizen_services") }
                        )
                        CategoryCard(
                            modifier = Modifier.weight(1f),
                            title = stringResource(id = R.string.category_farmer),
                            icon = Icons.Outlined.Agriculture,
                            iconBackgroundColor = IconBgLightBlue,
                            iconTintColor = IconTintDarkBlue,
                            onClick = { navController.navigate("farmer_services") }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CategoryCard(
                            modifier = Modifier.weight(1f),
                            title = stringResource(id = R.string.category_entrepreneur),
                            icon = Icons.Outlined.Storefront,
                            iconBackgroundColor = IconBgLightPurple,
                            iconTintColor = IconTintDarkPurple,
                            onClick = { navController.navigate("entrepreneur_services") }
                        )
                        CategoryCard(
                            modifier = Modifier.weight(1f),
                            title = stringResource(id = R.string.category_govt_office),
                            icon = Icons.Outlined.Apartment,
                            iconBackgroundColor = IconBgLightYellow,
                            iconTintColor = IconTintDarkYellow,
                            onClick = { navController.navigate("govt_office_services") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ServiceListItem(
                        title = stringResource(id = R.string.recent_services_title),
                        subtitle = stringResource(id = R.string.recent_services_subtitle),
                        onClick = { navController.navigate("history") }
                    )
                    ServiceListItem(
                        title = stringResource(id = R.string.favorite_services_title),
                        subtitle = stringResource(id = R.string.favorite_services_subtitle),
                        onClick = { navController.navigate("favorites") }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            when (val state = uiState) {
                is ServicesUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }

                is ServicesUiState.Success -> {
                    if (state.services.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 32.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            aiResponse?.let {
                                Text(
                                    text = it,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "AI-generated. Verify critical info with official sources.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                            } ?: run {
                                Text("No services found for \"$searchQuery\"")
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(onClick = { viewModel.searchWithAI(searchQuery) }) {
                                    Text("Search with AI")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.padding(top = 16.dp),
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
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = state.message,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}
