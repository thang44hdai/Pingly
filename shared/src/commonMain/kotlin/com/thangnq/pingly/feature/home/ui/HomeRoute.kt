package com.thangnq.pingly.feature.home.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.launch
import com.thangnq.pingly.settings.*
import org.jetbrains.compose.resources.stringResource
import pingly.shared.generated.resources.*

// Only primitive values and lists of strings are persisted by the shared saveable state registry.
private val HomeStateSaver = listSaver<HomeState, Any>(
    save = {
        listOf(it.emotion.name, it.tab.name, it.sheet.orEmpty(), ArrayList(it.sentEmotions),
            it.haptics, it.overlay, it.monthOffset, it.selectedDay, ArrayList(it.calendarNotes),
            it.orbEffect.name, it.overlayDuration, it.effectSendCount)
    },
    restore = {
        HomeState(
            emotion = Emotion.valueOf(it[0] as String), tab = HomeTab.valueOf(it[1] as String),
            sheet = (it[2] as String).ifEmpty { null },
            sentEmotions = (it[3] as List<*>).filterIsInstance<String>(),
            haptics = it[4] as Boolean, overlay = it[5] as Boolean,
            monthOffset = it[6] as Int, selectedDay = it[7] as Int,
            calendarNotes = (it[8] as List<*>).filterIsInstance<String>(),
            orbEffect = (it.getOrNull(9) as? String)?.let(OrbEffect::valueOf) ?: OrbEffect.Heart,
            overlayDuration = it.getOrNull(10) as? Float ?: 2.5f,
            effectSendCount = it.getOrNull(11) as? Int ?: 0,
        )
    },
)

@Composable
fun HomeRoute(
    settings: AppSettings,
    onThemeChanged: (ThemeMode) -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit,
) {
    var state by rememberSaveable(stateSaver = HomeStateSaver) { mutableStateOf(HomeState()) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    ProvideAppLanguage(settings.language) {
        val sentMessages = Emotion.entries.associateWith { stringResource(Res.string.orb_sent, stringResource(it.label)) }
        HomeScreen(state, settings, { action ->
            when (action) {
                is HomeAction.PreviewEffect -> state = state.copy(orbEffect = action.effect)
                is HomeAction.SetOverlayDuration -> state = state.copy(overlayDuration = action.seconds.coerceIn(1f, 5f))
                HomeAction.SendEffect -> {
                    if (state.haptics) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    state = state.copy(
                        sentEmotions = state.sentEmotions + "effect:${state.orbEffect.name}",
                        effectSendCount = state.effectSendCount + 1,
                    )
                }
                is HomeAction.ChangeTheme -> onThemeChanged(action.mode)
                is HomeAction.ChangeLanguage -> {
                    snackbar.currentSnackbarData?.dismiss()
                    onLanguageChanged(action.language)
                }
                is HomeAction.SelectEmotion -> state = state.copy(emotion = action.emotion, sheet = null)
                is HomeAction.Navigate -> state = state.copy(tab = action.tab, sheet = null)
                is HomeAction.ShowSheet -> state = state.copy(sheet = action.name)
                HomeAction.ToggleHaptics -> state = state.copy(haptics = !state.haptics)
                HomeAction.ToggleOverlay -> state = state.copy(overlay = !state.overlay)
                is HomeAction.ChangeMonth -> state = state.copy(monthOffset = state.monthOffset + action.delta, selectedDay = 1)
                is HomeAction.SelectDay -> state = state.copy(selectedDay = action.day)
                is HomeAction.AddNote -> {
                    if (action.text.isNotBlank()) {
                        val note = "${state.monthOffset}|${state.selectedDay}|${action.text.trim()}"
                        state = state.copy(calendarNotes = state.calendarNotes + note, sheet = null)
                    }
                }
                is HomeAction.Send -> {
                    if (state.haptics) haptics.performHapticFeedback(
                        if (action.held) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove,
                    )
                    state = state.copy(sentEmotions = state.sentEmotions + action.emotion.name, sheet = null)
                    scope.launch {
                        snackbar.currentSnackbarData?.dismiss()
                        snackbar.showSnackbar(sentMessages.getValue(action.emotion))
                    }
                }
            }
        }, snackbar)
    }
}
