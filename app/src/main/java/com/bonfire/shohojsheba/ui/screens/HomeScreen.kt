package com.bonfire.shohojsheba.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Agriculture
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.ui.components.CategoryCard
import com.bonfire.shohojsheba.ui.components.ServiceListItem
import com.bonfire.shohojsheba.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var query by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = stringResource(id = R.string.app_name), 
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(id = R.string.settings),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.secondary)
                },
                placeholder = {
                    Text(text = stringResource(id = R.string.search_hint), color = MaterialTheme.colorScheme.secondary)
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    unfocusedContainerColor = SearchBarBackground,
                    focusedContainerColor = SearchBarBackground
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                        onClick = {}
                    )
                    CategoryCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(id = R.string.category_farmer),
                        icon = Icons.Outlined.Agriculture,
                        iconBackgroundColor = IconBgLightBlue,
                        iconTintColor = IconTintDarkBlue,
                        onClick = {}
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CategoryCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(id = R.string.category_entrepreneur),
                        icon = Icons.Outlined.Storefront,
                        iconBackgroundColor = IconBgLightPurple,
                        iconTintColor = IconTintDarkPurple,
                        onClick = {}
                    )
                    CategoryCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(id = R.string.category_govt_office),
                        icon = Icons.Outlined.Apartment,
                        iconBackgroundColor = IconBgLightYellow,
                        iconTintColor = IconTintDarkYellow,
                        onClick = {}
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ServiceListItem(
                    title = stringResource(id = R.string.recent_services_title),
                    subtitle = stringResource(id = R.string.recent_services_subtitle),
                    onClick = { /* Handle navigation to recent services */ }
                )
                ServiceListItem(
                    title = stringResource(id = R.string.popular_services_title),
                    subtitle = stringResource(id = R.string.popular_services_subtitle),
                    onClick = { /* Handle navigation to popular services */ }
                )
            }
        }
    }
}
