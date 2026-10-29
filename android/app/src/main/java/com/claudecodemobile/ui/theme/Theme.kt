package com.claudecodemobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = ClaudeOrange,
    onPrimary = Ink,
    primaryContainer = InkHigh,
    onPrimaryContainer = ClaudeOrange,
    secondary = PaperMuted,
    onSecondary = Ink,
    background = Ink,
    onBackground = Paper,
    surface = Ink,
    onSurface = Paper,
    surfaceVariant = InkHigh,
    onSurfaceVariant = PaperMuted,
    surfaceContainerLowest = Ink,
    surfaceContainerLow = InkRaised,
    surfaceContainer = InkRaised,
    surfaceContainerHigh = InkHigh,
    surfaceContainerHighest = InkHigh,
    outline = InkLine,
    outlineVariant = InkLine,
    error = Danger,
    onError = Ink,
)

private val LightColors = lightColorScheme(
    primary = ClaudeOrangeDeep,
    onPrimary = CreamRaised,
    primaryContainer = CreamHigh,
    onPrimaryContainer = ClaudeOrangeDeep,
    secondary = InkMuted,
    onSecondary = CreamRaised,
    background = Cream,
    onBackground = Ink,
    surface = Cream,
    onSurface = Ink,
    surfaceVariant = CreamHigh,
    onSurfaceVariant = InkMuted,
    surfaceContainerLowest = CreamRaised,
    surfaceContainerLow = CreamRaised,
    surfaceContainer = CreamRaised,
    surfaceContainerHigh = CreamHigh,
    surfaceContainerHighest = CreamHigh,
    outline = CreamLine,
    outlineVariant = CreamLine,
    error = Danger,
    onError = CreamRaised,
)

@Composable
fun ClaudeCodeMobileTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
