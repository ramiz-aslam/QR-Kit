package com.app.qrkit.ui.navigation

import com.app.qrkit.ui.screens.MainPagerScreen

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.FastOutSlowInEasing
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.qrkit.ui.QrViewModel
import com.app.qrkit.ui.screens.ScanQrScreen
import com.app.qrkit.ui.screens.GenerateQrScreen
import com.app.qrkit.ui.screens.HomeScreen
import com.app.qrkit.ui.screens.SettingsScreen
import com.app.qrkit.ui.screens.QrDetailScreen
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import com.app.qrkit.ui.theme.UpdateSystemBars
import com.app.qrkit.ui.theme.isAppInDarkTheme
import com.app.qrkit.ui.screens.VaultScreen

private var lastNavigationTime = 0L

fun NavHostController.navigateSafely(route: String) {
    val currentTime = System.currentTimeMillis()
    if (currentTime - lastNavigationTime < 500L) {
        return
    }
    val currentEntry = currentBackStackEntry
    if (currentEntry != null && currentEntry.lifecycle.currentState != androidx.lifecycle.Lifecycle.State.RESUMED) {
        return
    }
    lastNavigationTime = currentTime
    navigate(route) {
        launchSingleTop = true
    }
}

fun NavHostController.popBackStackSafely(): Boolean {
    val currentTime = System.currentTimeMillis()
    if (currentTime - lastNavigationTime < 350L) {
        return false
    }
    val currentEntry = currentBackStackEntry
    if (currentEntry != null && currentEntry.lifecycle.currentState != androidx.lifecycle.Lifecycle.State.RESUMED) {
        return false
    }
    lastNavigationTime = currentTime
    return popBackStack()
}

fun NavHostController.navigateUpSafely(): Boolean {
    val currentTime = System.currentTimeMillis()
    if (currentTime - lastNavigationTime < 350L) {
        return false
    }
    val currentEntry = currentBackStackEntry
    if (currentEntry != null && currentEntry.lifecycle.currentState != androidx.lifecycle.Lifecycle.State.RESUMED) {
        return false
    }
    lastNavigationTime = currentTime
    return navigateUp()
}

