package com.bonfire.shohojsheba.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.bonfire.shohojsheba.R
import androidx.navigation.NavController
import com.bonfire.shohojsheba.navigation.Routes
import com.bonfire.shohojsheba.ui.viewmodels.AuthUiState
import com.bonfire.shohojsheba.ui.viewmodels.AuthViewModel

/**
 * =========================================================================================
 *                               FORGOT PASSWORD SCREEN
 * =========================================================================================
 * 
 * HOW IT WORKS:
 * 1.  **State Management**:
 *     -   `passwordResetState`: Observes the ViewModel's state (Idle, Loading, Success, Error).
 *     -   `isResetSuccess`: Local state to toggle between the input form and the success message.
 *     -   `hasAttemptedReset`: Ensures we only react to state changes *after* the user clicks the button.
 * 
 * 2.  **Input Validation**:
 *     -   Validates the email format using `android.util.Patterns.EMAIL_ADDRESS`.
 *     -   Shows inline error messages if validation fails.
 * 
 * 3.  **UI Feedback**:
 *     -   **Loading**: Shows a `CircularProgressIndicator` inside the button.
 *     -   **Success**: Replaces the form with a confirmation message and a "Back to Login" button.
 *     -   **Error**: Displays the error message below the button, allowing the user to retry.
 * 
 * 4.  **Animations**:
 *     -   Uses `AnimatedVisibility` with `scaleIn` + `fadeIn` for a smooth card entry animation.
 *     -   Background canvas draws subtle floating circles for visual depth.
 * =========================================================================================
 */

@Composable
fun ForgotPasswordScreen(navController: NavController, viewModel: AuthViewModel) {

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var isResetInProgress by remember { mutableStateOf(false) }
    var isResetSuccess by remember { mutableStateOf(false) }
    var hasAttemptedReset by remember { mutableStateOf(false) }

    val passwordResetState by viewModel.passwordResetState.collectAsState()

    // Handle password reset state - only update UI if user has clicked the button
    LaunchedEffect(passwordResetState) {
        if (hasAttemptedReset) {
            when (passwordResetState) {
                is AuthUiState.Success -> {
                    isResetInProgress = false
                    isResetSuccess = true
                }
                is AuthUiState.Error -> {
                    isResetInProgress = false
                    isResetSuccess = false
                    // Error shown in UI - allow user to retry
                }
                is AuthUiState.Loading -> {
                    isResetInProgress = true
                    isResetSuccess = false
                }
                is AuthUiState.Idle -> {
                    isResetInProgress = false
                    isResetSuccess = false
                }
            }
        }
    }

    var visible by remember { mutableStateOf(false) }

    // Trigger initial animation and clear state on screen entry
    LaunchedEffect(Unit) {
        visible = true
        // Reset the password reset state so email form is shown initially
        hasAttemptedReset = false
        viewModel.resetPasswordResetState()
    }

    // Extract colors outside Canvas
    val backgroundColor = MaterialTheme.colorScheme.background
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    // Background Canvas for subtle decorative elements (e.g., floating circles)
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Decorative Canvas (subtle, behind content)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            val width = size.width
            val height = size.height
            val radius = 60.dp.toPx()

            // Draw some subtle, semi-transparent circles
            drawCircle(
                color = primaryColor.copy(alpha = 0.03f),
                radius = radius,
                center = Offset(width * 0.15f, height * 0.2f)
            )
            drawCircle(
                color = secondaryColor.copy(alpha = 0.03f),
                radius = radius * 0.8f,
                center = Offset(width * 0.85f, height * 0.8f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(animationSpec = tween(500)) + fadeIn(animationSpec = tween(500))
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.95f), // Slightly wider card
                    shape = RoundedCornerShape(24.dp), // More rounded corners
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp), // Increased elevation for more depth
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface // Standard surface color
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp), // Increased padding inside card
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            IconButton(onClick = {
                                // Clear password reset state before going back
                                viewModel.resetPasswordResetState()
                                navController.popBackStack()
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.back_to_login_desc),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            stringResource(R.string.forgot_password_title),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                        )

                        Text(
                            stringResource(R.string.forgot_password_subtitle),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        if (isResetSuccess) {
                            Text(
                                text = stringResource(R.string.reset_email_sent),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Button(
                                onClick = {
                                    // Clear state and navigate back to login
                                    viewModel.resetPasswordResetState()
                                    navController.popBackStack()
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(stringResource(R.string.back_to_login_button), fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // Email Field
                            OutlinedTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    emailError = false
                                },
                                label = { Text(stringResource(R.string.email_address_label)) },
                                placeholder = { Text(stringResource(R.string.enter_registered_email)) },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = stringResource(R.string.email_icon_desc),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                isError = emailError,
                                supportingText = {
                                    if (emailError) {
                                        Text(text = stringResource(R.string.valid_email_required))
                                    }
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = if (emailError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    unfocusedIndicatorColor = if (emailError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                    focusedLabelColor = if (emailError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    unfocusedLabelColor = if (emailError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                    cursorColor = if (emailError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Reset Button
                            Button(
                                onClick = {
                                    // Check for validation errors
                                    emailError = email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

                                    if (!emailError) {
                                        // Mark that user has attempted reset
                                        hasAttemptedReset = true
                                        // Only proceed if not already in progress
                                        if (!isResetInProgress) {
                                            isResetInProgress = true
                                            viewModel.resetPassword(email)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                // Disable only while in progress, allow retry on error
                                enabled = !isResetInProgress,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                when {
                                    isResetInProgress -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                    passwordResetState is AuthUiState.Error -> {
                                        Text(stringResource(R.string.try_again), fontWeight = FontWeight.Bold)
                                    }
                                    else -> {
                                        Text(stringResource(R.string.reset_password_button), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Error message - allows retry
                            if (passwordResetState is AuthUiState.Error) {
                                Spacer(modifier = Modifier.height(8.dp))
                                val error = (passwordResetState as AuthUiState.Error).message
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
