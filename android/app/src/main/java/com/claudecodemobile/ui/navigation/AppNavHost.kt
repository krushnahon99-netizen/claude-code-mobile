package com.claudecodemobile.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.claudecodemobile.ui.chat.ChatScreen
import com.claudecodemobile.ui.files.FilesScreen
import com.claudecodemobile.ui.login.LoginScreen
import com.claudecodemobile.ui.projects.ProjectsScreen
import com.claudecodemobile.ui.settings.SettingsScreen
import com.claudecodemobile.ui.setup.SetupScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SetupRoute,
        enterTransition = { fadeIn(tween(durationMillis = 220)) },
        exitTransition = { fadeOut(tween(durationMillis = 220)) },
    ) {
        composable<SetupRoute> {
            SetupScreen(onSetupComplete = { navController.navigate(LoginRoute) })
        }
        composable<LoginRoute> {
            LoginScreen(
                onSignedIn = {
                    navController.navigate(ProjectsRoute) {
                        popUpTo<SetupRoute> { inclusive = true }
                    }
                },
            )
        }
        composable<ProjectsRoute> {
            ProjectsScreen(
                onOpenProject = { project -> navController.navigate(ChatRoute(project)) },
                onOpenSettings = { navController.navigate(SettingsRoute) },
            )
        }
        composable<ChatRoute> { entry ->
            val route = entry.toRoute<ChatRoute>()
            ChatScreen(
                project = route.project,
                onBack = { navController.popBackStackFrom(entry) },
                onOpenFiles = { navController.navigate(FilesRoute(route.project)) },
                onOpenSettings = { navController.navigate(SettingsRoute) },
            )
        }
        composable<FilesRoute> { entry ->
            val route = entry.toRoute<FilesRoute>()
            FilesScreen(
                project = route.project,
                onBack = { navController.popBackStackFrom(entry) },
            )
        }
        composable<SettingsRoute> { entry ->
            SettingsScreen(onBack = { navController.popBackStackFrom(entry) })
        }
    }
}

// A second tap on Back during the exit animation would otherwise pop the screen underneath too.
private fun NavHostController.popBackStackFrom(entry: NavBackStackEntry) {
    if (entry.lifecycle.currentState == Lifecycle.State.RESUMED) {
        popBackStack()
    }
}
