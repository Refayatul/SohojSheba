package com.bonfire.shohojsheba.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.data.models.User
import com.bonfire.shohojsheba.data.repositories.AuthRepository
import com.bonfire.shohojsheba.util.Resource
import com.bonfire.shohojsheba.util.UiText
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

// 'Sealed Class' is like a super-powered Enum.
// It defines the ONLY possible states our UI can be in.
// This prevents bugs because we handle every possible case (Idle, Loading, Success, Error).
sealed class AuthUiState {
    object Idle : AuthUiState()      // Doing nothing
    object Loading : AuthUiState()   // Spinner spinning
    data class Success(val user: User) : AuthUiState() // Logged in!
    data class Error(val message: String) : AuthUiState() // Something went wrong
}

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    // --- State Management (Backing Property Pattern) ---
    // Why use this pattern?
    // 1. Encapsulation: The mutable state (_state) is private, so only this ViewModel can modify it.
    // 2. Safety: The public state (state) is immutable (StateFlow), so the UI can only observe it, not change it.
    
    // Auth State: Tracks the status of email/password login and registration
    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    // Google Sign-In State: Tracks the status of Google authentication separately
    private val _googleSignInState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val googleSignInState: StateFlow<AuthUiState> = _googleSignInState.asStateFlow()

    // Logout State: Tracks if logout is in progress or completed
    private val _logoutState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val logoutState: StateFlow<AuthUiState> = _logoutState.asStateFlow()

    // Password Reset State: Tracks the status of the "Forgot Password" flow
    private val _passwordResetState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val passwordResetState: StateFlow<AuthUiState> = _passwordResetState.asStateFlow()

    // Current User: Holds the currently logged-in user's data (or null if logged out)
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Toast Messages: One-time events (SharedFlow) for showing pop-up messages
    // Unlike StateFlow, SharedFlow doesn't hold a value, it just emits events.
    // This is perfect for Toasts/Snackbars that should only show once.
    private val _toastMessage = MutableSharedFlow<UiText>()
    val toastMessage = _toastMessage.asSharedFlow()

    // Auth Check Flag: Prevents the UI from flickering (Login -> Home) on app start
    // We wait until this is true before deciding which screen to show.
    private val _isAuthCheckComplete = MutableStateFlow(false)
    val isAuthCheckComplete: StateFlow<Boolean> = _isAuthCheckComplete.asStateFlow()

    init {
        // Automatically check if a user is already logged in when the app starts
        getCurrentUser()
    }

    // --- Registration Logic ---
    fun register(email: String, password: String, name: String) {
        // 1. Input Validation
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            _authState.value = AuthUiState.Error("Please fill all fields")
            viewModelScope.launch { _toastMessage.emit(UiText.StringResource(R.string.fill_all_fields)) }
            return
        }
        
        // 2. Perform Registration in Background
        viewModelScope.launch {
            authRepository.register(email, password, name)
                .onEach { resource ->
                    // Handle the result from the repository
                    when (resource) {
                        is Resource.Loading -> _authState.value = AuthUiState.Loading
                        is Resource.Success -> {
                            resource.data?.let { user ->
                                // Success! Update state and current user
                                _authState.value = AuthUiState.Success(user)
                                _currentUser.value = user
                                _toastMessage.emit(UiText.StringResource(R.string.registration_successful))
                            }
                        }
                        is Resource.Error -> {
                            // Failure! Show error message
                            _authState.value = AuthUiState.Error(resource.error ?: "Registration failed")
                            _toastMessage.emit(UiText.StringResource(R.string.registration_failed))
                        }
                    }
                }
                .catch { e ->
                    // Handle unexpected crashes/exceptions
                    _authState.value = AuthUiState.Error(e.message ?: "Registration failed")
                    _toastMessage.emit(UiText.DynamicString(e.message ?: "Registration failed"))
                }
                .collectLatest { } // Trigger the flow
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthUiState.Error("Please enter email and password")
            viewModelScope.launch { _toastMessage.emit(UiText.StringResource(R.string.enter_email_password)) }
            return
        }
        viewModelScope.launch {
                authRepository.login(email, password)
                .onEach { resource ->
                    when (resource) {
                        is Resource.Loading -> _authState.value = AuthUiState.Loading
                        is Resource.Success -> {
                            resource.data?.let { user ->
                                _authState.value = AuthUiState.Success(user)
                                _currentUser.value = user  // Set currentUser
                                _toastMessage.emit(UiText.StringResource(R.string.login_successful))
                            }
                        }
                        is Resource.Error -> {
                            _authState.value = AuthUiState.Error(resource.error ?: "Login failed")
                            _toastMessage.emit(UiText.StringResource(R.string.login_failed))
                        }
                    }
                }
                .collectLatest { }
        }
    }

    // --- Google Sign-In Logic ---
    // This receives the ID Token from Google (via MainActivity) and sends it to Firebase
    fun googleSignIn(idToken: String) {
        if (idToken.isBlank()) {
            _googleSignInState.value = AuthUiState.Error("Invalid Google token")
            viewModelScope.launch { _toastMessage.emit(UiText.StringResource(R.string.invalid_google_token)) }
            return
        }
        viewModelScope.launch {
            authRepository.googleSignIn(idToken)
                .onEach { resource ->
                    when (resource) {
                        is Resource.Loading -> _googleSignInState.value = AuthUiState.Loading
                        is Resource.Success -> {
                            resource.data?.let { user ->
                                // Success! Update state and current user
                                _googleSignInState.value = AuthUiState.Success(user)
                                _currentUser.value = user
                                _toastMessage.emit(UiText.StringResource(R.string.google_signin_successful))
                            }
                        }
                        is Resource.Error -> {
                            _googleSignInState.value = AuthUiState.Error(resource.error ?: "Google sign-in failed")
                            _toastMessage.emit(UiText.StringResource(R.string.google_signin_failed))
                        }
                    }
                }
                .catch { e ->
                    _googleSignInState.value = AuthUiState.Error(e.message ?: "Google sign-in failed")
                    _toastMessage.emit(UiText.DynamicString(e.message ?: "Google sign-in failed"))
                }
                .collectLatest { }
        }
    }

    fun logout() {
        // Reset auth states immediately to prevent navigation loops
        _authState.value = AuthUiState.Idle
        _currentUser.value = null  // Set immediately to trigger navigation
        _googleSignInState.value = AuthUiState.Idle
        _logoutState.value = AuthUiState.Loading
        _passwordResetState.value = AuthUiState.Idle

        viewModelScope.launch {
            try {
                authRepository.logout().collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _logoutState.value = AuthUiState.Idle
                            _toastMessage.emit(UiText.StringResource(R.string.logout_successful))
                        }
                        is Resource.Error -> {
                            _logoutState.value = AuthUiState.Error(resource.error ?: "Logout failed")
                            _toastMessage.emit(UiText.StringResource(R.string.logout_failed))
                        }
                        is Resource.Loading -> {
                            _logoutState.value = AuthUiState.Loading
                        }
                    }
                }
            } catch (e: Exception) {
                _logoutState.value = AuthUiState.Error(e.message ?: "Logout failed")
                _toastMessage.emit(UiText.DynamicString(e.message ?: "Logout failed"))
            }
        }
    }

    // --- Session Management ---
    // Checks if a user is currently logged in (e.g., from a previous session)
    private fun getCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUser()
                .onEach { user ->
                    _currentUser.value = user // Update the user state
                    _isAuthCheckComplete.value = true // Mark the check as done so the Splash Screen can dismiss
                }
                .launchIn(viewModelScope)
        }
    }

    fun clearAuthState() {
        _authState.value = AuthUiState.Idle
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            authRepository.updateUser(user)
                .onEach { resource ->
                    when (resource) {
                        is Resource.Loading -> _authState.value = AuthUiState.Loading
                        is Resource.Success -> {
                            resource.data?.let { updatedUser ->
                                _currentUser.value = updatedUser
                                _authState.value = AuthUiState.Success(updatedUser)
                                _toastMessage.emit(UiText.StringResource(R.string.profile_update_successful))
                            }
                        }
                        is Resource.Error -> {
                            _toastMessage.emit(UiText.StringResource(R.string.update_failed))
                        }
                    }
                }
                .catch { e ->
                    _toastMessage.emit(UiText.DynamicString(e.message ?: "Update failed"))
                }
                .collectLatest { }
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _passwordResetState.value = AuthUiState.Error("Please enter a valid email address")
            viewModelScope.launch { _toastMessage.emit(UiText.StringResource(R.string.enter_valid_email)) }
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _passwordResetState.value = AuthUiState.Error("Please enter a valid email address")
            viewModelScope.launch { _toastMessage.emit(UiText.StringResource(R.string.enter_valid_email)) }
            return
        }

        viewModelScope.launch {
            try {
                authRepository.resetPassword(email).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> _passwordResetState.value = AuthUiState.Loading
                        is Resource.Success -> {
                            _passwordResetState.value = AuthUiState.Success(User("", email, "Reset Email Sent")) // Dummy user for success
                            _toastMessage.emit(UiText.StringResource(R.string.reset_email_sent_success))
                        }
                        is Resource.Error -> {
                            _passwordResetState.value = AuthUiState.Error(resource.error ?: "Failed to send reset email")
                            _toastMessage.emit(UiText.StringResource(R.string.reset_email_failed))
                        }
                    }
                }
            } catch (e: Exception) {
                _passwordResetState.value = AuthUiState.Error(e.message ?: "Failed to send reset email")
                _toastMessage.emit(UiText.DynamicString(e.message ?: "Failed to send reset email"))
            }
        }
    }

    fun resetPasswordResetState() {
        _passwordResetState.value = AuthUiState.Idle
    }
}
