package com.bonfire.shohojsheba.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DomainAdd
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Receipt
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
fun EntrepreneurServicesScreen(navController: NavController) {
    val services = listOf(
        CitizenServiceItem(
            icon = Icons.Default.Business,
            titleRes = R.string.service_trade_license_title,
            subtitleRes = R.string.service_trade_license_subtitle,
            onClick = { navController.navigate("service_detail/trade_license") }
        ),
        CitizenServiceItem(
            icon = Icons.Default.DomainAdd,
            titleRes = R.string.service_company_reg_title,
            subtitleRes = R.string.service_company_reg_subtitle,
            onClick = { navController.navigate("service_detail/company_reg") }
        ),
        CitizenServiceItem(
            icon = Icons.Default.Receipt,
            titleRes = R.string.service_tin_cert_title,
            subtitleRes = R.string.service_tin_cert_subtitle,
            onClick = { navController.navigate("service_detail/tin_cert") }
        ),
        CitizenServiceItem(
            icon = Icons.Default.MonetizationOn,
            titleRes = R.string.service_vat_reg_title,
            subtitleRes = R.string.service_vat_reg_subtitle,
            onClick = { navController.navigate("service_detail/vat_reg") }
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.category_entrepreneur), fontWeight = FontWeight.Bold) },
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
