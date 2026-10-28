package com.thangnq.pingly.feature.home.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thangnq.pingly.ui.theme.PinglyTheme
import com.thangnq.pingly.settings.AppSettings
import com.thangnq.pingly.settings.AppLanguage
import com.thangnq.pingly.settings.ThemeMode
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pingly.shared.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    state: HomeState,
    settings: AppSettings,
    onAction: (HomeAction) -> Unit,
    snackbar: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val effects = PinglyTheme.effects
    Surface(modifier.fillMaxSize(), color = colors.background, contentColor = colors.onBackground) {
        Box(Modifier.fillMaxSize().drawBehind {
            drawRect(Brush.radialGradient(listOf(colors.primary.copy(alpha = .09f), Color.Transparent),
                center = Offset(size.width * .1f, size.height * .15f), radius = size.width * 1.2f))
            drawRect(Brush.radialGradient(listOf(colors.secondary.copy(alpha = .07f), Color.Transparent),
                center = Offset(size.width, size.height * .55f), radius = size.width))
        }) {
            Column(Modifier.fillMaxSize().safeDrawingPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
                HomeHeader { onAction(HomeAction.ShowSheet(it)) }
                when (state.tab) {
                    HomeTab.SendOrb -> OrbContent(state, onAction, Modifier.weight(1f))
                    HomeTab.Calendar -> CalendarContent(state, onAction, Modifier.weight(1f))
                    HomeTab.Moments -> MomentsContent(state, Modifier.weight(1f))
                }
                BottomNavigation(state.tab) { onAction(HomeAction.Navigate(it)) }
            }
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 92.dp))
        }
    }
    if (state.sheet != null) {
        ModalBottomSheet(
            sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)),
            onDismissRequest = { onAction(HomeAction.ShowSheet(null)) },
            containerColor = colors.surfaceContainer, contentColor = colors.onBackground,
        ) {
            if (state.sheet == "emotions") EmotionPickerSheet(state, onAction)
            else SheetContent(state, settings, onAction)
        }
    }
}

@Composable
private fun HomeHeader(onOpen: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 20.dp)) {
        Row(
            Modifier.align(Alignment.CenterStart).widthIn(max = 140.dp).clip(RoundedCornerShape(20.dp))
                .clickable { onOpen("partner") }.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                Image(painterResource(Res.drawable.partner_portrait), null,
                    Modifier.size(36.dp).border(1.dp, colors.primary.copy(alpha = .7f), CircleShape).padding(2.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop)
                Box(Modifier.align(Alignment.BottomEnd).size(11.dp).background(colors.primary, CircleShape).border(2.dp, colors.background, CircleShape))
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Elena", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Symbol(Res.drawable.home_sync, null, Modifier.padding(start = 4.dp).size(12.dp), colors.primary)
                }
                Text(stringResource(Res.string.breathing_with_you), fontSize = 10.sp, color = colors.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Text("Pingly", Modifier.align(Alignment.Center), fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold, color = colors.primary, letterSpacing = (-1).sp)
        GlassIconButton(Res.drawable.home_settings, stringResource(Res.string.home_settings),
            Modifier.align(Alignment.CenterEnd)) { onOpen("settings") }
    }
}

@Composable
private fun OrbContent(state: HomeState, onAction: (HomeAction) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            val minHeight = maxHeight
            Column(
                Modifier.widthIn(max = 460.dp).fillMaxWidth().verticalScroll(rememberScrollState())
                    .heightIn(min = minHeight).padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PresenceCapsule { onAction(HomeAction.ShowSheet("partner")) }
                Spacer(Modifier.height(16.dp))
                PartnerScene(state.emotion, onAction)
                InsightCard { onAction(HomeAction.Send(Emotion.MissingYou)) }
            }
        }
        Box(Modifier.widthIn(max = 460.dp).fillMaxWidth().padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 4.dp)) {
            EmotionDock(state.emotion, onAction)
        }
    }
}

@Composable
private fun PresenceCapsule(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.glass(CircleShape).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Symbol(Res.drawable.home_location, null, Modifier.size(13.dp), colors.secondary)
        Text("Paris · 22°", fontSize = 11.sp, color = colors.secondary)
        Dot(colors.onSurface.copy(alpha = .2f))
        Symbol(Res.drawable.home_battery, null, Modifier.size(14.dp), colors.onSurfaceVariant)
        Text("84%", fontSize = 11.sp, color = colors.onSurfaceVariant)
        Dot(colors.onSurface.copy(alpha = .2f))
        Dot(colors.primary)
        Text(stringResource(Res.string.partner_awake), fontSize = 11.sp, color = colors.primary)
    }
}

