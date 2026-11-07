package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

val TribeLightColorScheme = lightColorScheme(
    primary = TribeGreenPrimary,
    onPrimary = TribeLightOnPrimary,
    primaryContainer = TribeLightPrimaryContainer,
    onPrimaryContainer = TribeLightOnPrimaryContainer,
    secondary = TribeGreenLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8F5EE),
    onSecondaryContainer = Color(0xFF0F3B2B),
    tertiary = TribeMintAccent,
    onTertiary = Color(0xFF003822),
    background = TribeLightBackground,
    onBackground = TribeLightOnSurface,
    surface = TribeLightSurface,
    onSurface = TribeLightOnSurface,
    surfaceVariant = TribeLightSurfaceVariant,
    onSurfaceVariant = TribeLightOnSurfaceVariant,
    outline = TribeLightOutline,
    outlineVariant = TribeLightOutlineVariant,
    error = StatusCancelled,
    onError = Color.White
)

val TribeDarkColorScheme = darkColorScheme(
    primary = TribeDarkPrimary,
    onPrimary = TribeDarkOnPrimary,
    primaryContainer = TribeDarkPrimaryContainer,
    onPrimaryContainer = TribeDarkOnPrimaryContainer,
    secondary = TribeMintAccent,
    onSecondary = Color(0xFF003920),
    secondaryContainer = Color(0xFF1E3A2E),
    onSecondaryContainer = Color(0xFFA5E6C9),
    tertiary = TribeGreenLight,
    onTertiary = Color.White,
    background = TribeDarkBackground,
    onBackground = TribeDarkOnSurface,
    surface = TribeDarkSurface,
    onSurface = TribeDarkOnSurface,
    surfaceVariant = TribeDarkSurfaceVariant,
    onSurfaceVariant = TribeDarkOnSurfaceVariant,
    outline = TribeDarkOutline,
    outlineVariant = TribeDarkOutlineVariant,
    error = StatusCancelled,
    onError = Color.White
)

@Composable
fun TribeTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) TribeDarkColorScheme else TribeLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TribeTypography,
        shapes = TribeShapes,
        content = content
    )
}
