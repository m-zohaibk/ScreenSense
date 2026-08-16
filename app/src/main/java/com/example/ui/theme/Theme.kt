package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Calm Wellness Dark Color Scheme
private val CalmWellnessDarkColorScheme = darkColorScheme(
    primary = SageGreenLight,
    onPrimary = SageGreenDark,
    primaryContainer = SageGreenDark,
    onPrimaryContainer = SageGreenContainer,
    secondary = SoftCyanLight,
    onSecondary = SoftCyanDark,
    secondaryContainer = SoftCyanDark,
    onSecondaryContainer = SoftCyanContainer,
    tertiary = WarmAmberLight,
    onTertiary = WarmAmberDark,
    tertiaryContainer = WarmAmberDark,
    onTertiaryContainer = WarmAmberContainer,
    background = DeepMidnightBackground,
    onBackground = TextPrimary,
    surface = SlateSurfaceContainer,
    onSurface = TextPrimary,
    surfaceVariant = SlateElevatedSurface,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = SlateSurfaceContainer,
    surfaceContainerHigh = SlateElevatedSurface,
    surfaceContainerHighest = SlateSurfaceBorder,
    outline = Color(0x33FFFFFF), // subtle 1px border highlight
    outlineVariant = Color(0x1FFFFFFF)
)

// Calm Wellness Daylight Light Color Scheme
private val CalmWellnessLightColorScheme = lightColorScheme(
    primary = SageGreen,
    onPrimary = Color.White,
    primaryContainer = SageGreenContainer,
    onPrimaryContainer = SageGreenDark,
    secondary = SoftCyan,
    onSecondary = Color.White,
    secondaryContainer = SoftCyanContainer,
    onSecondaryContainer = SoftCyanDark,
    tertiary = WarmAmber,
    onTertiary = Color.White,
    tertiaryContainer = WarmAmberContainer,
    onTertiaryContainer = WarmAmberDark,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceContainer,
    onSurfaceVariant = LightTextSecondary,
    surfaceContainer = LightSurface,
    surfaceContainerHigh = LightSurfaceContainer,
    surfaceContainerHighest = Color(0xFFE2E8F0),
    outline = LightSurfaceBorder,
    outlineVariant = Color(0xFFCBD5E1)
)

@Composable
fun ScreenSenseTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> CalmWellnessDarkColorScheme
        else -> CalmWellnessLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
