package com.memoir.app.presentation.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Memoir light color scheme
 */
private val LightColorScheme = lightColorScheme(
    primary = Mustard,
    onPrimary = TextOnPrimary,
    primaryContainer = Mustard.copy(alpha = 0.2f),
    onPrimaryContainer = DarkOlive,

    secondary = CarrotOrange,
    onSecondary = SurfaceLight,
    secondaryContainer = CarrotOrange.copy(alpha = 0.2f),
    onSecondaryContainer = DarkOlive,

    tertiary = DarkOlive,
    onTertiary = SurfaceLight,

    error = Error,
    onError = SurfaceLight,
    errorContainer = Error.copy(alpha = 0.1f),
    onErrorContainer = Error,

    background = BackgroundLight,
    onBackground = TextPrimary,

    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundLight,
    onSurfaceVariant = TextSecondary,

    outline = BorderLight,
    outlineVariant = BorderLight.copy(alpha = 0.5f),
)

/**
 * Memoir dark color scheme
 * Note: Dark mode is not in MVP scope, but prepared for Phase 2
 */
private val DarkColorScheme = darkColorScheme(
    primary = Mustard,
    onPrimary = DarkOlive,
    primaryContainer = Mustard.copy(alpha = 0.3f),
    onPrimaryContainer = SurfaceLight,

    secondary = CarrotOrange,
    onSecondary = BackgroundDark,
    secondaryContainer = CarrotOrange.copy(alpha = 0.3f),
    onSecondaryContainer = SurfaceLight,

    tertiary = Mustard.copy(alpha = 0.8f),
    onTertiary = BackgroundDark,

    error = Error.copy(alpha = 0.9f),
    onError = BackgroundDark,
    errorContainer = Error.copy(alpha = 0.2f),
    onErrorContainer = Error.copy(alpha = 0.9f),

    background = BackgroundDark,
    onBackground = TextOnDark,

    surface = SurfaceDark,
    onSurface = TextOnDark,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextOnDark.copy(alpha = 0.7f),

    outline = BorderDark,
    outlineVariant = BorderDark.copy(alpha = 0.5f),
)

/**
 * Memoir theme composable
 * Applies Material3 theme with Memoir design system
 */
@Composable
fun MemoirTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    // For MVP, we disable it to maintain consistent branding
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Note: Dynamic color disabled for MVP to maintain brand consistency
        // dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        //     val context = LocalContext.current
        //     if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        // }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
