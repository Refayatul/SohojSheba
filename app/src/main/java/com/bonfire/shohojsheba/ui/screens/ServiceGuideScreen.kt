package com.bonfire.shohojsheba.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource

@Composable
fun ServiceGuideScreen(navController: NavController) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(stringResource(com.bonfire.shohojsheba.R.string.passport_application), fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        Text(stringResource(com.bonfire.shohojsheba.R.string.guide_step_1), fontSize = 18.sp)
        Text(stringResource(com.bonfire.shohojsheba.R.string.guide_step_2), fontSize = 18.sp)
        Text(stringResource(com.bonfire.shohojsheba.R.string.guide_step_3), fontSize = 18.sp)
        Text(stringResource(com.bonfire.shohojsheba.R.string.guide_step_4), fontSize = 18.sp)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.navigate("offline") },
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(com.bonfire.shohojsheba.R.string.view_offline_service))
        }
    }
}
