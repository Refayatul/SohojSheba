
package com.bonfire.shohojsheba.navigation

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bonfire.shohojsheba.R
import com.bonfire.shohojsheba.ui.screens.*
import com.bonfire.shohojsheba.ui.viewmodels.AuthViewModel
import com.bonfire.shohojsheba.ui.viewmodels.ViewModelFactory
import androidx.compose.ui.platform.LocalContext

/**
 * =========================================================================================
 *                                   APP NAVIGATION GRAPH
 * =========================================================================================
 * 
 * HOW IT WORKS:
 * 1.  **Navigation Container**:
 *     -   `NavHost` acts as the container for swapping screens.
 *     -   It observes the `navController` to know which screen to display.
 * 
 * 2.  **Route Definition**:
 *     -   Each screen is defined using the `composable(route = ...)` function.
 *     -   Routes are unique string paths (e.g., "home", "login", "service_detail/{id}").
 * 
 * 3.  **Screen Transitions**:
 *     -   **Android 15 Depth Stack**: Mimics the native OS feel. New screens slide in, while the background screen scales down (0.92x) and fades, creating a premium 3D depth effect.
 *     -   **Physics**: Using `Spring.StiffnessMediumLow` for fluid, organic motion that matches the system's predictive back gestures.
 *     -   **Chat Screen**: Slides up from the bottom for a modal-like feel.
 * 
 * 4.  **Argument Passing**:
 *     -   Routes can have dynamic arguments (e.g., `serviceId`).
 *     -   These are extracted from `backStackEntry.arguments` and passed to the destination screen.
 * 
 * 5.  **Auth Logic**:
 *     -   `startDestination` is dynamically calculated based on `currentUser`.
 *     -   If logged in -> Home; otherwise -> Login.
 * =========================================================================================
 */

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onVoiceSearchClick: () -> Unit,
    currentThemeMode: String,
    onThemeChange: (String) -> Unit,
    isVoiceInput: Boolean,
    onVoiceInputReset: () -> Unit,
    locale: java.util.Locale,
    googleSignInLauncher: ActivityResultLauncher<Intent>? = null,
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    // Determine startDestination based on auth state
    val startDestination = remember(currentUser) {
        if (currentUser != null) Routes.HOME else Routes.LOGIN
    }

    // 'NavHost' is the container that swaps screens in and out.
    // It's like a TV that changes channels based on the 'route'.
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        // A 'composable' is a single screen or page in the app.
        // 'route' is the unique name (URL) for this screen.
        composable(
            route = Routes.LOGIN,
            // Android 15 Depth Stack Animation
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, spring(stiffness = Spring.StiffnessLow)) + fadeIn(spring(stiffness = Spring.StiffnessLow))
            },
            exitTransition = {
                scaleOut(targetScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            popEnterTransition = {
                scaleIn(initialScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, spring(stiffness = Spring.StiffnessLow)) + fadeOut(spring(stiffness = Spring.StiffnessLow))
            }
        ) {
            LoginScreen(
                navController = navController,
                googleSignInLauncher = googleSignInLauncher,
                authViewModel = authViewModel
            )
        }

        composable(
            route = Routes.REGISTER,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, spring(stiffness = Spring.StiffnessLow)) + fadeIn(spring(stiffness = Spring.StiffnessLow))
            },
            exitTransition = {
                scaleOut(targetScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            popEnterTransition = {
                scaleIn(initialScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, spring(stiffness = Spring.StiffnessLow)) + fadeOut(spring(stiffness = Spring.StiffnessLow))
            }
        ) {
            RegisterScreen(
                navController = navController,
                googleSignInLauncher = googleSignInLauncher,
                authViewModel = authViewModel
            )
        }

        composable(
            route = Routes.FORGOT_PASSWORD,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, spring(stiffness = Spring.StiffnessLow)) + fadeIn(spring(stiffness = Spring.StiffnessLow))
            },
            exitTransition = {
                scaleOut(targetScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            popEnterTransition = {
                scaleIn(initialScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, spring(stiffness = Spring.StiffnessLow)) + fadeOut(spring(stiffness = Spring.StiffnessLow))
            }
        ) {
            ForgotPasswordScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }

        composable(
            route = Routes.HOME,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, spring(stiffness = Spring.StiffnessLow)) + fadeIn(spring(stiffness = Spring.StiffnessLow))
            },
            exitTransition = {
                scaleOut(targetScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            popEnterTransition = {
                scaleIn(initialScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, spring(stiffness = Spring.StiffnessLow)) + fadeOut(spring(stiffness = Spring.StiffnessLow))
            }
        ) {
            HomeScreen(
                navController = navController,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                isVoiceInput = isVoiceInput,
                onVoiceInputReset = onVoiceInputReset,
                onVoiceSearchClick = onVoiceSearchClick,
                locale = locale
            )
        }

        composable(
            route = Routes.DEPARTMENTS,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, spring(stiffness = Spring.StiffnessLow)) + fadeIn(spring(stiffness = Spring.StiffnessLow))
            },
            exitTransition = {
                scaleOut(targetScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            popEnterTransition = {
                scaleIn(initialScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, spring(stiffness = Spring.StiffnessLow)) + fadeOut(spring(stiffness = Spring.StiffnessLow))
            }
        ) {
            DepartmentsScreen(navController = navController)
        }

        composable(
            route = Routes.CITIZEN_SERVICES,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, spring(stiffness = Spring.StiffnessLow)) + fadeIn(spring(stiffness = Spring.StiffnessLow))
            },
            exitTransition = {
                scaleOut(targetScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            popEnterTransition = {
                scaleIn(initialScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, spring(stiffness = Spring.StiffnessLow)) + fadeOut(spring(stiffness = Spring.StiffnessLow))
            }
        ) {
            ServiceListScreen(navController, "citizen", R.string.category_citizen, locale)
        }

        composable(
            route = Routes.FARMER_SERVICES,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, spring(stiffness = Spring.StiffnessLow)) + fadeIn(spring(stiffness = Spring.StiffnessLow))
            },
            exitTransition = {
                scaleOut(targetScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            popEnterTransition = {
                scaleIn(initialScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, spring(stiffness = Spring.StiffnessLow)) + fadeOut(spring(stiffness = Spring.StiffnessLow))
            }
        ) {
            ServiceListScreen(navController, "farmer", R.string.category_farmer, locale)
        }

        composable(
            route = Routes.ENTREPRENEUR_SERVICES,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, spring(stiffness = Spring.StiffnessLow)) + fadeIn(spring(stiffness = Spring.StiffnessLow))
            },
            exitTransition = {
                scaleOut(targetScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            popEnterTransition = {
                scaleIn(initialScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, spring(stiffness = Spring.StiffnessLow)) + fadeOut(spring(stiffness = Spring.StiffnessLow))
            }
        ) {
            ServiceListScreen(navController, "entrepreneur", R.string.category_entrepreneur, locale)
        }

        composable(
            route = Routes.GOVT_OFFICE_SERVICES,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, spring(stiffness = Spring.StiffnessLow)) + fadeIn(spring(stiffness = Spring.StiffnessLow))
            },
            exitTransition = {
                scaleOut(targetScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            popEnterTransition = {
                scaleIn(initialScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, spring(stiffness = Spring.StiffnessLow)) + fadeOut(spring(stiffness = Spring.StiffnessLow))
            }
        ) {
            ServiceListScreen(navController, "govt_office", R.string.category_govt_office, locale)
        }

        composable(
            route = "${Routes.SERVICE_DETAIL}/{serviceId}",
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, spring(stiffness = Spring.StiffnessLow)) + fadeIn(spring(stiffness = Spring.StiffnessLow))
            },
            exitTransition = {
                scaleOut(targetScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            popEnterTransition = {
                scaleIn(initialScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, spring(stiffness = Spring.StiffnessLow)) + fadeOut(spring(stiffness = Spring.StiffnessLow))
            }
        ) { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getString("serviceId")
            if (!serviceId.isNullOrBlank()) {
                ServiceDetailScreen(navController = navController, serviceId = serviceId, locale = locale)
            }
        }

        composable(
            route = Routes.CHAT,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, spring(stiffness = Spring.StiffnessLow)) + fadeIn(spring(stiffness = Spring.StiffnessLow))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, spring(stiffness = Spring.StiffnessLow)) + fadeOut(spring(stiffness = Spring.StiffnessLow))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, spring(stiffness = Spring.StiffnessLow)) + fadeIn(spring(stiffness = Spring.StiffnessLow))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, spring(stiffness = Spring.StiffnessLow)) + fadeOut(spring(stiffness = Spring.StiffnessLow))
            }
        ) {
            ChatScreen(navController = navController)
        }

        composable(
            route = Routes.HISTORY,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, spring(stiffness = Spring.StiffnessLow)) + fadeIn(spring(stiffness = Spring.StiffnessLow))
            },
            exitTransition = {
                scaleOut(targetScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            popEnterTransition = {
                scaleIn(initialScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, spring(stiffness = Spring.StiffnessLow)) + fadeOut(spring(stiffness = Spring.StiffnessLow))
            }
        ) {
            HistoryScreen(navController = navController, locale = locale)
        }

        composable(
            route = Routes.FAVORITES,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, spring(stiffness = Spring.StiffnessLow)) + fadeIn(spring(stiffness = Spring.StiffnessLow))
            },
            exitTransition = {
                scaleOut(targetScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            popEnterTransition = {
                scaleIn(initialScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, spring(stiffness = Spring.StiffnessLow)) + fadeOut(spring(stiffness = Spring.StiffnessLow))
            }
        ) {
            FavoritesScreen(navController = navController, locale = locale)
        }

        composable(
            route = Routes.SETTINGS,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, spring(stiffness = Spring.StiffnessLow)) + fadeIn(spring(stiffness = Spring.StiffnessLow))
            },
            exitTransition = {
                scaleOut(targetScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            popEnterTransition = {
                scaleIn(initialScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, spring(stiffness = Spring.StiffnessLow)) + fadeOut(spring(stiffness = Spring.StiffnessLow))
            }
        ) {
            SettingsScreen(
                navController = navController,
                currentThemeMode = currentThemeMode,
                onThemeChange = onThemeChange
            )
        }
    }
}