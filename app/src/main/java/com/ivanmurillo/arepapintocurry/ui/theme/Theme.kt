package com.ivanmurillo.arepapintocurry.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrandRed,
    onPrimary = Color.White,
    secondary = Saffron,
    onSecondary = Color.White,
    secondaryContainer = SaffronLight,
    onSecondaryContainer = Color(0xFF2E1500),
    tertiary = FlagBlue,
    onTertiary = Color.White,
    background = Cream,
    surface = Cream,
    surfaceVariant = Color(0xFFF3E2D3),
    onSurfaceVariant = Color(0xFF55443A),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFFF1E6),
    surfaceContainer = Color(0xFFFCEBDD),
    surfaceContainerHigh = Color(0xFFF8E6D7),
    surfaceContainerHighest = Color(0xFFF5E1D0),
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandRedLight,
    secondary = SaffronLight,
    secondaryContainer = Color(0xFF6B3A00),
    onSecondaryContainer = SaffronLight,
    tertiary = FlagBlueLight,
)

// Sin colores dinámicos: la app siempre muestra los colores de la marca
@Composable
fun ArepaPintoCurryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
