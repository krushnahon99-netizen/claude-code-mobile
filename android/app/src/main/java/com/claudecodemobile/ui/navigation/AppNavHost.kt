package com.claudecodemobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.claudecodemobile.ui.components.PlaceholderScreen
import com.claudecodemobile.ui.components.ScreenAction

private val projectArgument = listOf(navArgument(Routes.PROJECT_ARG) { type = NavType.StringType })

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.SETUP) {
        composable(Routes.SETUP) {
            PlaceholderScreen(
                title = "Setting up",
                milestone = 4,
                description = "Downloads Ubuntu, installs Node.js and Claude Code, and checks that claude runs. A progress bar shows each step.",
                actions = listOf(
                    ScreenAction("Continue") {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.SETUP) { inclusive = true }
                        }
                    },
                ),
            )
        }

        composable(Routes.LOGIN) {
            PlaceholderScreen(
                title = "Sign in",
                milestone = 9,
                description = "Sign in with your Anthropic account, or use an API key or a custom base URL and token.",
                actions = listOf(
                    ScreenAction("Continue") {
                        navController.navigate(Routes.PROJECTS) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                ),
            )
        }

        composable(Routes.PROJECTS) {
            PlaceholderScreen(
                title = "Projects",
                milestone = 8,
                description = "Pick or create a project folder. Claude Code runs inside the folder you open.",
                actions = listOf(
                    ScreenAction("Open demo project") { navController.navigate(Routes.chat("demo")) },
                    ScreenAction("Settings") { navController.navigate(Routes.SETTINGS) },
                ),
            )
        }

        composable(Routes.CHAT, arguments = projectArgument) { entry ->
            val project = entry.arguments?.getString(Routes.PROJECT_ARG).orEmpty()
            PlaceholderScreen(
                title = project,
                milestone = 6,
                description = "Chat with Claude Code: streaming replies, tool cards, diffs for edits, and Approve or Deny for every edit and command.",
                onBack = { navController.popBackStack() },
                actions = listOf(
                    ScreenAction("Browse files") { navController.navigate(Routes.files(project)) },
                ),
            )
        }

        composable(Routes.FILES, arguments = projectArgument) { entry ->
            val project = entry.arguments?.getString(Routes.PROJECT_ARG).orEmpty()
            PlaceholderScreen(
                title = "Files · $project",
                milestone = 8,
                description = "A simple file browser and viewer for the project folder.",
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SETTINGS) {
            PlaceholderScreen(
                title = "Settings",
                milestone = 9,
                description = "Account, API key, base URL, model, and permission mode. Secrets are encrypted with the Android Keystore.",
                onBack = { navController.popBackStack() },
            )
        }
    }
}
