package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AppleBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1B2838),
    onPrimaryContainer = Color(0xFFD0E4FF),
    background = AppleDarkBackground,
    onBackground = Color.White,
    surface = AppleDarkSurface,
    onSurface = Color.White,
    surfaceVariant = AppleDarkSurfaceVariant,
    onSurfaceVariant = AppleTextSecondary,
    outline = AppleDarkBorder,
    outlineVariant = Color(0xFF2C2C2E)
)

private val LightColorScheme = lightColorScheme(
    primary = AppleBlue,
    onPrimary = Color.White,
    primaryContainer = AppleBlueLight,
    onPrimaryContainer = AppleBluePressed,
    background = AppleLightBackground,
    onBackground = AppleTextPrimary,
    surface = AppleLightSurface,
    onSurface = AppleTextPrimary,
    surfaceVariant = AppleLightSurfaceVariant,
    onSurfaceVariant = AppleTextSecondary,
    outline = AppleLightBorder,
    outlineVariant = AppleLightBorderSubtle
)

@Composable
fun SalimTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We intentionally enforce Apple's iconic light mode aesthetic or clean dark mode
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SalimTheme(darkTheme = darkTheme, content = content)
}
