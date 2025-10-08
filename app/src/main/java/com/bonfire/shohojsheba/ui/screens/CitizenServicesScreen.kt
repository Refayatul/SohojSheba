package com.bonfire.shohojsheba.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.OtherHouses
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bonfire.shohojsheba.R

data class CitizenServiceItem(
    val icon: ImageVector,
    val titleRes: Int,
    val subtitleRes: Int,
    val onClick: () -> Unit = {}
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenServicesScreen(navController: NavController) {

    val services = listOf(
        CitizenServiceItem(
            icon = Icons.Default.Badge,
            titleRes = R.string.service_nid_title,
            subtitleRes = R.string.service_nid_subtitle,
            onClick = { navController.navigate("service_detail/nid") }
        ),
        CitizenServiceItem(
            icon = Icons.Default.SwapHoriz,
            titleRes = R.string.service_voter_transfer_title,
            subtitleRes = R.string.service_voter_transfer_subtitle,
            onClick = { navController.navigate("service_detail/voter_transfer") }
        ),
        CitizenServiceItem(
            icon = Icons.Default.Book,
            titleRes = R.string.service_passport_title,
            subtitleRes = R.string.service_passport_subtitle,
            onClick = { navController.navigate("service_detail/passport") }
        ),
        CitizenServiceItem(
            icon = Icons.Default.WorkspacePremium,
            titleRes = R.string.service_birth_cert_title,
            subtitleRes = R.string.service_birth_cert_subtitle,
            onClick = { navController.navigate("service_detail/birth_cert") }
        ),
        CitizenServiceItem(
            icon = Icons.Default.DirectionsCar,
            titleRes = R.string.service_driving_license_title,
            subtitleRes = R.string.service_driving_license_subtitle,
            onClick = { navController.navigate("service_detail/driving_license") }
        ),
        CitizenServiceItem(
            icon = Icons.Default.OtherHouses,
            titleRes = R.string.service_land_reg_title,
            subtitleRes = R.string.service_land_reg_subtitle,
            onClick = { navController.navigate("service_detail/land_reg") }
        ),
        CitizenServiceItem(
            icon = Icons.Default.Favorite,
            titleRes = R.string.service_marriage_reg_title,
            subtitleRes = R.string.service_marriage_reg_subtitle,
            onClick = { navController.navigate("service_detail/marriage_reg") }
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.citizen_services_title), fontWeight = FontWeight.Bold) },
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

@Composable
fun ServiceRow(service: CitizenServiceItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = service.onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = service.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = service.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(id = service.subtitleRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
