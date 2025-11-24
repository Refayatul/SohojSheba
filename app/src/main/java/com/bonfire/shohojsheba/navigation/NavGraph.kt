package com.bonfire.shohojsheba.navigation

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onVoiceSearchClick: () -> Unit,
    currentThemeMode: String,
    onThemeChange: (String) -> Unit,
    googleSignInLauncher: ActivityResultLauncher<Intent>? = null,
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    // Determine startDestination based on auth state
    val startDestination = remember(currentUser) {
        if (currentUser != null) Routes.HOME else Routes.LOGIN
    }

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(
            route = Routes.LOGIN,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
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
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
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
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
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
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
            }
        ) {
            HomeScreen(
                navController = navController,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onVoiceSearchClick = onVoiceSearchClick
            )
        }

        composable(
            route = Routes.DEPARTMENTS,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
            }
        ) {
            DepartmentsScreen(navController = navController)
        }

        composable(
            route = Routes.CITIZEN_SERVICES,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
            }
        ) {
            ServiceListScreen(navController, "citizen", R.string.category_citizen)
        }

        composable(
            route = Routes.FARMER_SERVICES,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
            }
        ) {
            ServiceListScreen(navController, "farmer", R.string.category_farmer)
        }

        composable(
            route = Routes.ENTREPRENEUR_SERVICES,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
            }
        ) {
            ServiceListScreen(navController, "entrepreneur", R.string.category_entrepreneur)
        }

        composable(
            route = Routes.GOVT_OFFICE_SERVICES,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
            }
        ) {
            ServiceListScreen(navController, "govt_office", R.string.category_govt_office)
        }

        composable(
            route = "${Routes.SERVICE_DETAIL}/{serviceId}",
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
            }
        ) { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getString("serviceId")
            if (!serviceId.isNullOrBlank()) {
                ServiceDetailScreen(navController = navController, serviceId = serviceId)
            }
        }

        composable(
            route = Routes.CHAT,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
            }
        ) {
            ChatScreen(navController = navController)
        }

        composable(
            route = Routes.HISTORY,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
            }
        ) {
            HistoryScreen(navController = navController)
        }

        composable(
            route = Routes.FAVORITES,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
            }
        ) {
            FavoritesScreen(navController = navController)
        }

        composable(
            route = Routes.SETTINGS,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(500))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = FastOutSlowInEasing)) + fadeOut(tween(500))
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
