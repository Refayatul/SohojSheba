package com.bonfire.shohojsheba.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Agriculture
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.ui.components.CategoryCard
import com.bonfire.shohojsheba.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.departments_page_title), fontWeight = FontWeight.Bold) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.service_categories),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
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
    }
}
