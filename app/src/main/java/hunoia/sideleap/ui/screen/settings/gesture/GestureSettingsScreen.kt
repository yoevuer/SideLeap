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
import hunoia.sideleap.settings.model.GestureSettings
import hunoia.sideleap.settings.model.GestureSettings.VirtualMouseTrailStyle
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxLongPressTriggerDelayMs
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxLongSlideTriggerDelayMs
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxLongSlideTriggerDistance
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxSlideTriggerDistance
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinLongPressTriggerDelayMs
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinLongSlideTriggerDelayMs
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinLongSlideTriggerDistance
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinSlideTriggerDistance
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

private val VirtualMouseTimeoutOptions = listOf(0L, 5_000L, 10_000L, 15_000L, 30_000L)
private val VirtualMouseTrailStyleOptions = listOf(
    VirtualMouseTrailStyle.None,
    VirtualMouseTrailStyle.Dots,
    VirtualMouseTrailStyle.LightBand,
)

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
    var showVirtualMouseSettings by remember { mutableStateOf(false) }
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
        if (showVirtualMouseSettings) {
            ModalBottomSheet(
                onDismissRequest = { showVirtualMouseSettings = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                BottomSheetNestedContent {
                    MyColumn(scrollState = rememberScrollState()) {
                        VirtualMouseSettingsContent(
                            uiState = uiState,
                            vm = vm,
                        )
                    }
                }
            }
        }
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
                    title = stringResource(id = R.string.virtual_mouse)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = MinItemHeightNoSecondary)
                            .onSingleClick { showVirtualMouseSettings = true }
                            .padding(horizontal = ContentPaddingHorizontal, vertical = ContentPaddingVerticalWithSection),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(ItemPadding)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.virtual_mouse),
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1
                            )
                            Text(
                                text = virtualMouseSummaryText(uiState.virtualMouse),
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
                    title = stringResource(id = R.string.vibration)
                ) {
                    LabeledSwitch(
                        onCheckedChange = { vm.onVibrateImmediatelyChange(it) },
                        checked = uiState.vibrations.vibrateImmediately,
                        text = stringResource(id = R.string.vibrate_immediately),
                        secondaryText = stringResource(id = R.string.vibrate_immediately_hint)
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
                                    value = uiState.vibrations.customVibrationMs.toFloat(),
                                    onValueChange = { vm.onCustomVibrationMsChange(it) },
                                    onValueChangeFinished = { vm.saveSettings() },
                                    text = stringResource(id = R.string.vibration_strength),
                                    sliderValueHint = stringResource(id = R.string.low) to stringResource(id = R.string.high),
                                    valueRange = MinCustomVibrationMs.toFloat()..MaxCustomVibrationMs.toFloat()
                                )
                            }
                        }
                    } else {
                        MyTextSlider(
                            value = uiState.vibrations.customVibrationMs.toFloat(),
                            onValueChange = { vm.onCustomVibrationMsChange(it) },
                            onValueChangeFinished = { vm.saveSettings() },
                            text = stringResource(id = R.string.vibration_strength),
                            sliderValueHint = stringResource(id = R.string.low) to stringResource(id = R.string.high),
                            valueRange = MinCustomVibrationMs.toFloat()..MaxCustomVibrationMs.toFloat()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VirtualMouseSettingsContent(
    uiState: GestureSettingsVM.UiState,
    vm: GestureSettingsVM,
) {
    val virtualMouse = uiState.virtualMouse
    var showTimeoutDropdown by remember { mutableStateOf(false) }
    var showTrailStyleDropdown by remember { mutableStateOf(false) }
    Column {
        LabeledSwitch(
            onCheckedChange = { vm.onVirtualMouseContinuousModeChange(it) },
            checked = virtualMouse.continuousMode,
            text = stringResource(id = R.string.virtual_mouse_continuous_mode),
            secondaryText = stringResource(id = R.string.virtual_mouse_continuous_mode_hint)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MinItemHeightNoSecondary)
                .onSingleClick { showTimeoutDropdown = true }
                .padding(horizontal = ContentPaddingHorizontal, vertical = ContentPaddingVerticalWithSection),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ItemPadding)
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.virtual_mouse_continuous_timeout),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )
            Box {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = virtualMouseTimeoutText(virtualMouse.continuousModeTimeoutMs),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }
                DropdownMenu(
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium,
                    expanded = showTimeoutDropdown,
                    onDismissRequest = { showTimeoutDropdown = false }
                ) {
                    VirtualMouseTimeoutOptions.fastForEach { timeoutMs ->
                        DropdownMenuItem(
                            onClick = {
                                vm.onVirtualMouseContinuousModeTimeoutChange(timeoutMs)
                                showTimeoutDropdown = false
                            },
                            text = { Text(virtualMouseTimeoutText(timeoutMs)) }
                        )
                    }
                }
            }
        }
        MyTextSlider(
            value = virtualMouse.sensitivityX,
            onValueChange = { vm.onVirtualMouseChange(virtualMouse.copy(sensitivityX = it)) },
            onValueChangeFinished = { vm.saveSettings() },
            text = stringResource(id = R.string.virtual_mouse_sensitivity_x, virtualMouse.sensitivityX),
            sliderValueHint = "0.5x" to "4.0x",
            valueRange = 0.5f..4f
        )
        MyTextSlider(
            value = virtualMouse.sensitivityY,
            onValueChange = { vm.onVirtualMouseChange(virtualMouse.copy(sensitivityY = it)) },
            onValueChangeFinished = { vm.saveSettings() },
            text = stringResource(id = R.string.virtual_mouse_sensitivity_y, virtualMouse.sensitivityY),
            sliderValueHint = "0.5x" to "4.0x",
            valueRange = 0.5f..4f
        )
        MyTextSlider(
            value = virtualMouse.acceleration,
            onValueChange = { vm.onVirtualMouseChange(virtualMouse.copy(acceleration = it)) },
            onValueChangeFinished = { vm.saveSettings() },
            text = stringResource(id = R.string.virtual_mouse_acceleration, virtualMouse.acceleration),
            sliderValueHint = "0.0" to "2.0",
            valueRange = 0f..2f
        )
        MyTextSlider(
            value = virtualMouse.cursorSizeDp.toFloat(),
            onValueChange = { vm.onVirtualMouseChange(virtualMouse.copy(cursorSizeDp = it.toInt())) },
            onValueChangeFinished = { vm.saveSettings() },
            text = stringResource(id = R.string.virtual_mouse_cursor_size, virtualMouse.cursorSizeDp),
            sliderValueHint = "12dp" to "64dp",
            valueRange = 12f..64f
        )
        MyTextSlider(
            value = virtualMouse.cursorAlpha,
            onValueChange = { vm.onVirtualMouseChange(virtualMouse.copy(cursorAlpha = it)) },
            onValueChangeFinished = { vm.saveSettings() },
            text = stringResource(id = R.string.virtual_mouse_cursor_alpha, (virtualMouse.cursorAlpha * 100).toInt()),
            sliderValueHint = "20%" to "100%",
            valueRange = 0.2f..1f
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MinItemHeightNoSecondary)
                .onSingleClick { showTrailStyleDropdown = true }
                .padding(horizontal = ContentPaddingHorizontal, vertical = ContentPaddingVerticalWithSection),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ItemPadding)
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.virtual_mouse_trail),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )
            Box {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = virtualMouseTrailStyleText(virtualMouse.trailStyle),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }
                DropdownMenu(
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium,
                    expanded = showTrailStyleDropdown,
                    onDismissRequest = { showTrailStyleDropdown = false }
                ) {
                    VirtualMouseTrailStyleOptions.fastForEach { style ->
                        DropdownMenuItem(
                            onClick = {
                                vm.onVirtualMouseTrailStyleChange(style)
                                showTrailStyleDropdown = false
                            },
                            text = { Text(virtualMouseTrailStyleText(style)) }
                        )
                    }
                }
            }
        }
        if (virtualMouse.trailStyle != VirtualMouseTrailStyle.None) {
            MyTextSlider(
                value = virtualMouse.trailStrength,
                onValueChange = { vm.onVirtualMouseChange(virtualMouse.copy(trailStrength = it)) },
                onValueChangeFinished = { vm.saveSettings() },
                text = stringResource(id = R.string.virtual_mouse_trail_strength, virtualMouse.trailStrength),
                sliderValueHint = "0.5" to "2.0",
                valueRange = 0.5f..2f
            )
            MyTextSlider(
                value = virtualMouse.trailAlpha,
                onValueChange = { vm.onVirtualMouseChange(virtualMouse.copy(trailAlpha = it)) },
                onValueChangeFinished = { vm.saveSettings() },
                text = stringResource(id = R.string.virtual_mouse_trail_alpha, (virtualMouse.trailAlpha * 100).toInt()),
                sliderValueHint = "20%" to "100%",
                valueRange = 0.2f..1f
            )
        }
        LabeledSwitch(
            onCheckedChange = { vm.onVirtualMouseClickAnimationChange(it) },
            checked = virtualMouse.clickAnimationEnabled,
            text = stringResource(id = R.string.virtual_mouse_click_animation)
        )
        LabeledSwitch(
            onCheckedChange = { vm.onVirtualMouseLongPressEnabledChange(it) },
            checked = virtualMouse.longPressEnabled,
            text = stringResource(id = R.string.virtual_mouse_long_press),
            secondaryText = stringResource(id = R.string.virtual_mouse_long_press_hint)
        )
        if (virtualMouse.longPressEnabled) {
            MyTextSlider(
                value = virtualMouse.longPressDelayMs.toFloat(),
                onValueChange = { vm.onVirtualMouseChange(virtualMouse.copy(longPressDelayMs = it.toLong())) },
                onValueChangeFinished = { vm.saveSettings() },
                text = stringResource(id = R.string.virtual_mouse_long_press_delay, virtualMouse.longPressDelayMs),
                sliderValueHint = "400ms" to "2000ms",
                valueRange = 400f..2000f
            )
            MyTextSlider(
                value = virtualMouse.longPressMoveToleranceDp.toFloat(),
                onValueChange = { vm.onVirtualMouseChange(virtualMouse.copy(longPressMoveToleranceDp = it.toInt())) },
                onValueChangeFinished = { vm.saveSettings() },
                text = stringResource(id = R.string.virtual_mouse_long_press_tolerance, virtualMouse.longPressMoveToleranceDp),
                sliderValueHint = "2dp" to "16dp",
                valueRange = 2f..16f
            )
        }
    }
}

