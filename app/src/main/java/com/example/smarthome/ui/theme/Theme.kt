package com.example.smarthome.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Esquemas mapeados desde los tokens CSS de la web para que fondos, tarjetas,
// textos y acentos sean los mismos en móvil y en web.
// surfaceTint = surface anula el tinte tonal de Material: en la web las
// superficies elevadas son planas (solo sombra), sin lavado de color.
private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = LightPrimaryBg,
    onPrimaryContainer = LightPrimaryDk,
    inversePrimary = DarkPrimary2,
    secondary = LightPrimary2,
    onSecondary = Color.White,
    secondaryContainer = LightPrimaryBg2,
    onSecondaryContainer = LightPrimaryDk,
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    tertiaryContainer = LightSuccessBg,
    onTertiaryContainer = SuccessGreen,
    background = LightBg,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightSurface2,
    onSurfaceVariant = LightText3,
    surfaceTint = LightSurface,
    inverseSurface = LightText,
    inverseOnSurface = LightSurface2,
    error = DangerRed,
    onError = Color.White,
    errorContainer = LightDangerBg,
    onErrorContainer = DangerRed,
    outline = LightMuted,
    outlineVariant = LightBorderLight,
    surfaceBright = LightSurface,
    surfaceDim = LightSurface3,
    surfaceContainerLowest = LightSurface,
    surfaceContainerLow = LightSurface,   // contenedor por defecto de Card = --surface
    surfaceContainer = LightSurface2,
    surfaceContainerHigh = LightSurface3,
    surfaceContainerHighest = LightSurface3,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color.White,              // la web mantiene texto blanco sobre el primario
    primaryContainer = DarkPrimaryBg,
    onPrimaryContainer = DarkPrimary2,
    inversePrimary = LightPrimary,
    secondary = DarkPrimary2,
    onSecondary = Color(0xFF1A1200),
    secondaryContainer = DarkPrimaryBg2,
    onSecondaryContainer = DarkPrimary2,
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    tertiaryContainer = DarkSuccessBg,
    onTertiaryContainer = Color(0xFF4ADE80),
    background = DarkBg,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurface2,
    onSurfaceVariant = DarkText3,
    surfaceTint = DarkSurface,
    inverseSurface = DarkText,
    inverseOnSurface = DarkSurface,
    error = DangerRed,
    onError = Color.White,
    errorContainer = DarkDangerBg,
    onErrorContainer = Color(0xFFE8857A),
    outline = DarkMuted,
    outlineVariant = DarkBorder2,
    surfaceBright = DarkSurface3,
    surfaceDim = DarkBg,
    surfaceContainerLowest = DarkBg,
    surfaceContainerLow = DarkSurface,    // contenedor por defecto de Card = --surface
    surfaceContainer = DarkSurface2,
    surfaceContainerHigh = DarkSurface3,
    surfaceContainerHighest = DarkSurface3,
)

@Composable
fun SmarthomeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
