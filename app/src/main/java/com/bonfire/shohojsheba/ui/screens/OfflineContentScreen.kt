package com.bonfire.sohojsheba.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun OfflineContentScreen(navController: NavController) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("এই সেবা অফলাইনে ব্যবহারযোগ্য", fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { /* Download logic later */ }) {
            Text("সেবা প্যাক ডাউনলোড করুন")
        }
    }
}
