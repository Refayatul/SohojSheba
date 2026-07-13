package com.bonfire.shohojsheba.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.bonfire.shohojsheba.R
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bonfire.shohojsheba.navigation.Routes
import com.bonfire.shohojsheba.ui.viewmodels.AuthUiState
import com.bonfire.shohojsheba.ui.viewmodels.AuthViewModel
import com.bonfire.shohojsheba.ui.viewmodels.ViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.flow.collectLatest

/**
 * =========================================================================================
 *                                  REGISTER SCREEN
 * =========================================================================================
 * 
 * HOW IT WORKS:
 * 1.  **Registration Methods**:
 *     -   **Email/Password**: Creates a new Firebase Auth account with email, password, and name.
 *     -   **Google Sign-In**: Alternative sign-up using Google Identity Services.
 * 
 * 2.  **Form Validation**:
 *     -   **Password Mismatch**: Checks if `password` equals `confirmPassword` before submission.
 *     -   **Input Fields**: Validates format (e.g., email keyboard type) and visibility toggles.
 * 
 * 3.  **State Management**:
 *     -   `authState`: Tracks the registration process (Idle, Loading, Success, Error).
 *     -   `googleSignInState`: Tracks Google Sign-In status.
 *     -   `toastMessage`: Listens for one-time events like "Registration Successful" or errors.
 * 
 * 4.  **UI Components**:
 *     -   **Inputs**: Name, Email, Password, Confirm Password.
 *     -   **Buttons**: Register (primary), Google Sign-Up (secondary), Login Link (tertiary).
 *     -   **Feedback**: Loading spinners and error messages.
 * 
 * 5.  **Navigation**:
 *     -   On success, the ViewModel handles navigation (usually to Home).
 *     -   Provides a link back to `LoginScreen` if the user already has an account.
 * =========================================================================================
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    googleSignInLauncher: ActivityResultLauncher<android.content.Intent>? = null,
    authViewModel: AuthViewModel // Injected ViewModel
) {
    val context = LocalContext.current
    // Removed local ViewModel creation
    val authState by authViewModel.authState.collectAsState()
    val googleSignInState by authViewModel.googleSignInState.collectAsState()

    // --- Form State ---
    // We track input fields for Name, Email, Password, and Confirm Password
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    
    // Visibility toggles for password fields
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Configure Google Sign-In
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("187859520695-03dpjg2k339oi3if24ts12ioip830a79.apps.googleusercontent.com")
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    // Listen for auth messages
    LaunchedEffect(Unit) {
        authViewModel.toastMessage.collectLatest {
            Toast.makeText(context, it.asString(context), Toast.LENGTH_SHORT).show()
        }
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
                center = Offset(width * 0.9f, height * 0.1f)
            )
            drawCircle(
                color = secondaryColor.copy(alpha = 0.03f),
                radius = radius * 0.8f,
                center = Offset(width * 0.1f, height * 0.9f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
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
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_content_desc))
                        }
                    }

                    Text(
                        text = stringResource(R.string.create_account_title),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.full_name_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(R.string.email_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        enabled = authState != AuthUiState.Loading && googleSignInState != AuthUiState.Loading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.password_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) stringResource(R.string.hide_password_desc) else stringResource(R.string.show_password_desc)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        enabled = authState != AuthUiState.Loading && googleSignInState != AuthUiState.Loading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text(stringResource(R.string.confirm_password_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (confirmPasswordVisible) stringResource(R.string.hide_password_desc) else stringResource(R.string.show_password_desc)
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        enabled = authState != AuthUiState.Loading && googleSignInState != AuthUiState.Loading
                    )

                    if (authState is AuthUiState.Error) {
                        Text(
                            text = (authState as AuthUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (googleSignInState is AuthUiState.Error) {
                        Text(
                            text = (googleSignInState as AuthUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- Registration Button ---
                    Button(
                        onClick = {
                            // 1. Client-side Validation: Check if passwords match
                            if (password != confirmPassword) {
                                Toast.makeText(context, context.getString(R.string.passwords_mismatch), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            // 2. Trigger Registration via ViewModel
                            authViewModel.register(email, password, name)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        // Disable button while loading to prevent double-clicks
                        enabled = authState != AuthUiState.Loading && googleSignInState != AuthUiState.Loading
                    ) {
                        if (authState is AuthUiState.Loading) {
                            // Show spinner while registering
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(stringResource(R.string.register_button))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Google Sign-In Button
                    OutlinedButton(
                        onClick = {
                            if (googleSignInLauncher != null) {
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            } else {
                                Toast.makeText(context, context.getString(R.string.google_signin_error), Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = authState != AuthUiState.Loading && googleSignInState != AuthUiState.Loading
                    ) {
                        if (googleSignInState is AuthUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(stringResource(R.string.google_signup_button))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.login_link))
                    }
                }
            }
        }
    }


}
