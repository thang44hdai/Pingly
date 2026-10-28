package com.thangnq.pingly

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.thangnq.pingly.feature.home.ui.HomeRoute
import com.thangnq.pingly.settings.*
import com.thangnq.pingly.ui.theme.PinglyTheme

@Composable
fun App() {
    val preferences = rememberAppPreferences()
    val initial = remember(preferences) { preferences.loadSettings() }
    var theme by rememberSaveable { mutableStateOf(initial.themeMode.name) }
    var language by rememberSaveable { mutableStateOf(initial.language.name) }
    val settings = AppSettings(ThemeMode.valueOf(theme), AppLanguage.valueOf(language))
    PinglyTheme(mode = settings.themeMode) {
        HomeRoute(
            settings = settings,
            onThemeChanged = {
                theme = it.name
                preferences.write("theme_mode", it.name)
            },
            onLanguageChanged = {
                language = it.name
                preferences.write("language", it.name)
            },
        )
    }
}
