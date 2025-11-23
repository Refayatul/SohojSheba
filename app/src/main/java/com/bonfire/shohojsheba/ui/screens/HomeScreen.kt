package com.bonfire.shohojsheba.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bonfire.shohojsheba.LocalLocale
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.navigation.Routes
import com.bonfire.shohojsheba.ui.components.CategoryCard
import com.bonfire.shohojsheba.ui.components.ServiceListItem
import com.bonfire.shohojsheba.ui.components.ServiceRow
import com.bonfire.shohojsheba.ui.theme.*
import com.bonfire.shohojsheba.ui.viewmodels.ServicesUiState
import com.bonfire.shohojsheba.ui.viewmodels.ServicesViewModel
import com.bonfire.shohojsheba.ui.viewmodels.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onVoiceSearchClick: () -> Unit
) {
    val context = LocalContext.current
    val appScope = rememberCoroutineScope()
    val locale = LocalLocale.current
    val viewModel: ServicesViewModel = viewModel(
        factory = ViewModelFactory(context, appScope = appScope)
    )

    val uiState by viewModel.uiState.collectAsState()
    val aiResponse by viewModel.aiResponse.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Listen for toast messages
    LaunchedEffect(key1 = Unit) {
        viewModel.toastMessage.collectLatest {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
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
            onValueChange = { 
                onSearchQueryChange(it)
                if (it.isBlank()) {
                    viewModel.clearSearch()
                } else {
                    // Local search while typing (enableAI = false)
                    viewModel.searchServices(it, enableAI = false)
                }
            },
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
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (searchQuery.isNotBlank()) {
                        // AI search on Enter (enableAI = true)
                        viewModel.searchServices(searchQuery, enableAI = true)
                        keyboardController?.hide()
                    }
                }
            ),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        if (searchQuery.isBlank()) {
            // 🏠 Default content (Categories)
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
                            onClick = { navController.navigate(Routes.CITIZEN_SERVICES) }
                        )
                        CategoryCard(
                            modifier = Modifier.weight(1f),
                            title = stringResource(id = R.string.category_farmer),
                            icon = Icons.Outlined.Agriculture,
                            iconBackgroundColor = IconBgLightBlue,
                            iconTintColor = IconTintDarkBlue,
                            onClick = { navController.navigate(Routes.FARMER_SERVICES) }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CategoryCard(
                            modifier = Modifier.weight(1f),
                            title = stringResource(id = R.string.category_entrepreneur),
                            icon = Icons.Outlined.Storefront,
                            iconBackgroundColor = IconBgLightPurple,
                            iconTintColor = IconTintDarkPurple,
                            onClick = { navController.navigate(Routes.ENTREPRENEUR_SERVICES) }
                        )
                        CategoryCard(
                            modifier = Modifier.weight(1f),
                            title = stringResource(id = R.string.category_govt_office),
                            icon = Icons.Outlined.Apartment,
                            iconBackgroundColor = IconBgLightYellow,
                            iconTintColor = IconTintDarkYellow,
                            onClick = { navController.navigate(Routes.GOVT_OFFICE_SERVICES) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ServiceListItem(
                        title = stringResource(id = R.string.recent_services_title),
                        subtitle = stringResource(id = R.string.recent_services_subtitle),
                        onClick = { navController.navigate(Routes.HISTORY) }
                    )
                    ServiceListItem(
                        title = stringResource(id = R.string.favorite_services_title),
                        subtitle = stringResource(id = R.string.favorite_services_subtitle),
                        onClick = { navController.navigate(Routes.FAVORITES) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            when (val state = uiState) {
                is ServicesUiState.Loading -> {
                    // Show loading indicator (Usually implies AI search is running if triggered by Enter)
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Searching with AI...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                is ServicesUiState.Success -> {
                    if (state.services.isEmpty()) {
                        // Show "No results" message
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No local results found.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Press Enter to search with AI assistant.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.padding(top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.services) { service ->
                                ServiceRow(service = service, locale = locale) {
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
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
