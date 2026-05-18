package hunoia.sideleap.ui.screen.gesturesettings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.component.UDFComponent
import com.aaron.compose.ktx.onSingleClick
import hunoia.sideleap.R
import hunoia.sideleap.settings.api.SettingsUiDefaults.MaxLongPressTriggerDelayMs
import hunoia.sideleap.settings.api.SettingsUiDefaults.MaxLongSlideTriggerDelayMs
import hunoia.sideleap.settings.api.SettingsUiDefaults.MaxLongSlideTriggerDistance
import hunoia.sideleap.settings.api.SettingsUiDefaults.MaxSlideTriggerDistance
import hunoia.sideleap.settings.api.SettingsUiDefaults.MinLongPressTriggerDelayMs
import hunoia.sideleap.settings.api.SettingsUiDefaults.MinLongSlideTriggerDelayMs
import hunoia.sideleap.settings.api.SettingsUiDefaults.MinLongSlideTriggerDistance
import hunoia.sideleap.settings.api.SettingsUiDefaults.MinSlideTriggerDistance
import hunoia.sideleap.system.vibration.VibrationDefaults.MaxCustomVibrationMs
import hunoia.sideleap.system.vibration.VibrationDefaults.MinCustomVibrationMs
import hunoia.sideleap.settings.api.SettingsUiDefaults.getPredefinedVibrationEffectText
import hunoia.sideleap.ui.screen.gesturesettings.GestureSettingsVM.UiEvent
import hunoia.sideleap.ui.theme.ContentPaddingHorizontal
import hunoia.sideleap.ui.theme.ContentPaddingVerticalWithSection
import hunoia.sideleap.ui.theme.ItemPadding
import hunoia.sideleap.ui.theme.MinItemHeightNoSecondary
import hunoia.sideleap.ui.theme.SectionPadding
import hunoia.sideleap.system.vibration.VibrationEffects
import hunoia.sideleap.ui.widget.MyColumn
import hunoia.sideleap.ui.widget.SectionCard
import hunoia.sideleap.ui.widget.MyTextSlider
import hunoia.sideleap.ui.widget.LabeledSwitch
import hunoia.sideleap.ui.widget.TopBar
import hunoia.sideleap.ui.widget.ColorPickerDialog
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
        if (uiState.showVirtualMouseColorPicker) {
            ColorPickerDialog(
                onDismissRequest = { vm.showVirtualMouseColorPicker(false) },
                onColorPicked = { color ->
                    vm.onVirtualMouseChange(uiState.virtualMouse.copy(cursorColor = color.toArgb().toLong() and 0xFFFFFFFFL))
                    vm.saveSettings()
                },
                initialColor = Color(uiState.virtualMouse.cursorColor.toInt())
            )
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
                    MyTextSlider(
                        value = uiState.longSlideTriggerDistance,
                        onValueChange = { vm.onLongSlideTriggerDistanceChange(it) },
                        onValueChangeFinished = { vm.saveSettings() },
                        text = stringResource(id = R.string.trigger_distance),
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
                SectionCard(
                    modifier = Modifier.padding(top = SectionPadding),
                    title = stringResource(id = R.string.virtual_mouse)
                ) {
                    LabeledSwitch(
                        onCheckedChange = { vm.onVirtualMouseContinuousModeChange(it) },
                        checked = uiState.virtualMouse.continuousMode,
                        text = stringResource(id = R.string.virtual_mouse_continuous_mode),
                        secondaryText = stringResource(id = R.string.virtual_mouse_continuous_mode_hint)
                    )
                    MyTextSlider(
                        value = uiState.virtualMouse.sensitivityX,
                        onValueChange = { vm.onVirtualMouseChange(uiState.virtualMouse.copy(sensitivityX = it)) },
                        onValueChangeFinished = { vm.saveSettings() },
                        text = stringResource(id = R.string.virtual_mouse_sensitivity_x, uiState.virtualMouse.sensitivityX),
                        sliderValueHint = "0.5x" to "4.0x",
                        valueRange = 0.5f..4f
                    )
                    MyTextSlider(
                        value = uiState.virtualMouse.sensitivityY,
                        onValueChange = { vm.onVirtualMouseChange(uiState.virtualMouse.copy(sensitivityY = it)) },
                        onValueChangeFinished = { vm.saveSettings() },
                        text = stringResource(id = R.string.virtual_mouse_sensitivity_y, uiState.virtualMouse.sensitivityY),
                        sliderValueHint = "0.5x" to "4.0x",
                        valueRange = 0.5f..4f
                    )
                    MyTextSlider(
                        value = uiState.virtualMouse.acceleration,
                        onValueChange = { vm.onVirtualMouseChange(uiState.virtualMouse.copy(acceleration = it)) },
                        onValueChangeFinished = { vm.saveSettings() },
                        text = stringResource(id = R.string.virtual_mouse_acceleration, uiState.virtualMouse.acceleration),
                        sliderValueHint = "0.0" to "2.0",
                        valueRange = 0f..2f
                    )
                    MyTextSlider(
                        value = uiState.virtualMouse.cursorSizeDp.toFloat(),
                        onValueChange = { vm.onVirtualMouseChange(uiState.virtualMouse.copy(cursorSizeDp = it.toInt())) },
                        onValueChangeFinished = { vm.saveSettings() },
                        text = stringResource(id = R.string.virtual_mouse_cursor_size, uiState.virtualMouse.cursorSizeDp),
                        sliderValueHint = "12dp" to "64dp",
                        valueRange = 12f..64f
                    )
                    MyTextSlider(
                        value = uiState.virtualMouse.cursorAlpha,
                        onValueChange = { vm.onVirtualMouseChange(uiState.virtualMouse.copy(cursorAlpha = it)) },
                        onValueChangeFinished = { vm.saveSettings() },
                        text = stringResource(id = R.string.virtual_mouse_cursor_alpha, (uiState.virtualMouse.cursorAlpha * 100).toInt()),
                        sliderValueHint = "20%" to "100%",
                        valueRange = 0.2f..1f
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = MinItemHeightNoSecondary)
                            .onSingleClick { vm.showVirtualMouseColorPicker(true) }
                            .padding(horizontal = ContentPaddingHorizontal, vertical = ContentPaddingVerticalWithSection),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(ItemPadding)
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = stringResource(id = R.string.virtual_mouse_cursor_color),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                .background(Color(uiState.virtualMouse.cursorColor.toInt()), CircleShape)
                        )
                    }
                    LabeledSwitch(
                        onCheckedChange = { vm.onVirtualMouseOuterRingChange(it) },
                        checked = uiState.virtualMouse.outerRingEnabled,
                        text = stringResource(id = R.string.virtual_mouse_outer_ring)
                    )
                    LabeledSwitch(
                        onCheckedChange = { vm.onVirtualMouseShadowChange(it) },
                        checked = uiState.virtualMouse.shadowEnabled,
                        text = stringResource(id = R.string.virtual_mouse_shadow)
                    )
                    LabeledSwitch(
                        onCheckedChange = { vm.onVirtualMouseClickAnimationChange(it) },
                        checked = uiState.virtualMouse.clickAnimationEnabled,
                        text = stringResource(id = R.string.virtual_mouse_click_animation)
                    )
                    LabeledSwitch(
                        onCheckedChange = { vm.onVirtualMouseTrailChange(it) },
                        checked = uiState.virtualMouse.trailEnabled,
                        text = stringResource(id = R.string.virtual_mouse_trail)
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