@Composable
private fun PartnerScene(emotion: Emotion, onAction: (HomeAction) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val effects = PinglyTheme.effects
    val transition = rememberInfiniteTransition(label = "partner presence")
    val breath = transition.animateFloat(1f, 1.025f,
        infiniteRepeatable(tween(3500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "breath")
    val wave = transition.animateFloat(0f, 1f,
        infiniteRepeatable(tween(2800, easing = LinearEasing)), label = "resonance")
    val avatarLabel = stringResource(Res.string.resonate_with_partner)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.glass(CircleShape, colors.primary.copy(alpha = .2f)).padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Dot(colors.primary)
            Text(stringResource(Res.string.partner_feels, stringResource(emotion.status)),
                fontSize = 11.sp, color = colors.primary, modifier = Modifier.weight(1f, fill = false))
            Text("·", color = colors.onSurfaceVariant)
            Text(stringResource(Res.string.just_now), fontSize = 10.sp, color = colors.onSurfaceVariant)
        }
        Box(Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val radius = size.minDimension * .37f
                drawCircle(Brush.radialGradient(listOf(colors.primary.copy(alpha = .17f), colors.secondary.copy(alpha = .07f), Color.Transparent),
                    radius = radius * 1.5f), radius * 1.5f)
                repeat(2) { index ->
                    val phase = (wave.value + index * .5f) % 1f
                    drawCircle(if (index == 0) colors.primary.copy(alpha = .19f * (1f - phase)) else colors.secondary.copy(alpha = .19f * (1f - phase)),
                        radius * (1f + phase * .4f), style = Stroke(1.dp.toPx()))
                }
            }
            Box(
                Modifier.size(208.dp).graphicsLayer { scaleX = breath.value; scaleY = breath.value; translationY = -4.dp.toPx() * (breath.value - 1f) / .025f }
                    .border(2.dp, Brush.linearGradient(listOf(colors.primary, colors.secondary, colors.primary.copy(alpha = .4f))), CircleShape)
                    .padding(4.dp).clip(CircleShape)
                    .combinedClickable(role = Role.Button, hapticFeedbackEnabled = false, onClickLabel = avatarLabel,
                        onLongClickLabel = stringResource(Res.string.hold_to_resonate),
                        onClick = { onAction(HomeAction.Send(emotion)) },
                        onLongClick = { onAction(HomeAction.Send(emotion, held = true)) })
                    .semantics { contentDescription = avatarLabel },
            ) {
                Image(painterResource(Res.drawable.partner_portrait), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(effects.glassHighlight.copy(alpha = .06f), Color.Transparent, effects.imageShade.copy(alpha = .45f)))))
            }
            ThoughtPill(stringResource(Res.string.thought_missing), colors.primary,
                Modifier.align(Alignment.TopStart).padding(top = 14.dp)) { onAction(HomeAction.ShowSheet("thought_missing")) }
            ThoughtPill(stringResource(Res.string.thought_tired), colors.secondary,
                Modifier.align(Alignment.CenterEnd).offset(y = (-62).dp)) { onAction(HomeAction.ShowSheet("thought_tired")) }
            ThoughtPill(stringResource(Res.string.thought_date), colors.primary,
                Modifier.align(Alignment.BottomStart).padding(bottom = 18.dp)) { onAction(HomeAction.ShowSheet("thought_date")) }
        }
    }
}

@Composable
private fun ThoughtPill(text: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Text(text, modifier.heightIn(min = 48.dp).glass(CircleShape, color.copy(alpha = .25f)).clickable(onClick = onClick)
        .padding(horizontal = 12.dp, vertical = 13.dp), fontSize = 11.sp, color = color)
}

