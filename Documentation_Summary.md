# Documentation Summary

This document summarizes the comprehensive inline documentation added to the project's UI screens. Each file now contains a "HOW IT WORKS" section at the top, explaining its core functionality, state management, and UI components.

## Documented Screens

### Authentication & Onboarding
*   **`MainActivity.kt`**: The app's entry point. Handles global configuration (Locale, Theme), Authentication setup (Google Sign-In), Navigation structure (Scaffold, BottomBar), and Voice Search integration.
*   **`NavGraph.kt`**: Defines the app's navigation structure, screen transitions (animations), and route handling.
*   **`SplashScreen.kt`**: Explains the initialization logic, custom Canvas logo drawing, and navigation delay.
*   **`LoginScreen.kt`**: Details the Email/Password and Google Sign-In flows, state management, and UI feedback.
*   **`RegisterScreen.kt`**: Covers form validation (password mismatch), Google Sign-Up integration, and registration state handling.
*   **`ForgotPasswordScreen.kt`**: Explains the password reset flow, input validation, and success/error feedback.

### Main Navigation & Core Features
*   **`HomeScreen.kt`**: Describes the search functionality (Live & AI), voice input integration, category grid, and recent history display.
*   **`DepartmentsScreen.kt`**: Explains the category navigation grid and layout structure.
*   **`SettingsScreen.kt`**: Covers user profile management, theme switching (System/Light/Dark), language toggling (English/Bangla), and support features.

### Service Management
*   **`ServiceListScreen.kt`**: Details dynamic data fetching based on category and locale, along with loading/error state handling.
*   **`ServiceDetailScreen.kt`**: Explains rich content rendering (Markdown parsing, auto-linking), step-by-step guide visualization, and favorite toggling logic.
*   **`ServiceGuideScreen.kt`**: Describes the static guide prototype structure.

### User Data
*   **`FavoritesScreen.kt`**: Explains how favorite services are filtered from the main list and displayed.
*   **`HistoryScreen.kt`**: Details the sorting logic for recently viewed services (newest first) and data persistence.
*   **`ChatScreen.kt`**: Covers the chat interface, multi-modal input handling (text/files), and AI interaction logic.

## Key Concepts Documented
*   **State Management**: How `ViewModel` states (`uiState`, `authState`) drive the UI.
*   **Locale Awareness**: How screens react to language changes (`Locale` parameter).
*   **Navigation**: How screens transition and pass arguments (e.g., `serviceId`).
*   **UI Components**: Usage of reusable components like `ServiceRow`, `CategoryCard`, and `EnhancedTopAppBar`.
