package com.thangnq.pingly.feature.home.ui

import com.thangnq.pingly.settings.AppLanguage
import com.thangnq.pingly.settings.ThemeMode
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import pingly.shared.generated.resources.*

internal enum class Emotion(val icon: DrawableResource, val label: StringResource, val status: StringResource) {
    Loved(Res.drawable.home_heart, Res.string.emotion_loved, Res.string.status_loved),
    Joyful(Res.drawable.home_smile, Res.string.emotion_joyful, Res.string.status_joyful),
    MissingYou(Res.drawable.home_hug, Res.string.emotion_missing, Res.string.status_missing),
    Calm(Res.drawable.home_calm, Res.string.emotion_calm, Res.string.status_calm),
    Excited(Res.drawable.home_sparkles, Res.string.emotion_excited, Res.string.status_excited),
}

internal enum class HomeTab { Calendar, SendOrb, Moments }

internal data class HomeState(
    val emotion: Emotion = Emotion.Loved,
    val tab: HomeTab = HomeTab.SendOrb,
    val sheet: String? = null,
    val sentEmotions: List<String> = emptyList(),
    val haptics: Boolean = true,
    val overlay: Boolean = true,
    val monthOffset: Int = 0,
    val selectedDay: Int = 28,
    val calendarNotes: List<String> = emptyList(),
    val orbEffect: OrbEffect = OrbEffect.Heart,
    val overlayDuration: Float = 2.5f,
    val effectSendCount: Int = 0,
)

internal sealed interface HomeAction {
    data class PreviewEffect(val effect: OrbEffect) : HomeAction
    data class SetOverlayDuration(val seconds: Float) : HomeAction
    data object SendEffect : HomeAction
    data class ChangeTheme(val mode: ThemeMode) : HomeAction
    data class ChangeLanguage(val language: AppLanguage) : HomeAction
    data class SelectEmotion(val emotion: Emotion) : HomeAction
    data class Send(val emotion: Emotion, val held: Boolean = false) : HomeAction
    data class Navigate(val tab: HomeTab) : HomeAction
    data class ShowSheet(val name: String?) : HomeAction
    data object ToggleHaptics : HomeAction
    data object ToggleOverlay : HomeAction
    data class ChangeMonth(val delta: Int) : HomeAction
    data class SelectDay(val day: Int) : HomeAction
    data class AddNote(val text: String) : HomeAction
}
