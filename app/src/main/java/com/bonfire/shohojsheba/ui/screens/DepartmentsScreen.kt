package com.bonfire.shohojsheba.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Agriculture
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Storefront
import com.bonfire.shohojsheba.ui.components.EnhancedTopAppBar
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
import com.bonfire.shohojsheba.navigation.Routes
import com.bonfire.shohojsheba.ui.components.CategoryCard
import com.bonfire.shohojsheba.ui.components.DecorativeBackground
import com.bonfire.shohojsheba.ui.theme.*

/**
 * =========================================================================================
 *                                 DEPARTMENTS SCREEN
 * =========================================================================================
 * 
 * HOW IT WORKS:
 * 1.  **Purpose**:
 *     -   Acts as a central hub for browsing services by category (Citizen, Farmer, etc.).
 *     -   Provides a visual, grid-based navigation menu.
 * 
 * 2.  **UI Layout**:
 *     -   **Grid System**: Uses nested `Column` and `Row` composables to create a 2x2 grid.
 *     -   **Category Cards**: Each item is a `CategoryCard` with a distinct icon, color, and label.
 *     -   **Decorative Background**: Wraps the content in `DecorativeBackground` for visual consistency.
 * 
 * 3.  **Navigation**:
 *     -   Each card navigates to a specific service list route (e.g., `Routes.CITIZEN_SERVICES`).
 *     -   Includes a top bar with a back button to return to the previous screen.
 * =========================================================================================
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            EnhancedTopAppBar(
                title = stringResource(id = R.string.departments_page_title),
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        DecorativeBackground {
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
            
            // 2x2 Grid of Categories
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CategoryCard(
                        title = stringResource(id = R.string.category_citizen),
                        icon = Icons.Outlined.Person,
                        iconBgColor = IconBgLightGreen,
                        iconTintColor = IconTintDarkGreen,
                        onClick = { navController.navigate(Routes.CITIZEN_SERVICES) },
                        modifier = Modifier.weight(1f)
                    )
                    CategoryCard(
                        title = stringResource(id = R.string.category_farmer),
                        icon = Icons.Outlined.Agriculture,
                        iconBgColor = IconBgLightBlue,
                        iconTintColor = IconTintDarkBlue,
                        onClick = { navController.navigate(Routes.FARMER_SERVICES) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CategoryCard(
                        title = stringResource(id = R.string.category_entrepreneur),
                        icon = Icons.Outlined.Storefront,
                        iconBgColor = IconBgLightPurple,
                        iconTintColor = IconTintDarkPurple,
                        onClick = { navController.navigate(Routes.ENTREPRENEUR_SERVICES) },
                        modifier = Modifier.weight(1f)
                    )
                    CategoryCard(
                        title = stringResource(id = R.string.category_govt_office),
                        icon = Icons.Outlined.Apartment,
                        iconBgColor = IconBgLightYellow,
                        iconTintColor = IconTintDarkYellow,
                        onClick = { navController.navigate(Routes.GOVT_OFFICE_SERVICES) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        }
    }
}
