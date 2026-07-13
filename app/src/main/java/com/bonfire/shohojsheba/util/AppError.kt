package com.bonfire.shohojsheba.util

sealed class AppError(val message: String) {
    class AuthenticationError(message: String = "Authentication failed") : AppError(message)
    class NetworkError(message: String = "Network error") : AppError(message)
    class UnknownError(message: String = "Unknown error") : AppError(message)
}
