package com.bonfire.shohojsheba.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bonfire.shohojsheba.data.database.entities.Service
import com.bonfire.shohojsheba.data.database.entities.ServiceDetail
import com.bonfire.shohojsheba.data.database.entities.UserFavorite
import com.bonfire.shohojsheba.ui.viewmodels.ServiceDetailViewModel
import com.bonfire.shohojsheba.ui.viewmodels.ViewModelFactory
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    navController: NavController,
    serviceId: String
) {
    val context = LocalContext.current
    val viewModel: ServiceDetailViewModel = viewModel(
        factory = ViewModelFactory(context, serviceId = serviceId)
    )

    val service by viewModel.service.collectAsState(initial = null)
    val detail by viewModel.serviceDetail.collectAsState(initial = null)
    val isFavorite by viewModel.isFavorite.collectAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize()) {
        if (service != null && detail != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Top spacer to avoid overlapping with floating buttons
                Spacer(modifier = Modifier.height(72.dp)) // 48dp button + 16dp padding + extra 8dp

                ServiceDetailContent(service!!, detail!!)

                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
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
                      coroutineScope.launch {
                          if(isFavorite){
                              viewModel.removeFavorite(serviceId)
                          } else {
                              viewModel.addFavorite(UserFavorite(serviceId = serviceId, addedDate = System.currentTimeMillis()))
                          }
                      }
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

@Composable
private fun ServiceDetailContent(service: Service, detail: ServiceDetail) {
    val imageUrls = detail.images.split(",").filter { it.isNotBlank() }
    val instructions = detail.instructions.split("\n\n")

    instructions.forEachIndexed { index, instruction ->
        AnimatedVisibility(
            visible = true,
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

                    if (index < imageUrls.size) {
                        Spacer(modifier = Modifier.height(12.dp))
                        AsyncImage(
                            model = imageUrls[index],
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
        Text(detail.requiredDocuments, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))

        Text("Processing Time", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(detail.processingTime, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))

        Text("Contact Info", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(detail.contactInfo, style = MaterialTheme.typography.bodyMedium)
    }
}
