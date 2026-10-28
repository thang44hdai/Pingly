package com.thangnq.pingly.feature.home.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thangnq.pingly.ui.theme.PinglyTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pingly.shared.generated.resources.*
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
internal fun EmotionPickerSheet(state: HomeState, onAction: (HomeAction) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val overlayLabel = stringResource(Res.string.screen_overlay)
    val initialSendCount = remember { state.effectSendCount }
    var showSent by remember { mutableStateOf(false) }
    LaunchedEffect(state.effectSendCount) {
        if (state.effectSendCount > initialSendCount) {
            showSent = true
            delay(1800)
            showSent = false
        }
    }
    Column(modifier.fillMaxWidth().fillMaxHeight(.92f)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(Res.string.choose_emotion), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Box(Modifier.size(7.dp).background(colors.primary, CircleShape))
                }
                Text(stringResource(Res.string.picker_subtitle), color = colors.onSurfaceVariant, fontSize = 11.sp, lineHeight = 16.sp)
            }
            Column(Modifier.clip(RoundedCornerShape(16.dp)).toggleable(state.overlay, role = Role.Switch,
                onValueChange = { onAction(HomeAction.ToggleOverlay) }).padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(overlayLabel, fontSize = 11.sp, color = colors.onSurfaceVariant)
                Switch(state.overlay, onCheckedChange = null)
            }
        }
        HorizontalDivider(color = colors.outlineVariant.copy(alpha = .4f))
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            EffectGrid(state.orbEffect) { onAction(HomeAction.PreviewEffect(it)) }
            PhonePreview(state) { onAction(HomeAction.SetOverlayDuration(it)) }
            Row(Modifier.fillMaxWidth().clip(CircleShape).background(colors.surfaceContainerLow)
                .border(1.dp, PinglyTheme.effects.glassBorder, CircleShape).padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PickerIcon(Res.drawable.picker_active, null, Modifier.size(18.dp), colors.secondary)
                Text(stringResource(if (state.overlay) Res.string.overlay_preview_on else Res.string.overlay_preview_off),
                    fontSize = 11.sp, color = colors.onSurface, modifier = Modifier.weight(1f))
                Text(stringResource(Res.string.preview_only), fontSize = 10.sp, color = colors.onSurfaceVariant)
            }
        }
        HorizontalDivider(color = colors.outlineVariant.copy(alpha = .4f))
        Box(Modifier.fillMaxWidth().background(colors.surfaceContainer).padding(horizontal = 20.dp, vertical = 12.dp)) {
            Button(
                onClick = { onAction(HomeAction.SendEffect) }, enabled = !showSent,
                modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp).clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(colors.primary, lerp(colors.primary, colors.primaryContainer, .2f), colors.primary))),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = colors.onPrimary,
                    disabledContainerColor = colors.primary, disabledContentColor = colors.onPrimary),
            ) {
                PickerIcon(if (showSent) Res.drawable.home_check else Res.drawable.picker_send, null, tint = colors.onPrimary)
                Text(stringResource(if (showSent) Res.string.picker_sent else Res.string.send_to_elena),
                    Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Box(Modifier.size(6.dp).background(colors.onPrimary, CircleShape))
            }
        }
    }
}

@Composable
private fun EffectGrid(selected: OrbEffect, onSelect: (OrbEffect) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.touch_palettes), Modifier.weight(1f), fontSize = 11.sp,
                color = colors.onSurfaceVariant, letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold)
            Text(stringResource(Res.string.tap_swap_effect), fontSize = 10.sp, color = colors.primary)
        }
        OrbEffect.entries.chunked(4).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { effect ->
                    val active = effect == selected
                    val accent = effect.accent(colors)
                    val label = stringResource(effect.label)
                    Box(Modifier.weight(1f).heightIn(min = 90.dp).clip(RoundedCornerShape(28.dp))
                        .background(if (active) accent.copy(alpha = .15f) else colors.surfaceContainerHigh.copy(alpha = .6f))
                        .border(1.dp, if (active) accent.copy(alpha = .6f) else PinglyTheme.effects.glassBorder, RoundedCornerShape(28.dp))
                        .selectable(active, role = Role.RadioButton, onClick = { onSelect(effect) })
                        .padding(horizontal = 3.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(Modifier.size(44.dp).background(Brush.linearGradient(listOf(accent.copy(alpha = if (active) .9f else .3f),
                                accent.copy(alpha = if (active) .65f else .14f))), CircleShape), contentAlignment = Alignment.Center) {
                                PickerIcon(effect.icon, null, tint = if (active) colors.onPrimary else accent)
                            }
                            Text(label, fontSize = 10.sp, lineHeight = 13.sp, textAlign = TextAlign.Center,
                                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (active) accent else colors.onSurfaceVariant)
                        }
                        if (active) Box(Modifier.align(Alignment.TopEnd).size(6.dp).background(accent, CircleShape))
                    }
                }
            }
        }
    }
}

