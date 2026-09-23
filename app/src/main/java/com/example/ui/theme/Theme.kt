package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = BlackBackground,
    primaryContainer = PureWhite,
    onPrimaryContainer = BlackBackground,
    secondary = OffWhite,
    onSecondary = BlackBackground,
    secondaryContainer = DarkCardElevated,
    onSecondaryContainer = PureWhite,
    tertiary = YouTubeRed,
    onTertiary = PureWhite,
    background = BlackBackground,
    onBackground = PureWhite,
    surface = DarkSurface,
    onSurface = PureWhite,
    surfaceVariant = DarkCard,
    onSurfaceVariant = OffWhite,
    outline = DarkBorder,
    outlineVariant = DarkBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

