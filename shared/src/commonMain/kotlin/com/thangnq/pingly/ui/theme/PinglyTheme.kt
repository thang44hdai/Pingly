package com.thangnq.pingly.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.ColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import com.thangnq.pingly.settings.ThemeMode
import com.thangnq.pingly.settings.ApplyPlatformTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.Font
import pingly.shared.generated.resources.Res
import pingly.shared.generated.resources.plus_jakarta_sans

@Immutable
data class PinglyEffects(
    val glassFill: Color,
    val glassBorder: Color,
    val glassHighlight: Color,
    val imageShade: Color,
    val selectedNavigationContainer: Color,
    val selectedNavigationContent: Color,
)

private fun effectsFor(colors: ColorScheme, darkTheme: Boolean) = PinglyEffects(
    glassFill = colors.surfaceContainer.copy(alpha = if (darkTheme) .72f else .9f),
    glassBorder = colors.onSurface.copy(alpha = if (darkTheme) .12f else .16f),
    glassHighlight = PinglyOpticalTokens.specular,
    imageShade = PinglyOpticalTokens.shade,
    selectedNavigationContainer = colors.primaryContainer.copy(alpha = if (darkTheme) .4f else .85f),
    selectedNavigationContent = if (darkTheme) colors.primary else colors.onPrimaryContainer,
)

private val LocalPinglyEffects = staticCompositionLocalOf { effectsFor(PinglyDarkColorScheme, true) }

object PinglyTheme {
    val effects: PinglyEffects
        @Composable get() = LocalPinglyEffects.current
}

@Composable
fun PinglyTheme(mode: ThemeMode = ThemeMode.System, content: @Composable () -> Unit) {
    val darkTheme = when (mode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val colors = if (darkTheme) PinglyDarkColorScheme else PinglyLightColorScheme
    val effects = remember(colors, darkTheme) { effectsFor(colors, darkTheme) }
    ApplyPlatformTheme(darkTheme, followSystem = mode == ThemeMode.System)
    val family = FontFamily(Font(Res.font.plus_jakarta_sans))
    val base = Typography()
    CompositionLocalProvider(LocalPinglyEffects provides effects) {
        MaterialTheme(
            colorScheme = colors,
            typography = base.copy(
                displayLarge = base.displayLarge.copy(fontFamily = family),
                displayMedium = base.displayMedium.copy(fontFamily = family),
                displaySmall = base.displaySmall.copy(fontFamily = family),
                headlineLarge = base.headlineLarge.copy(fontFamily = family),
                headlineMedium = base.headlineMedium.copy(fontFamily = family),
                headlineSmall = base.headlineSmall.copy(fontFamily = family),
                titleLarge = base.titleLarge.copy(fontFamily = family),
                titleMedium = base.titleMedium.copy(fontFamily = family),
                titleSmall = base.titleSmall.copy(fontFamily = family),
                bodyLarge = base.bodyLarge.copy(fontFamily = family),
                bodyMedium = base.bodyMedium.copy(fontFamily = family),
                bodySmall = base.bodySmall.copy(fontFamily = family),
                labelLarge = base.labelLarge.copy(fontFamily = family),
                labelMedium = base.labelMedium.copy(fontFamily = family),
                labelSmall = base.labelSmall.copy(fontFamily = family),
            ),
            content = content,
        )
    }
}
