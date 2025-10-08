package com.bonfire.shohojsheba.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bonfire.shohojsheba.ui.components.CategoryCard
import com.bonfire.shohojsheba.ui.components.ServiceListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var query by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "সহজ সেবা", 
                        color = Color(0xFF212121),
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF757575)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFFAFAFA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                },
                placeholder = {
                    Text(text = "সেবা খুঁজুন...", color = Color.Gray)
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    unfocusedContainerColor = Color(0xFFF1F1F1),
                    focusedContainerColor = Color(0xFFF1F1F1)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "সেবার বিভাগ",
                color = Color(0xFF212121),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        CategoryCard(
                            title = "নাগরিক",
                            icon = Icons.Outlined.Person,
                            iconBackgroundColor = Color(0xFFE8F5E9),
                            iconTintColor = Color(0xFF2E7D32),
                            onClick = {}
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        CategoryCard(
                            title = "কৃষক",
                            icon = Icons.Outlined.TwoWheeler,
                            iconBackgroundColor = Color(0xFFE3F2FD),
                            iconTintColor = Color(0xFF1976D2),
                            onClick = {}
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        CategoryCard(
                            title = "উদ্যোক্তা",
                            icon = Icons.Outlined.Storefront,
                            iconBackgroundColor = Color(0xFFFFEBEE),
                            iconTintColor = Color(0xFFD32F2F),
                            onClick = {}
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        CategoryCard(
                            title = "সরকারি অফিস",
                            icon = Icons.Outlined.Apartment,
                            iconBackgroundColor = Color(0xFFFFFDE7),
                            iconTintColor = Color(0xFFF9A825),
                            onClick = {}
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            ServiceListItem(
                title = "সাম্প্রতিক সেবা", 
                subtitle = "আপনি সম্প্রতি যা দেখেছেন", 
                onClick = { /*TODO*/ }
            )
        }
    }
}
