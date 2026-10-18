package com.nilasharafi.promptvault.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nilasharafi.promptvault.data.repository.PromptRepository
import com.nilasharafi.promptvault.ui.components.AppDrawerContent
import com.nilasharafi.promptvault.ui.components.BottomNavBar
import com.nilasharafi.promptvault.ui.screens.CategoriesScreen
import com.nilasharafi.promptvault.ui.screens.DetailScreen
import com.nilasharafi.promptvault.ui.screens.FavoritesScreen
import com.nilasharafi.promptvault.ui.screens.ForceUpdateScreen
import com.nilasharafi.promptvault.ui.screens.HomeScreen
import com.nilasharafi.promptvault.ui.screens.LoadingContentScreen
import com.nilasharafi.promptvault.ui.screens.MaintenanceScreen
import com.nilasharafi.promptvault.ui.screens.OfflineScreen
import com.nilasharafi.promptvault.ui.screens.OnboardingScreen
import com.nilasharafi.promptvault.ui.screens.SearchScreen
import com.nilasharafi.promptvault.ui.screens.SettingsScreen
import kotlinx.coroutines.launch

@Composable
fun PromptVaultAppScreen(
    repository: PromptRepository,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    val config by repository.configState.collectAsState()
    val promptsResponse by repository.promptsState.collectAsState()
    val hasSeenOnboarding by repository.hasSeenOnboarding.collectAsState(initial = false)
    val lastError by repository.lastError.collectAsState()

    var isInitialLoading by remember { mutableStateOf(false) }
    var userChoseViewCached by remember { mutableStateOf(false) }

    // Maintenance Mode Check
    if (config.app.maintenanceMode) {
        MaintenanceScreen(message = config.app.maintenanceMessage)
        return
    }

    // Force Update Check
    if (config.app.forceUpdate) {
        ForceUpdateScreen(updateUrl = config.app.updateUrl)
        return
    }

    // Onboarding Check
    if (!hasSeenOnboarding) {
        OnboardingScreen(
            onGetStarted = {
                scope.launch {
                    repository.setOnboardingSeen(true)
                    isInitialLoading = true
                }
            }
        )
        return
    }

    // Initial Loading animation
    if (isInitialLoading) {
        LoadingContentScreen(
            onFinished = {
                isInitialLoading = false
            }
        )
        return
    }

    // Offline screen when no items in cache and network error occurred
    if (promptsResponse.items.isEmpty() && lastError != null && !userChoseViewCached) {
        OfflineScreen(
            onTryAgain = {
                scope.launch {
                    repository.refreshAll(force = true)
                }
            },
            onViewCached = {
                userChoseViewCached = true
            }
        )
        return
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    if (route != currentRoute) {
                        navController.navigate(route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onRateApp = {
                    scope.launch { drawerState.close() }
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
                    try { context.startActivity(intent) } catch (_: Exception) { }
                },
                onShareApp = {
                    scope.launch { drawerState.close() }
                    val share = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "PromptVault: Discover AI Art & Prompts: https://play.google.com/store/apps/details?id=${context.packageName}")
                    }
                    context.startActivity(Intent.createChooser(share, "Share App"))
                },
                onPrivacyPolicy = {
                    scope.launch { drawerState.close() }
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://nilasharafi.github.io/PromptVault/privacy.html")))
                },
                onPremiumPrompts = {
                    scope.launch { drawerState.close() }
                    navController.navigate("home")
                }
            )
        }
    ) {
        Scaffold(
            bottomBar = {
                // Show bottom navigation bar on primary screens
                if (currentRoute in listOf("home", "categories", "favorites", "settings")) {
                    BottomNavBar(
                        currentRoute = currentRoute,
                        onTabSelected = { route ->
                            if (route != currentRoute) {
                                navController.navigate(route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                enterTransition = { fadeIn(tween(250)) },
                exitTransition = { fadeOut(tween(250)) }
            ) {
                // Home Screen
                composable("home") {
                    HomeScreen(
                        repository = repository,
                        onPromptClick = { item ->
                            navController.navigate("detail/${item.id}")
                        },
                        onSearchClick = {
                            navController.navigate("search")
                        },
                        onPremiumClick = {
                            navController.navigate("search")
                        },
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        }
                    )
                }

                // Detail Screen
                composable(
                    route = "detail/{itemId}",
                    arguments = listOf(navArgument("itemId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                    DetailScreen(
                        itemId = itemId,
                        repository = repository,
                        onBack = { navController.popBackStack() },
                        onRelatedItemClick = { related ->
                            navController.navigate("detail/${related.id}") {
                                popUpTo("home")
                            }
                        }
                    )
                }

                // Categories Screen
                composable("categories") {
                    CategoriesScreen(
                        repository = repository,
                        onCategoryClick = { _ ->
                            navController.navigate("home")
                        },
                        onBack = {
                            navController.navigate("home")
                        }
                    )
                }

                // Search Screen
                composable("search") {
                    SearchScreen(
                        repository = repository,
                        onPromptClick = { item ->
                            navController.navigate("detail/${item.id}")
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                // Favorites Screen
                composable("favorites") {
                    FavoritesScreen(
                        repository = repository,
                        onPromptClick = { item ->
                            navController.navigate("detail/${item.id}")
                        },
                        onBack = { navController.navigate("home") }
                    )
                }

                // Settings Screen
                composable("settings") {
                    SettingsScreen(
                        repository = repository,
                        onBack = { navController.navigate("home") }
                    )
                }
            }
        }
    }
}
