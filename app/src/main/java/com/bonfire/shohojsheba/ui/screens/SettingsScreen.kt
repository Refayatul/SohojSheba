package com.bonfire.shohojsheba.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bonfire.shohojsheba.LocalLocale
import com.bonfire.shohojsheba.LocalOnLocaleChange
import com.bonfire.shohojsheba.R
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.settings), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            LanguageSection()
            SupportSection()
            AboutSection()
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.secondary,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun LanguageSection() {
    val locale = LocalLocale.current
    val onLocaleChange = LocalOnLocaleChange.current

    Column {
        SectionTitle(title = stringResource(id = R.string.section_language))
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(
                        if (locale.language == "bn") R.string.language_bangla
                        else R.string.language_english
                    ),
                    fontSize = 18.sp
                )

                Switch(
                    checked = locale.language == "bn",
                    onCheckedChange = {
                        val newLocale = if (locale.language == "bn") Locale("en") else Locale("bn")
                        onLocaleChange(newLocale)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@Composable
private fun SupportSection() {
    Column {
        SectionTitle(title = stringResource(id = R.string.section_support))
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
        ) {
            SettingsRow(title = stringResource(id = R.string.report_a_problem), onClick = {})
            Divider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF1F1F1))
            SettingsRow(title = stringResource(id = R.string.provide_feedback), onClick = {})
        }
    }
}

@Composable
private fun AboutSection() {
    var showDevDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val githubUrl = "https://github.com/Refayatul/SohojSheba"

    if (showDevDialog) {
        DeveloperDialog(onDismiss = { showDevDialog = false })
    }

    Column {
        SectionTitle(title = stringResource(id = R.string.section_about))
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
        ) {
            SettingsRow(title = stringResource(id = R.string.app_name), onClick = { })
            Divider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF1F1F1))
            SettingsRow(
                title = stringResource(id = R.string.github),
                onClick = { uriHandler.openUri(githubUrl) }
            )
            Divider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF1F1F1))
            SettingsRow(title = stringResource(id = R.string.developers), onClick = { showDevDialog = true })
            Divider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF1F1F1))
            SettingsInfoRow(label = stringResource(id = R.string.version_label), value = stringResource(id = R.string.version_number))
        }
    }
}

@Composable
private fun DeveloperDialog(onDismiss: () -> Unit) {
    val developers = listOf("Meghla", "Paromita", "Galib", "Refayatul")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.dev_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                developers.forEach { name ->
                    Text(text = name, fontSize = 16.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.dialog_close))
            }
        }
    )
}

@Composable
private fun SettingsRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 18.sp)
        Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 18.sp)
        Text(text = value, fontSize = 18.sp, color = MaterialTheme.colorScheme.secondary)
    }
}