@Composable
private fun InsightCard(onSend: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().glass(RoundedCornerShape(20.dp), colors.primary.copy(alpha = .18f))
        .padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Symbol(Res.drawable.home_sparkles, null, Modifier.size(15.dp), colors.primary)
            Text(stringResource(Res.string.mood_insight), Modifier.weight(1f).padding(start = 5.dp),
                fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = colors.primary)
            Text(stringResource(Res.string.mood_accuracy), Modifier.background(colors.secondary.copy(alpha = .09f), CircleShape)
                .padding(horizontal = 7.dp, vertical = 4.dp), fontSize = 9.sp, color = colors.secondary)
        }
        Text(stringResource(Res.string.needs_hug), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Text(stringResource(Res.string.needs_care), fontSize = 11.sp, color = colors.onSurfaceVariant, lineHeight = 17.sp)
        HorizontalDivider(color = colors.onSurface.copy(alpha = .06f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.hug_suggestion), Modifier.weight(1f), fontSize = 10.sp, color = colors.onSurfaceVariant)
            TextButton(onClick = onSend, contentPadding = PaddingValues(horizontal = 12.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = colors.primary),
                modifier = Modifier.background(colors.primary.copy(alpha = .12f), CircleShape)) {
                Symbol(Res.drawable.home_hug, null, Modifier.size(16.dp), colors.primary)
                Text(stringResource(Res.string.send_hug), Modifier.padding(start = 5.dp), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun EmotionDock(selected: Emotion, onAction: (HomeAction) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(Res.string.partner_frequencies), fontSize = 10.sp, letterSpacing = 1.5.sp, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
        Row(Modifier.fillMaxWidth().glass(CircleShape).horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Emotion.entries.forEach { emotion ->
                val active = emotion == selected
                Box(Modifier.size(48.dp).background(if (active) colors.primary.copy(alpha = .24f) else Color.Transparent, CircleShape)
                    .then(if (active) Modifier.border(1.dp, colors.primary.copy(alpha = .4f), CircleShape) else Modifier)
                    .clip(CircleShape).selectable(active, role = Role.RadioButton) { onAction(HomeAction.SelectEmotion(emotion)) },
                    contentAlignment = Alignment.Center) {
                    Symbol(emotion.icon, stringResource(emotion.label), Modifier.size(25.dp), if (active) colors.primary else colors.onSurfaceVariant)
                    if (active) Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp).size(4.dp).background(colors.primary, CircleShape))
                }
            }
            GlassIconButton(Res.drawable.home_add, stringResource(Res.string.all_emotions)) { onAction(HomeAction.ShowSheet("emotions")) }
        }
    }
}

