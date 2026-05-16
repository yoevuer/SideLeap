package hunoia.sideleap.ui.screen.gesturesettings

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import hunoia.sideleap.ui.widget.TextActionButton
import hunoia.sideleap.ui.widget.MyTextSlider
import hunoia.sideleap.ui.widget.LabeledSwitch
import hunoia.sideleap.ui.widget.TopBar
import kotlinx.coroutines.launch

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/23
 */

@Composable
fun GestureSettingsScreen(
    onNavToGestureAngles: () -> Unit,
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
        Column {
            TopBar(
                onBack = onBack,
                title = stringResource(id = R.string.gesture_settings)
            )
            MyColumn(scrollState = scrollState) {
                SectionCard {
                    TextActionButton(
                        onClick = onNavToGestureAngles,
                        text = stringResource(id = R.string.gesture_angles),
                        secondaryText = stringResource(id = R.string.gesture_angles_hint)
                    )
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
