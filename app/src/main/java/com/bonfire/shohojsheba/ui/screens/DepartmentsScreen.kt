package com.bonfire.shohojsheba.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.ui.components.CategoryCard
import com.bonfire.shohojsheba.ui.theme.IconBgLightBlue
import com.bonfire.shohojsheba.ui.theme.IconBgLightGreen
import com.bonfire.shohojsheba.ui.theme.IconBgLightPurple
import com.bonfire.shohojsheba.ui.theme.IconBgLightYellow
import com.bonfire.shohojsheba.ui.theme.IconTintDarkBlue
import com.bonfire.shohojsheba.ui.theme.IconTintDarkGreen
import com.bonfire.shohojsheba.ui.theme.IconTintDarkPurple
import com.bonfire.shohojsheba.ui.theme.IconTintDarkYellow

private data class DepartmentCategory(
    val route: String,
    val titleRes: Int,
    val icon: ImageVector,
    val iconBackgroundColor: Color,
    val iconTintColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentsScreen(navController: NavController) {

    val departmentCategories = listOf(
        DepartmentCategory(
            route = "citizen_services",
            titleRes = R.string.category_citizen,
            icon = Icons.Outlined.Person,
            iconBackgroundColor = IconBgLightGreen,
            iconTintColor = IconTintDarkGreen
        ),
        DepartmentCategory(
            route = "farmer_services",
            titleRes = R.string.category_farmer,
            icon = Icons.Outlined.Agriculture,
            iconBackgroundColor = IconBgLightBlue,
            iconTintColor = IconTintDarkBlue
        ),
        DepartmentCategory(
            route = "entrepreneur_services",
            titleRes = R.string.category_entrepreneur,
            icon = Icons.Outlined.Storefront,
            iconBackgroundColor = IconBgLightPurple,
            iconTintColor = IconTintDarkPurple
        ),
        DepartmentCategory(
            route = "govt_office_services",
            titleRes = R.string.category_govt_office,
            icon = Icons.Outlined.Apartment,
            iconBackgroundColor = IconBgLightYellow,
            iconTintColor = IconTintDarkYellow
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(departmentCategories) { category ->
                CategoryCard(
                    title = stringResource(id = category.titleRes),
                    icon = category.icon,
                    iconBackgroundColor = category.iconBackgroundColor,
                    iconTintColor = category.iconTintColor,
                    onClick = { navController.navigate(category.route) }
                )
            }
        }
    }
}