@Composable
private fun PhonePreview(state: HomeState, onDurationChanged: (Float) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val accent = state.orbEffect.accent(colors)
    val shape = RoundedCornerShape(28.dp)
    val seconds = (state.overlayDuration * 10).roundToInt()
    Column(Modifier.fillMaxWidth().clip(shape).background(colors.surfaceContainerLow.copy(alpha = .75f))
        .border(1.dp, PinglyTheme.effects.glassBorder, shape).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PickerIcon(Res.drawable.picker_phone, null, Modifier.size(18.dp), colors.primary)
            Text(stringResource(Res.string.elena_phone_preview), Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(stringResource(state.orbEffect.badge), Modifier.clip(CircleShape).background(accent.copy(alpha = .15f))
                .padding(horizontal = 8.dp, vertical = 5.dp), color = accent, fontSize = 10.sp)
        }
        Box(Modifier.align(Alignment.CenterHorizontally).widthIn(max = 270.dp).fillMaxWidth().height(180.dp)
            .clip(RoundedCornerShape(28.dp)).background(colors.surfaceContainerLowest)
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(28.dp)).padding(10.dp)) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("18:42", Modifier.weight(1f), fontSize = 10.sp, color = colors.onSurfaceVariant)
                    PickerIcon(Res.drawable.picker_wifi, null, Modifier.size(12.dp), colors.onSurfaceVariant)
                    PickerIcon(Res.drawable.home_battery, null, Modifier.padding(start = 6.dp).size(12.dp), colors.onSurfaceVariant)
                }
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.surfaceVariant.copy(alpha = .4f))
                    .padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(16.dp).background(colors.onSurface.copy(alpha = .15f), CircleShape))
                        SkeletonLine(.6f, .2f)
                    }
                    SkeletonLine(1f, .1f)
                    SkeletonLine(.75f, .1f)
                    SkeletonLine(.5f, .1f)
                }
                Box(Modifier.align(Alignment.CenterHorizontally).width(64.dp).height(3.dp).background(colors.onSurface.copy(alpha = .3f), CircleShape))
            }
            if (state.overlay) {
                AnimatedOverlay(state.orbEffect, state.overlayDuration, Modifier.matchParentSize())
                Text(stringResource(Res.string.pingly_from_you), Modifier.align(Alignment.TopCenter).padding(top = 22.dp)
                    .clip(CircleShape).background(colors.surfaceContainerHigh.copy(alpha = .94f))
                    .border(1.dp, PinglyTheme.effects.glassBorder, CircleShape).padding(horizontal = 10.dp, vertical = 5.dp),
                    fontSize = 9.sp, color = colors.onSurface)
            }
        }
        Text(stringResource(state.orbEffect.description), fontSize = 10.sp, lineHeight = 14.sp, color = colors.onSurfaceVariant)
        HorizontalDivider(color = colors.outlineVariant.copy(alpha = .35f))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            PickerIcon(Res.drawable.picker_timer, null, Modifier.size(15.dp), colors.onSurfaceVariant)
            Text(stringResource(Res.string.overlay_duration), Modifier.weight(1f), fontSize = 11.sp, color = colors.onSurfaceVariant)
            Text(stringResource(Res.string.duration_seconds, seconds / 10, seconds % 10), fontSize = 12.sp,
                color = colors.primary, fontWeight = FontWeight.SemiBold)
        }
        val durationLabel = stringResource(Res.string.overlay_duration)
        Slider(value = state.overlayDuration, onValueChange = onDurationChanged,
            valueRange = 1f..5f, steps = 7,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = durationLabel },
            thumb = {
                Box(Modifier.size(20.dp).background(colors.primary, CircleShape)
                    .border(3.dp, colors.primaryContainer, CircleShape))
            },
            track = { slider ->
                Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(colors.surfaceVariant)) {
                    Box(Modifier.fillMaxWidth((slider.value - 1f) / 4f).fillMaxHeight().background(colors.primary))
                }
            },
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(Res.string.duration_quick), fontSize = 8.sp, color = colors.onSurfaceVariant)
            Text(stringResource(Res.string.duration_standard), fontSize = 8.sp, color = colors.onSurfaceVariant)
            Text(stringResource(Res.string.duration_lingering), fontSize = 8.sp, color = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun SkeletonLine(fraction: Float, opacity: Float) {
    Box(Modifier.fillMaxWidth(fraction).height(5.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = opacity), CircleShape))
}

@Composable
private fun AnimatedOverlay(effect: OrbEffect, duration: Float, modifier: Modifier = Modifier) {
    val accent = effect.accent(MaterialTheme.colorScheme)
    val transition = rememberInfiniteTransition(label = "emotion overlay")
    val progress = transition.animateFloat(0f, 1f,
        infiniteRepeatable(tween((duration * 1000).roundToInt(), easing = LinearEasing)), label = "particle drift")
    Box(modifier) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(Brush.radialGradient(listOf(accent.copy(alpha = .22f), Color.Transparent), radius = size.minDimension * .55f),
                radius = size.minDimension * .55f)
        }
        repeat(3) { index ->
            PickerIcon(effect.icon, null,
                Modifier.align(Alignment.BottomCenter).offset(x = (-26 + index * 34).dp, y = (-8).dp)
                    .size((36 - index * 8).dp).graphicsLayer {
                        val phase = (progress.value + index * .28f) % 1f
                        translationY = -100.dp.toPx() * phase
                        translationX = sin(phase * PI * 2).toFloat() * 6.dp.toPx()
                        alpha = sin(phase * PI).toFloat().coerceIn(0f, 1f)
                        scaleX = .8f + phase * .35f
                        scaleY = scaleX
                        if (effect == OrbEffect.Confetti || effect == OrbEffect.Star) rotationZ = phase * 45f
                    }, accent)
        }
    }
}

@Composable
private fun PickerIcon(resource: DrawableResource, label: String?, modifier: Modifier = Modifier.size(24.dp), tint: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Icon(painterResource(resource), label, modifier, tint)
}
