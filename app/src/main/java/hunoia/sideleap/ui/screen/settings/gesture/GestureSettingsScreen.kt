package hunoia.sideleap.ui.screen.settings.gesture

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.component.UDFComponent
import com.aaron.compose.ktx.onSingleClick
import hunoia.sideleap.R
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxLongPressTriggerDelayMs
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxLongSlideTriggerDelayMs
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxLongSlideTriggerDistance
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxSlideTriggerDistance
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxSubGestureTimeoutMs
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxSubGestureTriggerDistance
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinLongPressTriggerDelayMs
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinLongSlideTriggerDelayMs
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinLongSlideTriggerDistance
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinSlideTriggerDistance
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinSubGestureTimeoutMs
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinSubGestureTriggerDistance
import hunoia.sideleap.system.vibration.VibrationDefaults.MaxCustomVibrationMs
import hunoia.sideleap.system.vibration.VibrationDefaults.MinCustomVibrationMs
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.getPredefinedVibrationEffectText
import hunoia.sideleap.ui.screen.settings.gesture.GestureSettingsVM.UiEvent
import hunoia.sideleap.ui.theme.ContentPaddingHorizontal
import hunoia.sideleap.ui.theme.ContentPaddingVerticalWithSection
import hunoia.sideleap.ui.theme.ItemPadding
import hunoia.sideleap.ui.theme.MinItemHeightNoSecondary
import hunoia.sideleap.ui.theme.SectionPadding
import hunoia.sideleap.system.vibration.VibrationEffects
import hunoia.sideleap.ui.component.MyColumn
import hunoia.sideleap.ui.component.SectionCard
import hunoia.sideleap.ui.component.MyTextSlider
import hunoia.sideleap.ui.component.LabeledSwitch
import hunoia.sideleap.ui.component.TopBar
import hunoia.sideleap.ui.component.BottomSheetNestedContent
import kotlinx.coroutines.launch

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/23
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureSettingsScreen(
    onBack: () -> Unit,
    vm: GestureSettingsVM = viewModel()
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var showSlideSettings by remember { mutableStateOf(false) }
    UDFComponent(
        component = vm.udfComponent,
        onEvent = { event ->
            when (event) {
                UiEvent.ScrollToBottom -> {
                    coroutineScope.launch {
                        scrollState.animateScrollBy(
                            value = 1000f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                        )
                    }
                }
            }
        }
    ) { uiState ->
        var localVibrationMs by remember(uiState.vibrations.customVibrationMs) { mutableStateOf(uiState.vibrations.customVibrationMs.toFloat()) }
        if (showSlideSettings) {
            ModalBottomSheet(
                onDismissRequest = { showSlideSettings = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                BottomSheetNestedContent {
                    MyColumn(scrollState = rememberScrollState()) {
                        SlideSettingsContent(
                            uiState = uiState,
                            vm = vm,
                        )
                    }
                }
            }
        }
        Column {
            TopBar(
                onBack = onBack,
                title = stringResource(id = R.string.gesture_settings)
            )
            MyColumn(scrollState = scrollState) {
                SectionCard {
                    LabeledSwitch(
                        onCheckedChange = { vm.onPreciseSlideTypeChange(it) },
                        checked = uiState.isPreciseSlideTypeEnabled,
                        text = stringResource(id = R.string.precise_slide_type),
                        secondaryText = stringResource(id = R.string.precise_slide_type_hint)
                    )
                }
                SectionCard(
                    modifier = Modifier.padding(top = SectionPadding),
                    title = stringResource(id = R.string.slide_action)
                ) {
                    LabeledSwitch(
                        onCheckedChange = { vm.onVibrateForSlide(it) },
                        checked = uiState.vibrations.slideEnabled,
                        text = stringResource(id = R.string.vibration),
                        secondaryText = stringResource(id = R.string.vibration_hint)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = MinItemHeightNoSecondary)
                            .onSingleClick { showSlideSettings = true }
                            .padding(horizontal = ContentPaddingHorizontal, vertical = ContentPaddingVerticalWithSection),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(ItemPadding)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.trigger_distance),
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1
                            )
                            Text(
                                text = slideSettingsSummaryText(uiState),
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                SectionCard(
                    modifier = Modifier.padding(top = SectionPadding),
                    title = stringResource(id = R.string.long_slide_action)
                ) {
                    LabeledSwitch(
                        onCheckedChange = { vm.onLongSlideTriggerImmediatelyChange(it) },
                        checked = uiState.longSlideTriggerImmediately,
                        text = stringResource(id = R.string.long_slide_trigger_immediately),
                        secondaryText = stringResource(id = R.string.long_slide_trigger_immediately_hint)
                    )
                    LabeledSwitch(
                        onCheckedChange = { vm.onVibrateForLongSlide(it) },
                        checked = uiState.vibrations.longSlideEnabled,
                        text = stringResource(id = R.string.vibration),
                        secondaryText = stringResource(id = R.string.vibration_hint)
                    )
                }
                SectionCard(
                    modifier = Modifier.padding(top = SectionPadding),
                    title = stringResource(id = R.string.vibration)
                ) {
                    LabeledSwitch(
                        onCheckedChange = { vm.onVibrateImmediatelyChange(it) },
                        checked = uiState.vibrations.vibrateImmediately,
                        text = stringResource(id = R.string.vibrate_immediately),
                        secondaryText = stringResource(id = R.string.vibrate_immediately_hint)
                    )
                    LabeledSwitch(
                        onCheckedChange = { vm.onVibrateForTap(it) },
                        checked = uiState.vibrations.tapEnabled,
                        text = stringResource(id = R.string.vibration_tap),
                        secondaryText = stringResource(id = R.string.vibration_tap_hint)
                    )
                    LabeledSwitch(
                        onCheckedChange = { vm.onVibrateForLongPress(it) },
                        checked = uiState.vibrations.longPressEnabled,
                        text = stringResource(id = R.string.vibration_long_press),
                        secondaryText = stringResource(id = R.string.vibration_long_press_hint)
                    )
                    LabeledSwitch(
                        onCheckedChange = { vm.onVibrateForSubGesture(it) },
                        checked = uiState.vibrations.subGestureEnabled,
                        text = stringResource(id = R.string.vibration_sub_gesture),
                        secondaryText = stringResource(id = R.string.vibration_sub_gesture_hint)
                    )
                    if (uiState.canShowPredefinedVibration) {
                        LabeledSwitch(
                            onCheckedChange = { vm.onCustomVibrationChange(it) },
                            checked = uiState.isCustomVibration,
                            text = stringResource(id = R.string.custom_vibration),
                            secondaryText = stringResource(id = R.string.custom_vibration_hint)
                        )
                        AnimatedVisibility(
                            visible = uiState.isCustomVibration,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = MinItemHeightNoSecondary)
                                        .onSingleClick {
                                            vm.showPredefinedVibrationDropdown(true)
                                        }
                                        .padding(
                                            horizontal = ContentPaddingHorizontal,
                                            vertical = ContentPaddingVerticalWithSection
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(ItemPadding)
                                ) {
                                    Text(
                                        modifier = Modifier.weight(1f),
                                        text = stringResource(id = R.string.predefined_style),
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1
                                    )
                                    Box {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = getPredefinedVibrationEffectText(effect = uiState.vibrations.predefinedEffect),
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.titleMedium,
                                                maxLines = 1
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = stringResource(id = R.string.vibration_style)
                                            )
                                        }
                                        DropdownMenu(
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            shape = MaterialTheme.shapes.medium,
                                            expanded = uiState.showPredefinedVibrationDropdown,
                                            onDismissRequest = { vm.showPredefinedVibrationDropdown(false) }
                                        ) {
                                            listOf(
                                                VibrationEffects.Tick to getPredefinedVibrationEffectText(effect = VibrationEffects.Tick),
                                                VibrationEffects.Click to getPredefinedVibrationEffectText(effect = VibrationEffects.Click),
                                                VibrationEffects.HeavyClick to getPredefinedVibrationEffectText(effect = VibrationEffects.HeavyClick),
                                                VibrationEffects.None to getPredefinedVibrationEffectText(effect = VibrationEffects.None)
                                            ).fastForEach { (effectValue, text) ->
                                                key(effectValue) {
                                                    DropdownMenuItem(
                                                        onClick = {
                                                            vm.updatePredefinedVibration(effectValue)
                                                            vm.showPredefinedVibrationDropdown(false)
                                                        },
                                                        text = {
                                                            Text(text = text)
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                MyTextSlider(
                                    enabled = uiState.vibrations.predefinedEffect == VibrationEffects.None,
                                    value = localVibrationMs,
                                    onValueChange = { localVibrationMs = it },
                                    onValueChangeFinished = {
                                        vm.onCustomVibrationMsChange(localVibrationMs)
                                        vm.saveSettings()
                                    },
                                    text = stringResource(id = R.string.vibration_strength),
                                    valueDisplay = "${localVibrationMs.toLong()}ms",
                                    valueRange = MinCustomVibrationMs.toFloat()..MaxCustomVibrationMs.toFloat()
                                )
                            }
                        }
                    } else {
                        MyTextSlider(
                            value = localVibrationMs,
                            onValueChange = { localVibrationMs = it },
                            onValueChangeFinished = {
                                vm.onCustomVibrationMsChange(localVibrationMs)
                                vm.saveSettings()
                            },
                            text = stringResource(id = R.string.vibration_strength),
                            valueDisplay = "${localVibrationMs.toLong()}ms",
                            valueRange = MinCustomVibrationMs.toFloat()..MaxCustomVibrationMs.toFloat()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SlideSettingsContent(
    uiState: GestureSettingsVM.UiState,
    vm: GestureSettingsVM,
) {
    Column {
        var localLongPressDelay by remember(uiState.longPressTriggerDelayMs) { mutableStateOf(uiState.longPressTriggerDelayMs.toFloat()) }
        var localLongSlideDelay by remember(uiState.longSlideTriggerDelayMs) { mutableStateOf(uiState.longSlideTriggerDelayMs.toFloat()) }
        var localSubTriggerDist by remember(uiState.subGestureTriggerDistance) { mutableStateOf(uiState.subGestureTriggerDistance) }
        var localSubTimeout by remember(uiState.subGestureTimeoutMs) { mutableStateOf(uiState.subGestureTimeoutMs / 1000f) }
        MyTextSlider(
            value = uiState.slideTriggerDistance,
            onValueChange = { vm.onSlideTriggerDistanceChange(it) },
            onValueChangeFinished = { vm.saveSettings() },
            text = stringResource(id = R.string.trigger_distance),
            valueDisplay = "${uiState.slideTriggerDistance.toInt()}px",
            valueRange = MinSlideTriggerDistance.toFloat()..MaxSlideTriggerDistance.toFloat(),
        )
        MyTextSlider(
            value = localLongPressDelay,
            onValueChange = { localLongPressDelay = it },
            onValueChangeFinished = {
                vm.onLongPressTriggerDelayMsChange(localLongPressDelay)
                vm.saveSettings()
            },
            text = stringResource(id = R.string.long_press_trigger_delay_ms),
            valueDisplay = "${localLongPressDelay.toLong()}ms",
            valueRange = MinLongPressTriggerDelayMs.toFloat()..MaxLongPressTriggerDelayMs.toFloat(),
        )
        MyTextSlider(
            value = uiState.longSlideTriggerDistance,
            onValueChange = { vm.onLongSlideTriggerDistanceChange(it) },
            onValueChangeFinished = { vm.saveSettings() },
            text = stringResource(id = R.string.long_slide_trigger_distance),
            valueDisplay = "${uiState.longSlideTriggerDistance.toInt()}px",
            valueRange = MinLongSlideTriggerDistance.toFloat()..MaxLongSlideTriggerDistance.toFloat(),
        )
        MyTextSlider(
            value = localLongSlideDelay,
            onValueChange = { localLongSlideDelay = it },
            onValueChangeFinished = {
                vm.onLongSlideTriggerDelayMsChange(localLongSlideDelay)
                vm.saveSettings()
            },
            text = stringResource(id = R.string.long_slide_trigger_delay_ms),
            valueDisplay = "${localLongSlideDelay.toLong()}ms",
            valueRange = MinLongSlideTriggerDelayMs.toFloat()..MaxLongSlideTriggerDelayMs.toFloat(),
        )
        MyTextSlider(
            value = localSubTriggerDist,
            onValueChange = { localSubTriggerDist = it },
            onValueChangeFinished = {
                vm.onSubGestureTriggerDistanceChange(localSubTriggerDist)
                vm.saveSettings()
            },
            text = stringResource(id = R.string.sub_gesture_trigger_distance),
            valueDisplay = "${localSubTriggerDist.toInt()}px",
            valueRange = MinSubGestureTriggerDistance.toFloat()..MaxSubGestureTriggerDistance.toFloat(),
        )
        MyTextSlider(
            value = localSubTimeout,
            onValueChange = { localSubTimeout = it },
            onValueChangeFinished = {
                vm.onSubGestureTimeoutChange(localSubTimeout * 1000)
                vm.saveSettings()
            },
            text = "子手势超时",
            valueDisplay = "${localSubTimeout.toLong()}秒",
            valueRange = MinSubGestureTimeoutMs / 1000f..MaxSubGestureTimeoutMs / 1000f,
        )
    }
}

private fun slideSettingsSummaryText(uiState: GestureSettingsVM.UiState): String {
    return "滑动 ${uiState.slideTriggerDistance.toInt()} · 长按 ${uiState.longPressTriggerDelayMs}ms · 长滑 ${uiState.longSlideTriggerDistance.toInt()} · 延迟 ${uiState.longSlideTriggerDelayMs}ms · 子手势 ${uiState.subGestureTimeoutMs / 1000}s · 子距离 ${uiState.subGestureTriggerDistance.toInt()}px"
}
