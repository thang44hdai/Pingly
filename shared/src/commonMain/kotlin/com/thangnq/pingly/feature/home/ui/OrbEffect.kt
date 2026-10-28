package com.thangnq.pingly.feature.home.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import pingly.shared.generated.resources.*

internal enum class OrbEffect(
    val icon: DrawableResource,
    val label: StringResource,
    val badge: StringResource,
    val description: StringResource,
) {
    Heart(Res.drawable.home_heart, Res.string.effect_heart, Res.string.badge_heart, Res.string.effect_heart_description),
    Kiss(Res.drawable.picker_kiss, Res.string.effect_kiss, Res.string.badge_kiss, Res.string.effect_kiss_description),
    Hug(Res.drawable.picker_hug, Res.string.effect_hug, Res.string.badge_hug, Res.string.effect_hug_description),
    Star(Res.drawable.home_sparkles, Res.string.effect_star, Res.string.badge_star, Res.string.effect_star_description),
    Flower(Res.drawable.picker_flower, Res.string.effect_flower, Res.string.badge_flower, Res.string.effect_flower_description),
    Confetti(Res.drawable.picker_confetti, Res.string.effect_confetti, Res.string.badge_confetti, Res.string.effect_confetti_description),
    Rainbow(Res.drawable.picker_rainbow, Res.string.effect_rainbow, Res.string.badge_rainbow, Res.string.effect_rainbow_description),
    Cat(Res.drawable.picker_cat, Res.string.effect_cat, Res.string.badge_cat, Res.string.effect_cat_description),
    ;

    fun accent(colors: ColorScheme): Color = when (this) {
        Heart, Kiss, Flower, Cat -> colors.primary
        Hug, Confetti -> colors.tertiary
        Star, Rainbow -> colors.secondary
    }
}
