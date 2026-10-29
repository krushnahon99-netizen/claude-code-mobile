package com.claudecodemobile.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object SetupRoute

@Serializable
data object LoginRoute

@Serializable
data object ProjectsRoute

@Serializable
data class ChatRoute(val project: String)

@Serializable
data class FilesRoute(val project: String)

@Serializable
data object SettingsRoute
