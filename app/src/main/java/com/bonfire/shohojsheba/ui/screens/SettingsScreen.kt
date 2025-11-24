package com.bonfire.shohojsheba.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.navigation.Routes
import com.bonfire.shohojsheba.ui.components.EnhancedTopAppBar
import com.bonfire.shohojsheba.ui.viewmodels.AuthViewModel
import com.bonfire.shohojsheba.ui.viewmodels.ViewModelFactory
import com.bonfire.shohojsheba.utils.AppLocaleManager
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    currentThemeMode: String,
    onThemeChange: (String) -> Unit
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory(context))
    val currentUser by authViewModel.currentUser.collectAsState()
    val showLanguageOption = true
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    // Listen for logout messages
    LaunchedEffect(Unit) {
        authViewModel.toastMessage.collectLatest {
            Toast.makeText(context, it.asString(context), Toast.LENGTH_SHORT).show()
        }
    }

    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                authViewModel.logout()
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }

    if (showEditProfileDialog && currentUser != null) {
        EditProfileDialog(
            user = currentUser!!,
            onDismiss = { showEditProfileDialog = false },
            onSave = { newName ->
                val updatedUser = currentUser!!.copy(name = newName)
                authViewModel.updateUser(updatedUser)
                showEditProfileDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            EnhancedTopAppBar(
                title = stringResource(id = R.string.settings),
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- User Profile Section ---
            if (currentUser != null) {
                UserProfileSection(
                    user = currentUser!!,
                    onEditClick = { showEditProfileDialog = true },
                    onLogoutClick = { showLogoutDialog = true }
                )
            }

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
        SectionTitle(title = stringResource(id = R.string.section_appearance))
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // System chip
                ThemeChip(
                    text = stringResource(id = R.string.theme_system),
                    selected = currentMode == "system",
                    onClick = { onModeSelected("system") },
                    modifier = Modifier.weight(1f)
                )
                // Light chip
                ThemeChip(
                    text = stringResource(id = R.string.theme_light),
                    selected = currentMode == "light",
                    onClick = { onModeSelected("light") },
                    modifier = Modifier.weight(1f)
                )
                // Dark chip
                ThemeChip(
                    text = stringResource(id = R.string.theme_dark),
                    selected = currentMode == "dark",
                    onClick = { onModeSelected("dark") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ThemeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
// ------------------------------------------

@Composable
private fun LanguageSection() {
    val locale = com.bonfire.shohojsheba.utils.LocalLocale.current
    val onLocaleChange = com.bonfire.shohojsheba.utils.LocalOnLocaleChange.current
    val currentLanguage = locale.language

    Column {
        SectionTitle(title = stringResource(id = R.string.section_language))
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
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
                        if (currentLanguage == "bn") R.string.language_bangla
                        else R.string.language_english
                    ),
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Switch(
                    checked = currentLanguage == "bn",
                    onCheckedChange = { isBangla ->
                        val newLanguage = if (isBangla) "bn" else "en"
                        val newLocale = if (newLanguage == "bn") java.util.Locale("bn", "BD") else java.util.Locale("en", "US")
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

@Composable
private fun UserProfileSection(
    user: com.bonfire.shohojsheba.data.models.User,
    onEditClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar Placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.take(1).uppercase(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // User Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = user.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = user.email,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(id = R.string.edit_profile))
                }

                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(id = R.string.logout))
                }
            }
        }
    }
}

@Composable
private fun LogoutConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.logout)) },
        text = { Text(stringResource(id = R.string.logout_confirm)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(id = R.string.logout))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun EditProfileDialog(
    user: com.bonfire.shohojsheba.data.models.User,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var newName by remember { mutableStateOf(user.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.edit_profile)) },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text(stringResource(id = R.string.name)) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(newName) },
                enabled = newName.isNotBlank()
            ) {
                Text(stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}
