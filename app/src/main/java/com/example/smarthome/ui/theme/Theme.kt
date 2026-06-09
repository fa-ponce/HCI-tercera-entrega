package com.example.smarthome.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001B3E),
    secondary = BlueGrey40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD3E4F5),
    onSecondaryContainer = Color(0xFF0D2535),
    tertiary = BlueAccent40,
    onTertiary = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = Color(0xFF00315E),
    primaryContainer = Color(0xFF234880),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = BlueGrey80,
    onSecondary = Color(0xFF1D3445),
    secondaryContainer = Color(0xFF344B5C),
    onSecondaryContainer = Color(0xFFD3E4F5),
    tertiary = BlueAccent80,
    onTertiary = Color(0xFF003A6B),
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