@Composable
private fun BottomNavigation(selected: HomeTab, onSelect: (HomeTab) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val effects = PinglyTheme.effects
    Row(Modifier.widthIn(max = 440.dp).fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
        HomeTab.entries.forEach { tab ->
            val label = stringResource(when (tab) { HomeTab.Calendar -> Res.string.home_calendar; HomeTab.SendOrb -> Res.string.home_send_orb; HomeTab.Moments -> Res.string.home_moments })
            Column(Modifier.widthIn(min = 88.dp).clip(CircleShape)
                .background(if (selected == tab) effects.selectedNavigationContainer else Color.Transparent)
                .selectable(selected == tab, role = Role.Tab) { onSelect(tab) }.padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Symbol(when (tab) { HomeTab.Calendar -> Res.drawable.home_calendar; HomeTab.SendOrb -> Res.drawable.home_orb; HomeTab.Moments -> Res.drawable.home_history },
                    null, Modifier.size(23.dp), if (selected == tab) effects.selectedNavigationContent else colors.onSurfaceVariant)
                Text(label, fontSize = 11.sp, color = if (selected == tab) effects.selectedNavigationContent else colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun MomentsContent(state: HomeState, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    LazyColumn(
        modifier.widthIn(max = 460.dp).fillMaxWidth(),
        contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "heading") {
            Text(stringResource(Res.string.home_moments), style = MaterialTheme.typography.headlineMedium, color = colors.primary)
        }
        item(key = "description") {
            Text(stringResource(Res.string.moments_description), color = colors.onSurfaceVariant, fontSize = 13.sp)
        }
        if (state.sentEmotions.isEmpty()) item(key = "empty") {
            Column(Modifier.fillMaxWidth().padding(vertical = 64.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Symbol(Res.drawable.home_history, null, Modifier.size(48.dp), colors.primary.copy(alpha = .6f))
                Text(stringResource(Res.string.no_moments), color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
        items(state.sentEmotions.size, key = { state.sentEmotions.size - it }, contentType = { "moment" }) { index ->
            val value = state.sentEmotions[state.sentEmotions.lastIndex - index]
            val effect = if (value.startsWith("effect:")) OrbEffect.valueOf(value.substringAfter(':')) else null
            val emotion = if (effect == null) Emotion.valueOf(value) else null
            val icon = effect?.icon ?: requireNotNull(emotion).icon
            val label = effect?.label ?: requireNotNull(emotion).label
            Row(Modifier.fillMaxWidth().glass(RoundedCornerShape(20.dp)).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Symbol(icon, null, Modifier.size(28.dp), colors.primary)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(Res.string.moment_sent, stringResource(label)), fontSize = 14.sp)
                    Text(stringResource(Res.string.local_moment), color = colors.onSurfaceVariant, fontSize = 11.sp)
                }
                Text("#${state.sentEmotions.size - index}", color = colors.secondary, fontSize = 12.sp)
            }
        }
    }
}

private fun calendarMonth(offset: Int): Triple<Int, Int, Int> {
    val absolute = 2026 * 12 + 9 + offset
    val year = absolute.floorDiv(12)
    val month = absolute.mod(12) + 1
    val days = when (month) { 4, 6, 9, 11 -> 30; 2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28; else -> 31 }
    return Triple(year, month, days)
}

private fun weekdayOffset(year: Int, month: Int): Int {
    val y = if (month < 3) year - 1 else year
    val table = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)
    return (y + y / 4 - y / 100 + y / 400 + table[month - 1] + 1 + 6) % 7
}

@Composable
private fun CalendarContent(state: HomeState, onAction: (HomeAction) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val (year, month, days) = calendarMonth(state.monthOffset)
    val offset = weekdayOffset(year, month)
    Column(modifier.widthIn(max = 460.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(stringResource(Res.string.home_calendar), style = MaterialTheme.typography.headlineMedium, color = colors.primary)
        Text(stringResource(Res.string.calendar_description), color = colors.onSurfaceVariant, fontSize = 13.sp)
        Column(Modifier.glass(RoundedCornerShape(24.dp)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onAction(HomeAction.ChangeMonth(-1)) }) { Symbol(Res.drawable.home_back, stringResource(Res.string.previous_month)) }
                Text("$month / $year", Modifier.weight(1f), textAlign = TextAlign.Center, color = colors.primary, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = { onAction(HomeAction.ChangeMonth(1)) }) { Symbol(Res.drawable.home_next, stringResource(Res.string.next_month)) }
            }
            Row { stringResource(Res.string.weekdays).split(",").forEach { Text(it, Modifier.weight(1f).padding(vertical = 12.dp), color = colors.onSurfaceVariant, fontSize = 10.sp, textAlign = TextAlign.Center) } }
            repeat((offset + days + 6) / 7) { week ->
                Row {
                    repeat(7) { dayOfWeek ->
                        val day = week * 7 + dayOfWeek - offset + 1
                        Box(Modifier.weight(1f).height(48.dp), contentAlignment = Alignment.Center) {
                            if (day in 1..days) {
                                Box(Modifier.fillMaxSize().padding(1.dp).clip(CircleShape)
                                    .background(if (state.selectedDay == day) colors.primary.copy(alpha = .24f) else Color.Transparent)
                                    .selectable(state.selectedDay == day) { onAction(HomeAction.SelectDay(day)) }, contentAlignment = Alignment.Center) {
                                    Text("$day", color = if (state.selectedDay == day) colors.primary else colors.onSurfaceVariant, fontSize = 13.sp)
                                    if (state.calendarNotes.any { it.startsWith("${state.monthOffset}|$day|") })
                                        Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 5.dp).size(4.dp).background(colors.secondary, CircleShape))
                                }
                            }
                        }
                    }
                }
            }
        }
        Text("${state.selectedDay} / $month / $year", color = colors.primary, fontWeight = FontWeight.SemiBold)
        val notes = state.calendarNotes.filter { it.startsWith("${state.monthOffset}|${state.selectedDay}|") }
        if (notes.isEmpty()) Text(stringResource(Res.string.no_calendar_notes), color = colors.onSurfaceVariant, fontSize = 13.sp)
        notes.forEach { Text(it.substringAfter('|').substringAfter('|'), Modifier.fillMaxWidth().glass(RoundedCornerShape(16.dp)).padding(16.dp), fontSize = 14.sp) }
        OutlinedButton(onClick = { onAction(HomeAction.ShowSheet("add_note")) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.add_calendar_note), color = colors.primary)
        }
    }
}

@Composable
private fun SheetContent(state: HomeState, settings: AppSettings, onAction: (HomeAction) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().then(if (state.sheet == "settings") Modifier.fillMaxHeight(.9f) else Modifier)
        .imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)) {
        when (state.sheet) {
            "settings" -> {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    SheetTitle(stringResource(Res.string.home_settings))
                    TextButton(onClick = { onAction(HomeAction.ShowSheet(null)) }) {
                        Text(stringResource(Res.string.close_settings))
                    }
                }
                Text(stringResource(Res.string.appearance), style = MaterialTheme.typography.titleMedium)
                Column(Modifier.selectableGroup()) {
                    ThemeMode.entries.forEach { mode ->
                        SettingsChoice(
                            title = stringResource(when (mode) {
                                ThemeMode.System -> Res.string.follow_system
                                ThemeMode.Light -> Res.string.theme_light
                                ThemeMode.Dark -> Res.string.theme_dark
                            }),
                            selected = settings.themeMode == mode,
                        ) { onAction(HomeAction.ChangeTheme(mode)) }
                    }
                }
                HorizontalDivider(color = colors.outlineVariant)
                Text(stringResource(Res.string.app_language), style = MaterialTheme.typography.titleMedium)
                Column(Modifier.selectableGroup()) {
                    AppLanguage.entries.forEach { language ->
                        SettingsChoice(
                            title = when (language) {
                                AppLanguage.System -> stringResource(Res.string.follow_system)
                                AppLanguage.English -> "English"
                                AppLanguage.Vietnamese -> "Tiếng Việt"
                            },
                            selected = settings.language == language,
                        ) { onAction(HomeAction.ChangeLanguage(language)) }
                    }
                }
                HorizontalDivider(color = colors.outlineVariant)
                SettingSwitch(stringResource(Res.string.haptic_feedback), stringResource(Res.string.haptic_description), state.haptics) { onAction(HomeAction.ToggleHaptics) }
                SettingSwitch(stringResource(Res.string.overlay_preview), stringResource(Res.string.overlay_description), state.overlay) { onAction(HomeAction.ToggleOverlay) }
                if (state.overlay) Row(Modifier.fillMaxWidth().glass(RoundedCornerShape(20.dp), colors.primary.copy(alpha = .3f)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Symbol(Res.drawable.home_hug, null, Modifier.size(32.dp), colors.primary)
                    Text(stringResource(Res.string.overlay_sample), Modifier.padding(start = 12.dp), fontSize = 13.sp)
                }
            }
            "partner" -> {
                Image(painterResource(Res.drawable.partner_portrait), null, Modifier.align(Alignment.CenterHorizontally).size(112.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                SheetTitle("Elena")
                Text(stringResource(Res.string.partner_details), color = colors.onSurfaceVariant, lineHeight = 24.sp)
                Text(stringResource(Res.string.partner_feels, stringResource(state.emotion.status)), color = colors.primary)
                Button(onClick = { onAction(HomeAction.Send(state.emotion)) }, Modifier.fillMaxWidth()) { Text(stringResource(Res.string.send_resonance)) }
            }
            "add_note" -> {
                var note by rememberSaveable { mutableStateOf("") }
                SheetTitle(stringResource(Res.string.add_calendar_note))
                OutlinedTextField(note, { note = it.take(160) }, Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.calendar_note_hint)) }, minLines = 2, maxLines = 4)
                Button(onClick = { onAction(HomeAction.AddNote(note)) }, enabled = note.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text(stringResource(Res.string.save_note)) }
            }
            else -> {
                val title = when (state.sheet) { "thought_tired" -> Res.string.thought_tired; "thought_date" -> Res.string.thought_date; else -> Res.string.thought_missing }
                SheetTitle(stringResource(title))
                Text(stringResource(Res.string.thought_detail), color = colors.onSurfaceVariant, lineHeight = 24.sp)
                Button(onClick = { onAction(HomeAction.Send(Emotion.MissingYou)) }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(Res.string.send_hug)) }
            }
        }
    }
}

