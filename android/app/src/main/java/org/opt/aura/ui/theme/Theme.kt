package org.opt.aura.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AuraRose,
    secondary = AuraLavender,
    tertiary = AuraTeal,
    background = AuraBg,
    surface = AuraSurface,
    onPrimary = AuraBg,
    onSecondary = AuraBg,
    onBackground = AuraText,
    onSurface = AuraText
)

@Composable
fun AuraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}
