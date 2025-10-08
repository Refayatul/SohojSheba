package com.bonfire.shohojsheba.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SettingsScreen(navController: NavController) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("সেটিংস ও সহায়তা", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { /* Toggle Language */ }) {
            Text("ভাষা পরিবর্তন করুন")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = { /* Feedback logic */ }) {
            Text("সমস্যা জানান")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = { /* About logic */ }) {
            Text("অ্যাপ সম্পর্কে জানুন")
        }
    }
}
