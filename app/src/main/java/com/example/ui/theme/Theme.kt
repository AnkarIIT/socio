package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FrostedAccentIce,
    onPrimary = FrostedAccentIceDark,
    secondary = InstaPink,
    onSecondary = Color.White,
    tertiary = FrostedAccentIceEnd,
    background = FrostedCanvas,
    onBackground = FrostedTextPrimary,
    surface = FrostedCardSolid,
    onSurface = FrostedTextPrimary,
    outline = FrostedCardBorder,
    surfaceVariant = FrostedSurfaceVariantSolid,
    onSurfaceVariant = FrostedTextSecondary
)

private val LightColorScheme = darkColorScheme( // Frosted Glass aesthetic is dark obsidian
    primary = FrostedAccentIce,
    onPrimary = FrostedAccentIceDark,
    secondary = InstaPink,
    onSecondary = Color.White,
    tertiary = FrostedAccentIceEnd,
    background = FrostedCanvas,
    onBackground = FrostedTextPrimary,
    surface = FrostedCardSolid,
    onSurface = FrostedTextPrimary,
    outline = FrostedCardBorder,
    surfaceVariant = FrostedSurfaceVariantSolid,
    onSurfaceVariant = FrostedTextSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep TeaGram brand aesthetic consistent
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