@Composable
private fun SlideSettingsContent(
    uiState: GestureSettingsVM.UiState,
    vm: GestureSettingsVM,
) {
    Column {
        MyTextSlider(
            value = uiState.slideTriggerDistance,
            onValueChange = { vm.onSlideTriggerDistanceChange(it) },
            onValueChangeFinished = { vm.saveSettings() },
            text = stringResource(id = R.string.trigger_distance),
            sliderValueHint = stringResource(id = R.string.short1) to stringResource(id = R.string.long1),
            valueRange = MinSlideTriggerDistance.toFloat()..MaxSlideTriggerDistance.toFloat()
        )
        MyTextSlider(
            value = uiState.longPressTriggerDelayMs.toFloat(),
            onValueChange = { vm.onLongPressTriggerDelayMsChange(it) },
            onValueChangeFinished = { vm.saveSettings() },
            text = stringResource(id = R.string.long_press_trigger_delay_ms),
            sliderValueHint = stringResource(id = R.string.short1) to stringResource(id = R.string.long1),
            valueRange = MinLongPressTriggerDelayMs.toFloat()..MaxLongPressTriggerDelayMs.toFloat()
        )
        MyTextSlider(
            value = uiState.longSlideTriggerDistance,
            onValueChange = { vm.onLongSlideTriggerDistanceChange(it) },
            onValueChangeFinished = { vm.saveSettings() },
            text = stringResource(id = R.string.long_slide_trigger_distance),
            sliderValueHint = stringResource(id = R.string.short1) to stringResource(id = R.string.long1),
            valueRange = MinLongSlideTriggerDistance.toFloat()..MaxLongSlideTriggerDistance.toFloat()
        )
        MyTextSlider(
            value = uiState.longSlideTriggerDelayMs.toFloat(),
            onValueChange = { vm.onLongSlideTriggerDelayMsChange(it) },
            onValueChangeFinished = { vm.saveSettings() },
            text = stringResource(id = R.string.long_slide_trigger_delay_ms),
            sliderValueHint = stringResource(id = R.string.short1) to stringResource(id = R.string.long1),
            valueRange = MinLongSlideTriggerDelayMs.toFloat()..MaxLongSlideTriggerDelayMs.toFloat()
        )
    }
}

