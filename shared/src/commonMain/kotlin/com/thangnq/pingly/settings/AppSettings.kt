package com.thangnq.pingly.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable

enum class ThemeMode { System, Light, Dark }
enum class AppLanguage(val tag: String?) { System(null), English("en"), Vietnamese("vi") }

@Immutable
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    val language: AppLanguage = AppLanguage.System,
)

internal interface AppPreferences {
    fun read(key: String): String?
    fun write(key: String, value: String)
}

@Composable
internal expect fun rememberAppPreferences(): AppPreferences

@Composable
internal expect fun ProvideAppLanguage(language: AppLanguage, content: @Composable () -> Unit)

@Composable
internal expect fun ApplyPlatformTheme(darkTheme: Boolean, followSystem: Boolean)

internal fun AppPreferences.loadSettings() = AppSettings(
    themeMode = ThemeMode.entries.firstOrNull { it.name == read("theme_mode") } ?: ThemeMode.System,
    language = AppLanguage.entries.firstOrNull { it.name == read("language") } ?: AppLanguage.System,
)
