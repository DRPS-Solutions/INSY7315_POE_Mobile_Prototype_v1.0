package com.qulloobul.moemienen.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    tertiary = LightInfo,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color_White,
    onBackground = Color_Navy,
    onSurface = Color_Navy
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    tertiary = DarkInfo,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color_Navy,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary
)

private val AppTypography = Typography(
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
)

/**
 * @param useDarkTheme mirrors MainLayout.razor's isDarkMode toggle - passed
 * in explicitly (rather than always following the system) so the in-app
 * dark-mode switch in the top bar behaves the same as the original.
 */
@Composable
fun QulloobulMoemienenTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