private fun slideSettingsSummaryText(uiState: GestureSettingsVM.UiState): String {
    return "滑动 ${uiState.slideTriggerDistance.toInt()} · 长按 ${uiState.longPressTriggerDelayMs}ms · 长滑 ${uiState.longSlideTriggerDistance.toInt()} · 延迟 ${uiState.longSlideTriggerDelayMs}ms"
}

private fun virtualMouseSummaryText(settings: GestureSettings.VirtualMouse): String {
    val mode = if (settings.continuousMode) "连续" else "单次"
    val longPress = if (settings.longPressEnabled) "长按 ${settings.longPressDelayMs}ms" else "长按关闭"
    return "${settings.sensitivityX.format1()}x / ${settings.sensitivityY.format1()}x · ${virtualMouseTrailStyleText(settings.trailStyle)} · $mode · $longPress"
}

private fun virtualMouseTimeoutText(timeoutMs: Long): String {
    return if (timeoutMs <= 0L) "关闭" else "${timeoutMs / 1000} 秒"
}

private fun virtualMouseTrailStyleText(style: VirtualMouseTrailStyle): String {
    return when (style) {
        VirtualMouseTrailStyle.None -> "关闭"
        VirtualMouseTrailStyle.Dots -> "残影点"
        VirtualMouseTrailStyle.LightBand -> "光带"
    }
}

private fun Float.format1(): String = String.format("%.1f", this)
