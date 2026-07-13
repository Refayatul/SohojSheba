package com.bonfire.shohojsheba.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
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
import kotlinx.coroutines.launch

/**
 * =========================================================================================
 *                                     LOGIN SCREEN
 * =========================================================================================
 * 
 * HOW IT WORKS:
 * 1.  **Authentication Methods**:
 *     -   **Email/Password**: Standard login using Firebase Auth.
 *     -   **Google Sign-In**: Uses Google Identity Services to authenticate and then sign in to Firebase.
 * 
 * 2.  **State Management**:
 *     -   `authState`: Tracks the state of email/password login (Idle, Loading, Success, Error).
 *     -   `googleSignInState`: Tracks the state of Google Sign-In.
 *     -   `currentUser`: Observes the current user session to auto-redirect if already logged in.
 * 
 * 3.  **Google Sign-In Configuration**:
 *     -   Configures `GoogleSignInOptions` to request the user's ID token and email.
 *     -   The ID token is crucial for Firebase authentication.
 * 
 * 4.  **UI Components**:
 *     -   **Input Fields**: Email and Password fields with visibility toggle.
 *     -   **Buttons**: Login, Google Sign-In, Forgot Password, and Register buttons.
 *     -   **Feedback**: Shows loading spinners inside buttons and error messages below fields.
 * 
 * 5.  **Navigation**:
 *     -   Redirects to `Routes.HOME` on successful login.
 *     -   Links to `Routes.REGISTER` and `Routes.FORGOT_PASSWORD`.
 * =========================================================================================
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    googleSignInLauncher: ActivityResultLauncher<android.content.Intent>? = null,
    authViewModel: AuthViewModel // Injected ViewModel
) {
    val context = LocalContext.current
    // Removed local ViewModel creation
    val authState by authViewModel.authState.collectAsState()
    val googleSignInState by authViewModel.googleSignInState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    // --- State Management ---
    // 'remember' tells Compose to keep these variables alive even when the screen redraws.
    // 'mutableStateOf' makes them observable - when they change, the UI updates automatically.
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // --- Google Sign-In Configuration ---
    // 1. Configure options (request email and ID token)
    // 2. Create the client
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

    // --- UI Structure ---
    // Root Container: Box allows layering (Canvas background + Card content)
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Decorative Background
        // Custom drawing on a Canvas for a unique look (subtle floating circles)
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
                center = Offset(width * 0.8f, height * 0.1f)
            )
            drawCircle(
                color = secondaryColor.copy(alpha = 0.03f),
                radius = radius * 0.8f,
                center = Offset(width * 0.2f, height * 0.9f)
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
                    Text(
                        text = stringResource(R.string.welcome_title),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(R.string.email_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        // Disable input while loading to prevent errors
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

                    Button(
                        onClick = {
                            authViewModel.login(email, password)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = authState != AuthUiState.Loading && googleSignInState != AuthUiState.Loading
                    ) {
                        if (authState is AuthUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(stringResource(R.string.login_button))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Google Sign-In Button
                    // Triggers the Google Sign-In Intent via the ActivityResultLauncher
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
                            Text(stringResource(R.string.google_signin_button))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { navController.navigate(Routes.FORGOT_PASSWORD) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.forgot_password_link))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { navController.navigate(Routes.REGISTER) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.register_link))
                    }
                }
            }
        }
    }
}
