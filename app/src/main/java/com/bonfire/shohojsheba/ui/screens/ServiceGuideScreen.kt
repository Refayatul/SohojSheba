package com.bonfire.shohojsheba.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun ServiceGuideScreen(navController: NavController) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("পাসপোর্ট আবেদন", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        Text("ধাপ ১: অনলাইনে আবেদন ফর্ম পূরণ করুন।", fontSize = 18.sp)
        Text("ধাপ ২: প্রয়োজনীয় ডকুমেন্ট আপলোড করুন।", fontSize = 18.sp)
        Text("ধাপ ৩: ফি প্রদান করুন এবং সময় নির্ধারণ করুন।", fontSize = 18.sp)
        Text("ধাপ ৪: অফিসে গিয়ে যাচাই সম্পন্ন করুন।", fontSize = 18.sp)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.navigate("offline") },
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("অফলাইনে সেবা দেখুন")
        }
    }
}
