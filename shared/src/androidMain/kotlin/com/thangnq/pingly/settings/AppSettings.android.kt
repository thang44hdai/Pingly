package com.thangnq.pingly.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.runtime.*
import java.util.Locale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import android.os.Build
import android.view.View
import android.view.WindowInsetsController

@Composable
internal actual fun rememberAppPreferences(): AppPreferences {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        val preferences = context.getSharedPreferences("pingly_settings", Context.MODE_PRIVATE)
        object : AppPreferences {
            override fun read(key: String) = preferences.getString(key, null)
            override fun write(key: String, value: String) { preferences.edit().putString(key, value).apply() }
        }
    }
}

@Composable
internal actual fun ProvideAppLanguage(language: AppLanguage, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val systemConfiguration = LocalConfiguration.current
    val locale = remember(systemConfiguration, language) {
        language.tag?.let(Locale::forLanguageTag) ?: systemConfiguration.locales[0]
    }
    val configuration = remember(systemConfiguration, locale) {
        Configuration(systemConfiguration).apply { setLocales(LocaleList(locale)) }
    }
    val localizedContext = remember(context, configuration) { context.createConfigurationContext(configuration) }
    var appliedTag by remember { mutableStateOf<String?>(null) }
    val tag = locale.toLanguageTag()
    LaunchedEffect(tag) {
        // Compose resources 1.12.1 reads Locale.current (the JVM default), while
        // Android native resources read Configuration. Update both before rendering.
        Locale.setDefault(locale)
        appliedTag = tag
    }
    if (appliedTag == tag) key(tag) {
        CompositionLocalProvider(LocalContext provides localizedContext, LocalConfiguration provides configuration, content = content)
    }
}

@Composable
internal actual fun ApplyPlatformTheme(darkTheme: Boolean, followSystem: Boolean) {
    val context = LocalContext.current
    SideEffect {
        var host = context
        while (host is ContextWrapper && host !is Activity) host = host.baseContext
        (host as? Activity)?.window?.let { window ->
            if (Build.VERSION.SDK_INT >= 30) {
                val mask = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                window.insetsController?.setSystemBarsAppearance(if (darkTheme) 0 else mask, mask)
            } else {
                @Suppress("DEPRECATION")
                val mask = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = if (darkTheme)
                    window.decorView.systemUiVisibility and mask.inv()
                else window.decorView.systemUiVisibility or mask
            }
        }
    }
}
