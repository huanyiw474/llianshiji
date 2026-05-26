package com.lianshiji.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2F6B4F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7F2E1),
    onPrimaryContainer = Color(0xFF0E2418),
    secondary = Color(0xFF4C6358),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD5E8DC),
    onSecondaryContainer = Color(0xFF0B1F16),
    tertiary = Color(0xFF765A2A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDDA3),
    onTertiaryContainer = Color(0xFF281800),
    background = Color(0xFFF8FAF7),
    onBackground = Color(0xFF1A1C19),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C19),
    surfaceVariant = Color(0xFFE1E5DE),
    onSurfaceVariant = Color(0xFF444941),
    outline = Color(0xFF757970)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB9D6C4),
    onPrimary = Color(0xFF123723),
    primaryContainer = Color(0xFF1F4F35),
    onPrimaryContainer = Color(0xFFD5F2DF),
    secondary = Color(0xFFBACCBF),
    onSecondary = Color(0xFF26352C),
    secondaryContainer = Color(0xFF3C4C42),
    onSecondaryContainer = Color(0xFFD6E8DB),
    tertiary = Color(0xFFE6C48D),
    onTertiary = Color(0xFF422C05),
    tertiaryContainer = Color(0xFF5D4216),
    onTertiaryContainer = Color(0xFFFFDDA3),
    background = Color(0xFF111410),
    onBackground = Color(0xFFE2E3DE),
    surface = Color(0xFF191C18),
    onSurface = Color(0xFFE2E3DE),
    surfaceVariant = Color(0xFF444941),
    onSurfaceVariant = Color(0xFFC5C9C0),
    outline = Color(0xFF8F938A)
)

@Composable
fun LianShiJiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors: ColorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography,
        content = content
    )
}