@Composable
fun AppNavGraph(
    viewModel: QrViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("qr_kit_prefs", android.content.Context.MODE_PRIVATE) }
    val hasCompletedOnboarding = remember { sharedPrefs.getBoolean("onboarding_completed", false) }
    val startDestination = if (hasCompletedOnboarding) Screen.Home.route else Screen.Onboarding.route

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: startDestination

    val isDark = isAppInDarkTheme()

    val darkStatusIcons = when (currentRoute) {
        Screen.ScanQr.route -> false     // Always white icons on camera viewfinder
        else -> !isDark                  // Theme-dependent: black icons in light mode, white icons in dark mode
    }

    val darkNavigationIcons = when (currentRoute) {
        Screen.ScanQr.route -> false     // White icons on camera viewfinder
        else -> !isDark                  // Theme-dependent: black icons in light mode, white icons in dark mode
    }

    UpdateSystemBars(
        darkStatusIcons = darkStatusIcons,
        darkNavigationIcons = darkNavigationIcons
    )

    androidx.compose.animation.SharedTransitionLayout {
        val pushRoutes = setOf(
            Screen.GenerateQr.route,
            Screen.Settings.route,
            Screen.Feedback.route,
            Screen.Help.route,
            Screen.HelpFeedback.route,
            Screen.PrivacyPolicy.route,
            Screen.Terms.route,
            Screen.Onboarding.route
        )

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier,
            enterTransition = {
                if (targetState.destination.route in pushRoutes) {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    )
                } else {
                    fadeIn(animationSpec = tween(300))
                }
            },
            exitTransition = {
                if (targetState.destination.route in pushRoutes) {
                    slideOutHorizontally(
                        targetOffsetX = { -it / 4 },
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) + fadeOut(
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                        targetAlpha = 0.5f
                    )
                } else {
                    fadeOut(animationSpec = tween(300))
                }
            },
            popEnterTransition = {
                if (initialState.destination.route in pushRoutes) {
                    slideInHorizontally(
                        initialOffsetX = { -it / 4 },
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) + fadeIn(
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                        initialAlpha = 0.5f
                    )
                } else {
                    fadeIn(animationSpec = tween(300))
                }
            },
            popExitTransition = {
                if (initialState.destination.route in pushRoutes) {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    )
                } else {
                    fadeOut(animationSpec = tween(300))
                }
            }
        ) {
            composable(Screen.Home.route) {
                androidx.compose.runtime.CompositionLocalProvider(
                    LocalSharedTransitionScope provides this@SharedTransitionLayout,
                    LocalAnimatedVisibilityScope provides this@composable
                ) {
                    MainPagerScreen(
                        viewModel = viewModel,
                        onFabClick = { navController.navigateSafely(Screen.ScanQr.route) },
                        onQrClick = { id -> navController.navigateSafely(Screen.QrDetail.createRoute(id)) },
                        onGenerateClick = { navController.navigateSafely(Screen.GenerateQr.route) },
                        onSettingsClick = { navController.navigateSafely(Screen.Settings.route) },
                        animatedVisibilityScope = this@composable
                    )
                }
            }
            composable(Screen.ScanQr.route) {
                androidx.compose.runtime.CompositionLocalProvider(
                    LocalSharedTransitionScope provides this@SharedTransitionLayout,
                    LocalAnimatedVisibilityScope provides this@composable
                ) {
                    ScanQrScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStackSafely() }
                    )
                }
            }
            composable(Screen.GenerateQr.route) {
                androidx.compose.runtime.CompositionLocalProvider(
                    LocalSharedTransitionScope provides this@SharedTransitionLayout,
                    LocalAnimatedVisibilityScope provides this@composable
                ) {
                    GenerateQrScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStackSafely() },
                        onSuccess = {
                            navController.popBackStackSafely()
                        }
                    )
                }
            }
            composable(Screen.QrDetail.route) { backStackEntry ->
                androidx.compose.runtime.CompositionLocalProvider(
                    LocalSharedTransitionScope provides this@SharedTransitionLayout,
                    LocalAnimatedVisibilityScope provides this@composable
                ) {
                    val qrId = backStackEntry.arguments?.getString("qrId")
                    QrDetailScreen(
                        qrId = qrId,
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStackSafely() },
                        animatedVisibilityScope = this@composable
                    )
                }
            }
            composable(Screen.Settings.route) {
                com.app.qrkit.ui.screens.SettingsScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUpSafely() },
                    onNavigate = { route -> navController.navigateSafely(route) }
                )
            }
            composable(Screen.Feedback.route) {
                com.app.qrkit.ui.screens.FeedbackScreen(
                    onBackClick = { navController.navigateUpSafely() }
                )
            }
            composable(Screen.Help.route) {
                com.app.qrkit.ui.screens.HelpScreen(
                    onBackClick = { navController.navigateUpSafely() }
                )
            }
            composable(Screen.HelpFeedback.route) {
                com.app.qrkit.ui.screens.HelpScreen(
                    onBackClick = { navController.navigateUpSafely() }
                )
            }
            composable(Screen.PrivacyPolicy.route) {
                com.app.qrkit.ui.screens.PrivacyPolicyScreen(
                    onBackClick = { navController.navigateUpSafely() }
                )
            }
            composable(Screen.Terms.route) {
                com.app.qrkit.ui.screens.TermsScreen(
                    onBackClick = { navController.navigateUpSafely() }
                )
            }
            composable(Screen.Onboarding.route) {
                com.app.qrkit.ui.screens.OnboardingScreen(
                    onFinish = {
                        val editor = sharedPrefs.edit()
                        editor.putBoolean("onboarding_completed", true)
                        editor.apply()
                        if (!navController.popBackStackSafely()) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
    }
}

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Vault : Screen("vault")
    object Settings : Screen("settings")
    object Feedback : Screen("feedback")
    object Help : Screen("help")
    object HelpFeedback : Screen("help_feedback")
    object PrivacyPolicy : Screen("privacy_policy")
    object Terms : Screen("terms")
    object ScanQr : Screen("scan_qr")
    object GenerateQr : Screen("generate_qr")
    object Onboarding : Screen("onboarding")
    object QrDetail : Screen("qr_detail/{qrId}") {
        fun createRoute(qrId: String) = "qr_detail/$qrId"
    }
    object CategoryDetail : Screen("category_detail/{categoryName}") {
        fun createRoute(categoryName: String) = "category_detail/$categoryName"
    }
}



















