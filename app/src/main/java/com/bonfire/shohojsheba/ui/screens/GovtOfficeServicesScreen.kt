package com.bonfire.shohojsheba.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.OnlinePrediction
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
fun GovtOfficeServicesScreen(navController: NavController) {
    val services = listOf(
        CitizenServiceItem(
            icon = Icons.Default.OnlinePrediction,
            titleRes = R.string.service_power_div_title,
            subtitleRes = R.string.service_power_div_subtitle,
            onClick = { navController.navigate("service_detail/power_div") }
        ),
        CitizenServiceItem(
            icon = Icons.Default.CorporateFare,
            titleRes = R.string.service_brta_title,
            subtitleRes = R.string.service_brta_subtitle,
            onClick = { navController.navigate("service_detail/brta") }
        ),
        CitizenServiceItem(
            icon = Icons.Default.Functions,
            titleRes = R.string.service_education_board_title,
            subtitleRes = R.string.service_education_board_subtitle,
            onClick = { navController.navigate("service_detail/education_board") }
        ),
        CitizenServiceItem(
            icon = Icons.Default.Gavel,
            titleRes = R.string.service_public_procurement_title,
            subtitleRes = R.string.service_public_procurement_subtitle,
            onClick = { navController.navigate("service_detail/epg") }
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.category_govt_office), fontWeight = FontWeight.Bold) },
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
