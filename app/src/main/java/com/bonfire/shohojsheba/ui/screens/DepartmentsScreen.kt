package com.bonfire.shohojsheba.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Agriculture
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
fun DepartmentsScreen(navController: NavController) {

    val departmentCategories = listOf(
        CitizenServiceItem(
            icon = Icons.Outlined.Person,
            titleRes = R.string.category_citizen,
            subtitleRes = R.string.citizen_services_title,
            onClick = { navController.navigate("citizen_services") }
        ),
        CitizenServiceItem(
            icon = Icons.Outlined.Agriculture,
            titleRes = R.string.category_farmer,
            subtitleRes = R.string.service_agri_portal_subtitle,
            onClick = { navController.navigate("farmer_services") }
        ),
        CitizenServiceItem(
            icon = Icons.Outlined.Storefront,
            titleRes = R.string.category_entrepreneur,
            subtitleRes = R.string.service_trade_license_subtitle,
            onClick = { navController.navigate("entrepreneur_services") }
        ),
        CitizenServiceItem(
            icon = Icons.Outlined.Apartment,
            titleRes = R.string.category_govt_office,
            subtitleRes = R.string.service_public_procurement_subtitle,
            onClick = { navController.navigate("govt_office_services") }
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.departments_page_title),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(departmentCategories) { service ->
                ServiceRow(service = service)
            }
        }
    }
}
