package hunoia.luno.ui.home

import hunoia.luno.ui.theme.*
import hunoia.luno.R
import hunoia.luno.ui.component.ExpressiveSwitchItem
import hunoia.luno.ui.component.MyColumn
import hunoia.luno.ui.component.input.MyTextSlider
import hunoia.luno.config.model.GestureSettings
import hunoia.luno.config.model.GestureSettings.PointerTrailStyle

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.util.fastForEach
import com.aaron.compose.ktx.onSingleClick

internal val PointerTrailStyleOptions = listOf(
    PointerTrailStyle.None,
    PointerTrailStyle.Dots,
    PointerTrailStyle.LightBand,
)

@Composable
internal fun PointerSettingsContent(
    pointer: GestureSettings.Pointer,
    vm: HomeVM,
    scrollState: ScrollState? = null,
) {
    var showTrailStyleDropdown by remember { mutableStateOf(false) }
    MyColumn(scrollState = scrollState ?: rememberScrollState()) {
        ExpressiveSwitchItem(
            onCheckedChange = { vm.onPointerContinuousModeChange(it) },
            checked = pointer.continuousMode,
            title = stringResource(id = R.string.pointer_continuous_mode),
            subtitle = stringResource(id = R.string.pointer_continuous_mode_hint)
        )
        val currentPointer by rememberUpdatedState(pointer)
        MyTextSlider(
            value = pointer.continuousModeTimeoutMs / 1000f,
            onValueChange = { vm.onPointerContinuousModeTimeoutChange((it * 1000).toLong()) },
            onValueChangeFinished = { vm.savePointerSettings() },
            text = stringResource(id = R.string.pointer_continuous_timeout_plain),
            valueDisplay = stringResource(id = R.string.pointer_continuous_timeout, pointer.continuousModeTimeoutMs / 1000),
            valueRange = 1f..10f,
        )
        MyTextSlider(
            value = pointer.sensitivityX,
            onValueChange = { vm.onPointerChange(pointer.copy(sensitivityX = it)) },
            onValueChangeFinished = { vm.savePointerSettings() },
            text = stringResource(id = R.string.pointer_sensitivity_x_plain),
            valueDisplay = stringResource(id = R.string.pointer_sensitivity_x, pointer.sensitivityX),
            valueRange = 0.5f..4f,
        )
        MyTextSlider(
            value = pointer.sensitivityY,
            onValueChange = { vm.onPointerChange(pointer.copy(sensitivityY = it)) },
            onValueChangeFinished = { vm.savePointerSettings() },
            text = stringResource(id = R.string.pointer_sensitivity_y_plain),
            valueDisplay = stringResource(id = R.string.pointer_sensitivity_y, pointer.sensitivityY),
            valueRange = 0.5f..4f,
        )
        MyTextSlider(
            value = pointer.acceleration,
            onValueChange = { vm.onPointerChange(pointer.copy(acceleration = it)) },
            onValueChangeFinished = { vm.savePointerSettings() },
            text = stringResource(id = R.string.pointer_acceleration_plain),
            valueDisplay = stringResource(id = R.string.pointer_acceleration, pointer.acceleration),
            valueRange = 0f..2f,
        )
        var localCursorSize by remember(pointer.cursorSizeDp) { mutableStateOf(pointer.cursorSizeDp.toFloat()) }
        MyTextSlider(
            value = localCursorSize,
            onValueChange = { localCursorSize = it },
            onValueChangeFinished = {
                vm.onPointerChange(currentPointer.copy(cursorSizeDp = localCursorSize.toInt()))
                vm.savePointerSettings()
            },
            text = stringResource(id = R.string.pointer_cursor_size_plain),
            valueDisplay = stringResource(id = R.string.pointer_cursor_size, localCursorSize.toInt()),
            valueRange = 12f..64f,
        )
        MyTextSlider(
            value = pointer.cursorAlpha,
            onValueChange = { vm.onPointerChange(pointer.copy(cursorAlpha = it)) },
            onValueChangeFinished = { vm.savePointerSettings() },
            text = stringResource(id = R.string.pointer_cursor_alpha_plain),
            valueDisplay = stringResource(id = R.string.pointer_cursor_alpha, (pointer.cursorAlpha * 100).toInt()),
            valueRange = 0.2f..1f,
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
                text = stringResource(id = R.string.pointer_trail),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )
            Box {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = pointerTrailStyleText(pointer.trailStyle),
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
                    PointerTrailStyleOptions.fastForEach { style ->
                        DropdownMenuItem(
                            onClick = {
                                vm.onPointerTrailStyleChange(style)
                                showTrailStyleDropdown = false
                            },
                            text = { Text(pointerTrailStyleText(style)) }
                        )
                    }
                }
            }
        }
        if (pointer.trailStyle != PointerTrailStyle.None) {
            MyTextSlider(
                value = pointer.trailStrength,
                onValueChange = { vm.onPointerChange(pointer.copy(trailStrength = it)) },
                onValueChangeFinished = { vm.savePointerSettings() },
                text = stringResource(id = R.string.pointer_trail_strength_plain),
                valueDisplay = stringResource(id = R.string.pointer_trail_strength, pointer.trailStrength),
                valueRange = 0.5f..2f,
            )
            MyTextSlider(
                value = pointer.trailAlpha,
                onValueChange = { vm.onPointerChange(pointer.copy(trailAlpha = it)) },
                onValueChangeFinished = { vm.savePointerSettings() },
                text = stringResource(id = R.string.pointer_trail_alpha_plain),
                valueDisplay = stringResource(id = R.string.pointer_trail_alpha, (pointer.trailAlpha * 100).toInt()),
                valueRange = 0.2f..1f,
            )
        }
        ExpressiveSwitchItem(
            onCheckedChange = { vm.onPointerClickAnimationChange(it) },
            checked = pointer.clickAnimationEnabled,
            title = stringResource(id = R.string.pointer_click_animation)
        )
        ExpressiveSwitchItem(
            onCheckedChange = { vm.onPointerLongPressEnabledChange(it) },
            checked = pointer.longPressEnabled,
            title = stringResource(id = R.string.pointer_long_press),
            subtitle = stringResource(id = R.string.pointer_long_press_hint)
        )
        if (pointer.longPressEnabled) {
            var localLongPressDelay by remember(pointer.longPressDelayMs) { mutableStateOf(pointer.longPressDelayMs.toFloat()) }
            MyTextSlider(
                value = localLongPressDelay,
                onValueChange = { localLongPressDelay = it },
                onValueChangeFinished = {
                    vm.onPointerChange(currentPointer.copy(longPressDelayMs = localLongPressDelay.toLong()))
                    vm.savePointerSettings()
                },
                text = stringResource(id = R.string.pointer_long_press_delay_plain),
                valueDisplay = stringResource(id = R.string.pointer_long_press_delay, localLongPressDelay.toLong()),
                valueRange = 400f..2000f,
            )
            var localTolerance by remember(pointer.longPressMoveToleranceDp) { mutableStateOf(pointer.longPressMoveToleranceDp.toFloat()) }
            MyTextSlider(
                value = localTolerance,
                onValueChange = { localTolerance = it },
                onValueChangeFinished = {
                    vm.onPointerChange(currentPointer.copy(longPressMoveToleranceDp = localTolerance.toInt()))
                    vm.savePointerSettings()
                },
                text = stringResource(id = R.string.pointer_long_press_tolerance_plain),
                valueDisplay = stringResource(id = R.string.pointer_long_press_tolerance, localTolerance.toInt()),
                valueRange = 2f..16f,
            )
        }
    }
}

@Composable
internal fun pointerTrailStyleText(style: PointerTrailStyle): String {
    return when (style) {
        PointerTrailStyle.None -> stringResource(id = R.string.pointer_trail_style_close)
        PointerTrailStyle.Dots -> stringResource(id = R.string.pointer_trail_style_dot)
        PointerTrailStyle.LightBand -> stringResource(id = R.string.pointer_trail_style_band)
    }
}