@Composable
private fun SettingsChoice(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp).clip(RoundedCornerShape(16.dp))
            .selectable(selected, role = Role.RadioButton, onClick = onClick).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected, onClick = null)
        Text(title, Modifier.padding(start = 12.dp), fontSize = 14.sp)
    }
}

@Composable
private fun SettingSwitch(title: String, description: String, checked: Boolean, onToggle: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontSize = 15.sp)
            Text(description, fontSize = 12.sp, color = colors.onSurfaceVariant)
        }
        Switch(checked, onCheckedChange = { onToggle() }, modifier = Modifier.semantics { contentDescription = title })
    }
}

@Composable private fun SheetTitle(text: String) { Text(text, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary) }

@Composable
private fun Modifier.glass(shape: androidx.compose.ui.graphics.Shape, border: Color = PinglyTheme.effects.glassBorder): Modifier =
    clip(shape).background(PinglyTheme.effects.glassFill).border(1.dp, border, shape)

@Composable private fun Dot(color: Color) { Box(Modifier.size(4.dp).background(color, CircleShape)) }

@Composable
private fun Symbol(resource: DrawableResource, description: String?, modifier: Modifier = Modifier.size(24.dp), tint: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Icon(painterResource(resource), description, modifier, tint)
}

@Composable
private fun GlassIconButton(resource: DrawableResource, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    IconButton(onClick = onClick, modifier = modifier.size(48.dp).glass(CircleShape)) { Symbol(resource, label, Modifier.size(22.dp), colors.primary) }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    PinglyTheme { HomeScreen(HomeState(), AppSettings(), {}, remember { SnackbarHostState() }) }
}
