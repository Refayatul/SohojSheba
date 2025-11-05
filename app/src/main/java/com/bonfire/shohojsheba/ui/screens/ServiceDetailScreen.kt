package com.bonfire.shohojsheba.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.data.repositories.RepositoryProvider
import com.bonfire.shohojsheba.ui.viewmodels.ServiceDetailUiState
import com.bonfire.shohojsheba.ui.viewmodels.ServiceDetailViewModel
import com.bonfire.shohojsheba.ui.viewmodels.ServiceDetailViewModelFactory

// Fetch images dynamically based on service ID prefix
fun getServiceImages(context: Context, serviceId: String): List<Int> {
    val prefix = when (serviceId) {
        "citizen_apply_nid" -> "nid_registration_"
        "citizen_renew_passport" -> "passport_step_"
        else -> "img_step_placeholder_"
    }
    return R.drawable::class.java.declaredFields
        .filter { it.name.startsWith(prefix) }
        .mapNotNull { try { it.getInt(null) } catch (e: Exception) { null } }
        .sorted()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    navController: NavController,
    serviceId: String
) {
    val context = LocalContext.current
    val repository = RepositoryProvider.getRepository(context)

    //  Correct ViewModel creation (No flickering)
    val viewModel: ServiceDetailViewModel = viewModel(
        factory = ServiceDetailViewModelFactory(serviceId, repository)
    )

    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is ServiceDetailUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ServiceDetailUiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text((uiState as ServiceDetailUiState.Error).message)
            }
        }
        is ServiceDetailUiState.Success -> {
            val successState = uiState as ServiceDetailUiState.Success
            val service = successState.service
            val detail = successState.serviceDetail

            //  Remember favorite state only once — no flicker
            var isFavorite by remember { mutableStateOf(successState.isFavorite) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {

                TopAppBar(
                    title = { Text(text = context.getString(service.titleRes)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            isFavorite = !isFavorite
                            viewModel.toggleFavorite()
                        }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                detail?.let {
                    val images = getServiceImages(context, serviceId)
                    val instructions = it.instructions.split("\n\n")

                    images.forEachIndexed { index, resId ->
                        Image(
                            painter = painterResource(resId),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (index < instructions.size) {
                            Text(
                                text = instructions[index],
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Text("Required Documents", style = MaterialTheme.typography.titleMedium)
                        Text(it.requiredDocuments, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(16.dp))

                        Text("Processing Time", style = MaterialTheme.typography.titleMedium)
                        Text(it.processingTime, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(16.dp))

                        Text("Contact Info", style = MaterialTheme.typography.titleMedium)
                        Text(it.contactInfo, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
