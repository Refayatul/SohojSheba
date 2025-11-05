package com.bonfire.shohojsheba.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.data.repositories.RepositoryProvider
import com.bonfire.shohojsheba.ui.viewmodels.ServiceDetailUiState
import com.bonfire.shohojsheba.ui.viewmodels.ServiceDetailViewModel
import com.bonfire.shohojsheba.ui.viewmodels.ServiceDetailViewModelFactory

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ServiceDetailScreen(
    navController: NavController,
    serviceId: String
) {
    val context = LocalContext.current
    val repository = RepositoryProvider.getRepository(context)

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
            var isFavorite by remember { mutableStateOf(successState.isFavorite) }

            Box(Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Top spacer to avoid overlapping with floating buttons
                    Spacer(modifier = Modifier.height(72.dp)) // 48dp button + 16dp padding + extra 8dp

                    detail?.let {
                        val images = getServiceImages(context, serviceId)
                        val instructions = it.instructions.split("\n\n")

                        instructions.forEachIndexed { index, instruction ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(700)) +
                                        slideInVertically(animationSpec = tween(700)) { it / 2 },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp)
                                    ) {
                                        Text(
                                            text = instruction.trim(),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 16.sp,
                                                lineHeight = 24.sp
                                            ),
                                            fontWeight = FontWeight.Medium
                                        )

                                        if (index < images.size) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Image(
                                                painter = painterResource(images[index]),
                                                contentDescription = null,
                                                contentScale = ContentScale.FillWidth,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .shadow(2.dp, RoundedCornerShape(16.dp))
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Bottom Info Section - light card
                        Column(
                            Modifier
                                .padding(horizontal = 16.dp)
                                .background(Color.White, RoundedCornerShape(16.dp))
                                .shadow(2.dp, RoundedCornerShape(16.dp))
                                .padding(20.dp)
                        ) {
                            Text("Required Documents", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(it.requiredDocuments, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(16.dp))

                            Text("Processing Time", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(it.processingTime, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(16.dp))

                            Text("Contact Info", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(it.contactInfo, style = MaterialTheme.typography.bodyMedium)
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Floating back button
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .padding(16.dp)
                        .size(48.dp)
                        .align(Alignment.TopStart)
                        .background(Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }

                // Floating favorite button
                IconButton(
                    onClick = {
                        isFavorite = !isFavorite
                        viewModel.toggleFavorite()
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .size(48.dp)
                        .align(Alignment.TopEnd)
                        .background(Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.Black
                    )
                }
            }
        }
    }
}
