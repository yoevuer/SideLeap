package hunoia.luno.ui.screen.settings.gesture
import hunoia.luno.ui.theme.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.component.UDFComponent
import hunoia.luno.R
import hunoia.luno.ui.action.actionText
import hunoia.luno.action.payload.SubGestureActionData
import hunoia.luno.gesture.SubGestureDirection
import hunoia.luno.ui.ext.displayNameRes
import hunoia.luno.settings.defaults.SettingsUiDefaults.GestureButtonColorAlpha
import hunoia.luno.settings.model.SubGesture
import hunoia.luno.ui.navigation.SubGestureActionSelect
import hunoia.luno.ui.component.OptimizedBottomSheet
import hunoia.luno.ui.component.ColorPickerDialog
import hunoia.luno.ui.component.MyAlertDialog
import hunoia.luno.ui.component.MyColumn
import hunoia.luno.ui.component.MyTextSlider
import hunoia.luno.ui.component.ExpressiveCard
import hunoia.luno.ui.component.ExpressiveRow
import hunoia.luno.ui.component.ExpressiveSwitchItem
import hunoia.luno.ui.component.TopBar
import hunoia.luno.ui.screen.settings.gesture.SubGestureSettingsVM.UiEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import com.aaron.compose.ktx.onSingleClick
import hunoia.luno.settings.defaults.SettingsUiDefaults.getPredefinedVibrationEffectText
import hunoia.luno.settings.defaults.SettingsUiDefaults.MinSubGestureTriggerDistance
import hunoia.luno.settings.defaults.SettingsUiDefaults.MaxSubGestureTriggerDistance
import hunoia.luno.system.vibration.MaxCustomVibrationMs
import hunoia.luno.system.vibration.MinCustomVibrationMs
import hunoia.luno.system.vibration.VibrationEffects
import hunoia.luno.ui.theme.ContentPaddingHorizontal
import hunoia.luno.ui.theme.ContentPaddingVerticalWithSection
import hunoia.luno.ui.theme.IconTextPadding
import hunoia.luno.ui.theme.ItemPadding
import hunoia.luno.ui.theme.MinItemHeightNoSecondary
import hunoia.luno.ui.theme.IconTextPadding
import hunoia.luno.ui.theme.MarkColorSize
import hunoia.luno.ui.theme.SectionPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubGestureSettingsScreen(
    onBack: () -> Unit,
    onNavToSubGestureActionSelect: (SubGestureActionSelect) -> Unit = {},
    vm: SubGestureSettingsVM = viewModel()
) {
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showGestureAngles by remember { mutableStateOf(false) }
    var showSubVibrationSettings by remember { mutableStateOf(false) }
    var showSubTriggerDistanceSettings by remember { mutableStateOf(false) }
    UDFComponent(component = vm.udfComponent, onEvent = { }) { uiState ->
        if (uiState.showDeleteWarningDialog) {
            MyAlertDialog(
                onDismissRequest = { vm.showDeleteWarningDialog(false) },
                title = stringResource(id = R.string.delete_sub_gesture_warning),
                text = stringResource(id = R.string.delete_sub_gesture_warning_desc),
                onConfirmClick = { vm.deleteSubGesture() }
            )
        }
        if (uiState.showMirrorCopyDialog) {
            MyAlertDialog(
                onDismissRequest = { vm.showMirrorCopyDialog(false) },
                title = stringResource(id = R.string.mirror_sub_gesture),
                text = stringResource(id = R.string.mirror_sub_gesture_desc),
                onConfirmClick = { vm.createMirroredCopy() }
            )
        }

        val gesture = uiState.subGesture ?: return@UDFComponent

        if (showEditNameDialog) {
            androidx.compose.material3.AlertDialog(
                containerColor = MaterialTheme.colorScheme.surface,
                onDismissRequest = { showEditNameDialog = false },
                title = { Text(text = stringResource(id = R.string.sub_gesture)) },
                text = {
                    OutlinedTextField(
                        value = uiState.editingName,
                        onValueChange = { vm.updateName(it) },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        vm.confirmName()
                        showEditNameDialog = false
                    }) {
                        Text(text = stringResource(id = R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditNameDialog = false }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                }
            )
        }

        if (showGestureAngles) {
            OptimizedBottomSheet(
                onDismissRequest = { showGestureAngles = false }
            ) {
                SubGestureAngleContent(
                    angle = gesture.angle,
                    onDismiss = { showGestureAngles = false },
                    onSave = { newAngle ->
                        vm.updateAngle(newAngle)
                        showGestureAngles = false
                    },
                    color = Color(gesture.color)
                )
            }
        }

        if (showSubVibrationSettings) {
            OptimizedBottomSheet(
                onDismissRequest = { showSubVibrationSettings = false }
            ) {
                SubGestureVibrationContent(
                    gesture = gesture,
                    vm = vm
                )
            }
        }

        if (showSubTriggerDistanceSettings) {
            OptimizedBottomSheet(
                onDismissRequest = { showSubTriggerDistanceSettings = false }
            ) {
                SubGestureTriggerDistanceContent(
                    gesture = gesture,
                    vm = vm
                )
            }
        }

        if (uiState.colorPickerDialog.first) {
            ColorPickerDialog(
                onDismissRequest = { vm.colorPickerDialog.show(false) },
                onColorPicked = { color ->
                    vm.colorPickerDialog.onColorChange(color)
                    vm.colorPickerDialog.confirm()
                },
                initialColor = uiState.colorPickerDialog.second
            )
        }

        Column {
            TopBar(
                onBack = onBack,
                title = uiState.editingName.ifEmpty { stringResource(id = R.string.sub_gesture) },
                onTitleClick = { showEditNameDialog = true },
                titleStyle = MaterialTheme.typography.titleLarge,
                postfixTitle = {
                    Box(
                        modifier = Modifier
                            .padding(start = IconTextPadding)
                            .size(MarkColorSize)
                            .background(
                                color = Color(gesture.color).copy(alpha = GestureButtonColorAlpha),
                                shape = CircleShape
                            )
                    )
                },
                actions = {
                    IconButton(onClick = { vm.showMirrorCopyDialog(true) }) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = { vm.showDeleteWarningDialog(true) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null
                        )
                    }
                }
            )

            MyColumn(verticalArrangement = Arrangement.spacedBy(Spacing12)) {
                ExpressiveCard(
                    icon = Icons.Default.TouchApp,
                    title = stringResource(id = R.string.direction_actions),
                    subtitle = "8 个方向触发动作配置",
                    onClick = {},
                ) {
                    val directions = listOf(
                        SubGestureDirection.Up, SubGestureDirection.Down,
                        SubGestureDirection.Left, SubGestureDirection.Right,
                        SubGestureDirection.UpRight, SubGestureDirection.DownRight,
                        SubGestureDirection.DownLeft, SubGestureDirection.UpLeft
                    )
                    directions.fastForEach { direction ->
                        val action = gesture.actionFor(direction)
                        val text = actionDisplayText(action, uiState.allSubGestures)
                        ExpressiveRow(
                            onClick = {
                                onNavToSubGestureActionSelect(SubGestureActionSelect(gesture.id, direction))
                            },
                            text = stringResource(id = direction.displayNameRes),
                            secondaryText = text.ifEmpty { stringResource(id = R.string.action_none) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Gesture,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            },
                        )
                    }
                }

                ExpressiveCard(
                    icon = Icons.Default.Tune,
                    title = "物理参数",
                    subtitle = "角度、震动与触发距离",
                    onClick = {},
                ) {
                    ExpressiveRow(
                        onClick = { showGestureAngles = true },
                        text = stringResource(id = R.string.sub_gesture_angles),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Straighten,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                    )
                    ExpressiveRow(
                        onClick = { showSubVibrationSettings = true },
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
                        onClick = { showSubTriggerDistanceSettings = true },
                        text = stringResource(id = R.string.gesture_button_trigger_distance),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                    )
                }

                ExpressiveCard(
                    icon = Icons.Default.Palette,
                    title = stringResource(id = R.string.gesture_button_color),
                    subtitle = "自定义子手势颜色",
                    onClick = { vm.colorPickerDialog.show(true) },
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(
                                color = Color(gesture.color),
                                shape = CircleShape
                            )
                            .border(
                                width = Spacing1,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun actionDisplayText(actionId: String?, allSubGestures: List<SubGesture>?): String {
    if (actionId == null || actionId.isEmpty()) return ""
    if (actionId != hunoia.luno.action.GlobalActions.SUB_GESTURE) {
        return actionText(hunoia.luno.action.Action(actionId))
    }
    return stringResource(id = R.string.action_sub_gesture)
}

@Composable
private fun SubGestureVibrationContent(
    gesture: SubGesture,
    vm: SubGestureSettingsVM
) {
    MyColumn(verticalArrangement = Arrangement.spacedBy(Spacing8)) {
        ExpressiveSwitchItem(
            onCheckedChange = { vm.onSubVibrateChange(it) },
            checked = gesture.vibrate,
            title = stringResource(R.string.sub_gesture_vibration)
        )
        ExpressiveSwitchItem(
            onCheckedChange = { vm.onSubVibrateImmediatelyChange(it) },
            checked = gesture.vibrateImmediately,
            title = stringResource(R.string.vibrate_immediately),
            subtitle = stringResource(R.string.vibrate_immediately_hint)
        )
        SubVibrationEffectSelector(
            effect = gesture.vibrationEffect,
            onEffectChange = { vm.onSubVibrationEffectChange(it) }
        )
        MyTextSlider(
            enabled = gesture.vibrationEffect == VibrationEffects.None,
            value = gesture.customVibrationMs.toFloat(),
            onValueChange = { vm.onSubCustomVibrationMsChange(it) },
            text = stringResource(R.string.vibration_strength),
            valueDisplay = "${gesture.customVibrationMs}ms",
            valueRange = MinCustomVibrationMs.toFloat()..MaxCustomVibrationMs.toFloat()
        )
    }
}

@Composable
private fun SubVibrationEffectSelector(
    effect: VibrationEffects,
    onEffectChange: (VibrationEffects) -> Unit
) {
    var showDropdown by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = MinItemHeightNoSecondary)
            .onSingleClick { showDropdown = true }
            .padding(horizontal = ContentPaddingHorizontal, vertical = ContentPaddingVerticalWithSection),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ItemPadding)
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.predefined_style),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1
        )
        Box {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = getPredefinedVibrationEffectText(effect),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = stringResource(R.string.vibration_style)
                )
            }
            DropdownMenu(
                containerColor = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium,
                expanded = showDropdown,
                onDismissRequest = { showDropdown = false }
            ) {
                listOf(
                    VibrationEffects.Tick,
                    VibrationEffects.Click,
                    VibrationEffects.HeavyClick,
                    VibrationEffects.None
                ).fastForEach { effectValue ->
                    key(effectValue) {
                        DropdownMenuItem(
                            onClick = {
                                onEffectChange(effectValue)
                                showDropdown = false
                            },
                            text = { Text(text = getPredefinedVibrationEffectText(effectValue)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubGestureTriggerDistanceContent(
    gesture: SubGesture,
    vm: SubGestureSettingsVM
) {
    MyColumn {
        MyTextSlider(
            value = gesture.triggerDistance.toFloat(),
            onValueChange = { vm.onSubTriggerDistanceChange(it) },
            text = stringResource(R.string.trigger_sub_gesture_distance),
            valueDisplay = "${gesture.triggerDistance}px",
            valueRange = MinSubGestureTriggerDistance.toFloat()..MaxSubGestureTriggerDistance.toFloat()
        )
    }
}
