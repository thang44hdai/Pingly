package com.thangnq.pingly.settings

import androidx.compose.runtime.*
import androidx.compose.ui.uikit.LocalUIViewController
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIUserInterfaceStyle

@Composable
internal actual fun rememberAppPreferences(): AppPreferences = remember {
    val defaults = NSUserDefaults.standardUserDefaults
    object : AppPreferences {
        override fun read(key: String) = defaults.stringForKey("pingly_$key")
        override fun write(key: String, value: String) { defaults.setObject(value, "pingly_$key") }
    }
}

@Composable
internal actual fun ProvideAppLanguage(language: AppLanguage, content: @Composable () -> Unit) {
    var appliedLanguage by remember { mutableStateOf<AppLanguage?>(null) }
    LaunchedEffect(language) {
        val defaults = NSUserDefaults.standardUserDefaults
        val tag = language.tag
        if (tag == null) defaults.removeObjectForKey("AppleLanguages")
        else defaults.setObject(listOf(tag), "AppleLanguages")
        appliedLanguage = language
    }
    // Recreate only resource consumers after the native locale has changed.
    // HomeRoute owns its state outside this provider, so tab/history/sheets are retained.
    if (appliedLanguage == language) key(language) { content() }
}

@Composable
internal actual fun ApplyPlatformTheme(darkTheme: Boolean, followSystem: Boolean) {
    val controller = LocalUIViewController.current
    SideEffect {
        controller.overrideUserInterfaceStyle = when {
            followSystem -> UIUserInterfaceStyle.UIUserInterfaceStyleUnspecified
            darkTheme -> UIUserInterfaceStyle.UIUserInterfaceStyleDark
            else -> UIUserInterfaceStyle.UIUserInterfaceStyleLight
        }
        controller.setNeedsStatusBarAppearanceUpdate()
    }
}
