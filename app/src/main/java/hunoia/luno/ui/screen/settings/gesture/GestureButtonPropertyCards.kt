package hunoia.luno.ui.screen.settings.gesture
import hunoia.luno.ui.theme.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import hunoia.luno.R
import hunoia.luno.config.defaults.SettingsUiDefaults.GestureButtonColorAlpha
import hunoia.luno.config.defaults.SettingsUiDefaults.MaxGestureButtonPosition
import hunoia.luno.config.defaults.SettingsUiDefaults.MaxGestureButtonWidth
import hunoia.luno.config.defaults.SettingsUiDefaults.MinGestureButtonPosition
import hunoia.luno.config.defaults.SettingsUiDefaults.MinGestureButtonWidth
import hunoia.luno.config.model.GestureButton
import hunoia.luno.ui.component.ExpressiveCard
import hunoia.luno.ui.component.ExpressiveRow
import hunoia.luno.ui.component.ExpressiveSwitchItem
import hunoia.luno.ui.component.MyTextRangeSlider
import hunoia.luno.ui.component.MyTextSlider

@Composable
fun GestureButtonPhysicalParamsCard(
    gestureButton: GestureButton,
    isSideButton: Boolean,
    alignRegion: Boolean,
    vm: GestureButtonSettingsVM,
    onGestureAnglesClick: () -> Unit,
    onVibrationClick: () -> Unit,
    onTriggerDistanceClick: () -> Unit,
) {
    ExpressiveCard(
        icon = Icons.Default.Tune,
        title = "物理参数",
        subtitle = "角度、震动、触发距离与尺寸",
        onClick = {},
    ) {
        ExpressiveRow(
            onClick = onGestureAnglesClick,
            text = stringResource(id = R.string.gesture_angles),
            secondaryText = stringResource(id = R.string.gesture_button_angles_hint),
            icon = {
                Icon(
                    imageVector = Icons.Default.Straighten,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
        )
        ExpressiveRow(
            onClick = onVibrationClick,
            text = stringResource(id = R.string.gesture_button_vibration),
            secondaryText = stringResource(id = R.string.vibration_hint),
            icon = {
                Icon(
                    imageVector = Icons.Default.Vibration,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
        )
        ExpressiveRow(
            onClick = onTriggerDistanceClick,
            text = stringResource(id = R.string.gesture_button_trigger_distance),
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
        )
        var localBtnWidth by remember(gestureButton.width) { mutableStateOf(gestureButton.width.toFloat()) }
        MyTextSlider(
            value = localBtnWidth,
            onValueChange = { localBtnWidth = it },
            onValueChangeFinished = {
                vm.onGestureButtonWidthChange(localBtnWidth)
                vm.onGestureButtonAdjustFinish()
            },
            text = stringResource(id = R.string.gesture_button_width),
            valueDisplay = "${localBtnWidth.toInt()}px",
            valueRange = MinGestureButtonWidth.toFloat()..MaxGestureButtonWidth.toFloat()
        )
        MyTextRangeSlider(
            value = gestureButton.start..gestureButton.end,
            onValueChange = { vm.onGestureButtonPositionChange(it.start, it.endInclusive) },
            onValueChangeFinished = { vm.onGestureButtonAdjustFinish() },
            text = stringResource(id = R.string.gesture_button_length),
            sliderValueHint = stringResource(id = R.string.top) to stringResource(id = R.string.bottom),
            valueRange = MinGestureButtonPosition..MaxGestureButtonPosition
        )
        if (isSideButton) {
            ExpressiveSwitchItem(
                onCheckedChange = { vm.onGestureButtonAlignChange(it) },
                checked = alignRegion,
                title = stringResource(id = R.string.gesture_button_align),
                subtitle = stringResource(id = R.string.gesture_button_align_hint),
            )
        }
    }
}

@Composable
fun GestureButtonBehaviorCard(
    gestureButton: GestureButton,
    vm: GestureButtonSettingsVM,
) {
    ExpressiveCard(
        icon = Icons.Default.Keyboard,
        title = "行为适配",
        subtitle = "键盘与滑动模式",
        onClick = {},
    ) {
        ExpressiveSwitchItem(
            onCheckedChange = { vm.onFitSoftKeyboardChange(it) },
            checked = gestureButton.fitSoftKeyboard,
            title = stringResource(R.string.fit_soft_keyboard),
            subtitle = stringResource(R.string.fit_soft_keyboard_hint),
        )
        ExpressiveSwitchItem(
            onCheckedChange = { vm.onPreciseSlideTypeChange(it) },
            checked = gestureButton.isPreciseSlideType,
            title = stringResource(R.string.precise_slide_type),
            subtitle = stringResource(R.string.precise_slide_type_hint),
        )
    }
}

@Composable
fun GestureButtonDisplayCard(
    gestureButton: GestureButton,
    vm: GestureButtonSettingsVM,
) {
    ExpressiveCard(
        icon = Icons.Default.Visibility,
        title = "显示场景",
        subtitle = "横屏、锁屏与桌面隐藏",
        onClick = {},
    ) {
        ExpressiveSwitchItem(
            onCheckedChange = { vm.onHideLandscapeChange(it) },
            checked = gestureButton.hideLandscape,
            title = stringResource(R.string.landscape),
        )
        ExpressiveSwitchItem(
            onCheckedChange = { vm.onHideScreenLockChange(it) },
            checked = gestureButton.hideScreenLock,
            title = stringResource(R.string.lock_screen),
        )
        ExpressiveSwitchItem(
            onCheckedChange = { vm.onHideHomeScreenChange(it) },
            checked = gestureButton.hideHomeScreen,
            title = stringResource(R.string.launcher),
        )
    }
}
