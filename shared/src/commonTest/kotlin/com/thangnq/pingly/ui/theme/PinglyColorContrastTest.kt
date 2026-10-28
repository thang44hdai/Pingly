package com.thangnq.pingly.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.test.Test
import kotlin.test.assertTrue

class PinglyColorContrastTest {
    @Test
    fun semanticTextRolesRemainReadableInBothThemes() {
        for ((name, colors) in listOf("light" to PinglyLightColorScheme, "dark" to PinglyDarkColorScheme)) {
            val roles = mapOf(
                "background" to (colors.onBackground to colors.background),
                "surface" to (colors.onSurface to colors.surface),
                "secondary text" to (colors.onSurfaceVariant to colors.surfaceContainer),
                "primary accent" to (colors.primary to colors.background),
                "secondary accent" to (colors.secondary to colors.background),
                "primary button" to (colors.onPrimary to colors.primary),
                "primary container" to (colors.onPrimaryContainer to colors.primaryContainer),
                "secondary button" to (colors.onSecondary to colors.secondary),
            )
            for ((role, pair) in roles) {
                val ratio = contrast(pair.first, pair.second)
                assertTrue(ratio >= 4.5f, "$name $role text contrast is $ratio; expected at least 4.5:1")
            }
        }
    }

    private fun contrast(foreground: Color, background: Color): Float {
        val a = foreground.luminance()
        val b = background.luminance()
        return (maxOf(a, b) + .05f) / (minOf(a, b) + .05f)
    }
}
