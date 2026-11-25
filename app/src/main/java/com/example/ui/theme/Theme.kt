package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LangoLightColorScheme = lightColorScheme(
    primary = LangoEmerald,
    onPrimary = Color.White,
    primaryContainer = LangoEmeraldContainer,
    onPrimaryContainer = LangoOnEmeraldContainer,
    secondary = LangoTerracotta,
    onSecondary = Color.White,
    secondaryContainer = LangoTerracottaContainer,
    onSecondaryContainer = LangoOnTerracottaContainer,
    tertiary = LangoGold,
    onTertiary = LangoOnGoldContainer,
    tertiaryContainer = LangoGoldContainer,
    onTertiaryContainer = LangoOnGoldContainer,
    background = LangoIvory,
    onBackground = LangoCharcoal,
    surface = LangoSurfaceBright,
    onSurface = LangoCharcoal,
    surfaceVariant = LangoSand,
    onSurfaceVariant = LangoMuted,
    outline = LangoOutline,
    error = LangoError,
    errorContainer = LangoErrorContainer
)

private val LangoDarkColorScheme = darkColorScheme(
    primary = LangoDarkEmerald,
    onPrimary = LangoOnEmeraldContainer,
    primaryContainer = LangoDarkEmeraldContainer,
    onPrimaryContainer = LangoEmeraldContainer,
    secondary = LangoDarkTerracotta,
    onSecondary = LangoOnTerracottaContainer,
    secondaryContainer = LangoDarkTerracottaContainer,
    onSecondaryContainer = LangoTerracottaContainer,
    tertiary = LangoDarkGold,
    onTertiary = LangoOnGoldContainer,
    tertiaryContainer = Color(0xFF4F3800),
    onTertiaryContainer = LangoGoldContainer,
    background = LangoDarkBg,
    onBackground = LangoDarkOnSurface,
    surface = LangoDarkSurface,
    onSurface = LangoDarkOnSurface,
    surfaceVariant = LangoDarkSurfaceHigh,
    onSurfaceVariant = LangoDarkMuted,
    outline = Color(0xFF3E4A43),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A)
)

val LangoShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun LangoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) LangoDarkColorScheme else LangoLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = LangoTypography,
        shapes = LangoShapes,
        content = content
    )
}
