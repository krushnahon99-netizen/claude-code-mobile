package com.claudecodemobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = ClaudeOrange,
    onPrimary = Ink,
    primaryContainer = EmberDark,
    onPrimaryContainer = EmberLight,
    inversePrimary = ClaudeOrangeDeep,
    secondary = Stone,
    onSecondary = Ink,
    secondaryContainer = InkHighest,
    onSecondaryContainer = Paper,
    tertiary = Stone,
    onTertiary = Ink,
    tertiaryContainer = InkHighest,
    onTertiaryContainer = Paper,
    background = Ink,
    onBackground = Paper,
    surface = Ink,
    onSurface = Paper,
    surfaceVariant = InkHigh,
    onSurfaceVariant = PaperMuted,
    inverseSurface = Paper,
    inverseOnSurface = Ink,
    outline = InkOutline,
    outlineVariant = InkLine,
    surfaceBright = InkBright,
    surfaceDim = Ink,
    surfaceContainerLowest = InkLowest,
    surfaceContainerLow = InkLow,
    surfaceContainer = InkMid,
    surfaceContainerHigh = InkHigh,
    surfaceContainerHighest = InkHighest,
)

private val LightColors = lightColorScheme(
    primary = ClaudeOrangeDeep,
    onPrimary = Color.White,
    primaryContainer = EmberLight,
    onPrimaryContainer = EmberInk,
    inversePrimary = ClaudeOrange,
    secondary = CharcoalMuted,
    onSecondary = Cream,
    secondaryContainer = CreamHighest,
    onSecondaryContainer = Charcoal,
    tertiary = CharcoalMuted,
    onTertiary = Cream,
    tertiaryContainer = CreamHighest,
    onTertiaryContainer = Charcoal,
    background = Cream,
    onBackground = Charcoal,
    surface = Cream,
    onSurface = Charcoal,
    surfaceVariant = CreamHigh,
    onSurfaceVariant = CharcoalMuted,
    inverseSurface = Charcoal,
    inverseOnSurface = Cream,
    outline = CreamOutline,
    outlineVariant = CreamLine,
    surfaceBright = Cream,
    surfaceDim = CreamHighest,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = CreamLow,
    surfaceContainer = CreamMid,
    surfaceContainerHigh = CreamHigh,
    surfaceContainerHighest = CreamHighest,
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
