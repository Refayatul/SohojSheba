package com.bonfire.shohojsheba.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bonfire.shohojsheba.LocalLocale
import com.bonfire.shohojsheba.LocalOnLocaleChange
import com.bonfire.shohojsheba.R
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    currentThemeMode: String,       // Added Parameter
    onThemeChange: (String) -> Unit // Added Parameter
) {
    val showLanguageOption = false

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.settings),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface // Fix text color
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface // Fix icon color
                        )
                    }
                },
                // Changed from Color.White to Surface so it respects dark mode
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- Added Theme Section ---
            ThemeSection(
                currentMode = currentThemeMode,
                onModeSelected = onThemeChange
            )

            if (showLanguageOption) {
                LanguageSection()
            }
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

// --- New Composable for Theme Selection ---
@Composable
private fun ThemeSection(
    currentMode: String,
    onModeSelected: (String) -> Unit
) {
    Column {
        SectionTitle(title = "Appearance") // You can add string resource later
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface) // Adapt to dark mode
        ) {
            ThemeOptionRow("System Default", currentMode == "system") { onModeSelected("system") }
            Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            ThemeOptionRow("Light Mode", currentMode == "light") { onModeSelected("light") }
            Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            ThemeOptionRow("Dark Mode", currentMode == "dark") { onModeSelected("dark") }
        }
    }
}

@Composable
private fun ThemeOptionRow(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        RadioButton(
            selected = selected,
            onClick = null // Handled by Row
        )
    }
}
// ------------------------------------------

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
                .background(MaterialTheme.colorScheme.surface) // Fix: use surface
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
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface // Fix text color
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
                .background(MaterialTheme.colorScheme.surface) // Fix: use surface
        ) {
            SettingsRow(title = stringResource(id = R.string.report_a_problem), onClick = {})
            Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
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

    val versionNumber = stringResource(id = R.string.version_number)
    val versionName = stringResource(id = R.string.version_name)
    val annotatedVersionString = buildAnnotatedString {
        append(versionNumber)
        withStyle(style = SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = 12.sp)) {
            append(versionName)
        }
    }

    Column {
        SectionTitle(title = stringResource(id = R.string.section_about))
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface) // Fix: use surface
        ) {
            SettingsRow(title = stringResource(id = R.string.app_name), onClick = { })
            Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            SettingsRow(
                title = stringResource(id = R.string.github),
                onClick = { uriHandler.openUri(githubUrl) }
            )
            Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            SettingsRow(title = stringResource(id = R.string.developers), onClick = { showDevDialog = true })
            Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            SettingsInfoRowWithAnnotatedValue(label = stringResource(id = R.string.version_label), value = annotatedVersionString)
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
        },
        containerColor = MaterialTheme.colorScheme.surface, // Fix background
        titleContentColor = MaterialTheme.colorScheme.onSurface, // Fix text
        textContentColor = MaterialTheme.colorScheme.onSurface // Fix text
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
        Text(text = title, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface) // Fix text
        Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun SettingsInfoRowWithAnnotatedValue(label: String, value: AnnotatedString) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface) // Fix text
        Text(text = value, fontSize = 18.sp, color = MaterialTheme.colorScheme.secondary)
    }
}