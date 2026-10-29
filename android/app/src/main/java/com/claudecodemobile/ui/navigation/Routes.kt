package com.claudecodemobile.ui.navigation

import android.net.Uri

object Routes {
    const val PROJECT_ARG = "project"

    const val SETUP = "setup"
    const val LOGIN = "login"
    const val PROJECTS = "projects"
    const val CHAT = "chat/{$PROJECT_ARG}"
    const val FILES = "files/{$PROJECT_ARG}"
    const val SETTINGS = "settings"

    fun chat(project: String) = "chat/${Uri.encode(project)}"

    fun files(project: String) = "files/${Uri.encode(project)}"
}
