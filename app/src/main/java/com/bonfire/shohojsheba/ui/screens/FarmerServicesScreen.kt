package com.bonfire.shohojsheba.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bonfire.shohojsheba.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerServicesScreen(navController: NavController) {
    val services = listOf(
        CitizenServiceItem(
            icon = Icons.Default.Spa,
            titleRes = R.string.service_agri_portal_title,
            subtitleRes = R.string.service_agri_portal_subtitle,
            onClick = { navController.navigate("service_detail/agri_portal") }
        ),
        CitizenServiceItem(
            icon = Icons.Default.Eco,
            titleRes = R.string.service_fertilizer_rec_title,
            subtitleRes = R.string.service_fertilizer_rec_subtitle,
            onClick = { navController.navigate("service_detail/fertilizer") }
        ),
        CitizenServiceItem(
            icon = Icons.Default.Payments,
            titleRes = R.string.service_agri_loan_title,
            subtitleRes = R.string.service_agri_loan_subtitle,
            onClick = { navController.navigate("service_detail/agri_loan") }
        ),
        CitizenServiceItem(
            icon = Icons.Default.Agriculture,
            titleRes = R.string.service_seed_cert_title,
            subtitleRes = R.string.service_seed_cert_subtitle,
            onClick = { navController.navigate("service_detail/seed_cert") }
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.category_farmer), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(services) { service ->
                ServiceRow(service = service)
            }
        }
    }
}
