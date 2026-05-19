package hunoia.sideleap.ui.screen.gesturebuttonsettings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.component.UDFComponent
import hunoia.sideleap.R
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.GestureButtonColorAlpha
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxGestureButtonPosition
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MaxGestureButtonWidth
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinGestureButtonPosition
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.MinGestureButtonWidth
import hunoia.sideleap.ui.navigation.ActionSelect
import hunoia.sideleap.gesture.GestureButton
import hunoia.sideleap.ui.component.BottomSheetNestedContent
import hunoia.sideleap.gesture.Position
import hunoia.sideleap.gesture.TriggerDirection
import hunoia.sideleap.gesture.TriggerDirection.Center
import hunoia.sideleap.gesture.TriggerDirection.Center2
import hunoia.sideleap.gesture.TriggerDirection.Down
import hunoia.sideleap.gesture.TriggerDirection.Down2
import hunoia.sideleap.gesture.TriggerDirection.Up
import hunoia.sideleap.gesture.TriggerDirection.Up2
import hunoia.sideleap.action.display.actionTextCompose
import hunoia.sideleap.gesture.bounds
import hunoia.sideleap.gesture.styleBy
import hunoia.sideleap.settings.model.ActionPanelStyles
import hunoia.sideleap.settings.model.LongSlideActionPanelStyles
import hunoia.sideleap.ui.screen.actionselect.ActionSelectContent
import hunoia.sideleap.ui.screen.gestureangles.GestureButtonAngleContent
import hunoia.sideleap.ui.theme.IconTextPadding
import hunoia.sideleap.ui.theme.MarkColorSize
import hunoia.sideleap.ui.theme.SectionPadding
import hunoia.sideleap.ui.theme.SectionPaddingNoTitle
import hunoia.sideleap.ui.component.ColorPickerDialog
import hunoia.sideleap.ui.component.MyAlertDialog
import hunoia.sideleap.ui.component.MyColumn
import hunoia.sideleap.ui.component.SectionCard
import hunoia.sideleap.ui.component.TextActionButton
import hunoia.sideleap.ui.component.MyTextRangeSlider
import hunoia.sideleap.ui.component.MyTextSlider
import hunoia.sideleap.ui.component.LabeledSwitch
import hunoia.sideleap.ui.component.TopBar

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/28
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureButtonSettingsScreen(
    onBack: () -> Unit,
    vm: GestureButtonSettingsVM = viewModel()
) {
    var showActionSelect by remember { mutableStateOf(false) }
    var pendingActionSelect by remember { mutableStateOf<ActionSelect?>(null) }
    var showGestureAngles by remember { mutableStateOf(false) }
    var pendingActionPanelStyleDirection by remember { mutableStateOf<TriggerDirection?>(null) }
    UDFComponent(component = vm.udfComponent, onEvent = { }) { uiState ->
        if (uiState.showDeleteWarningDialog) {
            MyAlertDialog(
                onDismissRequest = { vm.showDeleteWarningDialog(false) },
                title = stringResource(id = R.string.delete_gesture_button_warning),
                text = when (uiState.gestureButtonSettings.isSideButton) {
                    true -> stringResource(id = R.string.delete_side_gesture_button_warning_desc)
                    else -> stringResource(id = R.string.delete_gesture_button_warning_desc)
                },
                onConfirmClick = { vm.deleteGestureButton() }
            )
        }
        if (uiState.showCopyAnotherSideGestureButtonDialog) {
            MyAlertDialog(
                onDismissRequest = { vm.showCopyAnotherSideGestureButtonDialog(false) },
                title = stringResource(id = R.string.copy_another_side_button),
                text = stringResource(R.string.copy_another_side_button_tips),
                onConfirmClick = { vm.copyAnotherSideGestureButton() }
            )
        }
        if (uiState.colorPickerDialog.first) {
            ColorPickerDialog(
                onDismissRequest = {
                    vm.colorPickerDialog.show(false)
                },
                onColorPicked = { color ->
                    vm.colorPickerDialog.onColorChange(color)
                    vm.colorPickerDialog.confirm()
                },
                initialColor = uiState.colorPickerDialog.second
            )
        }

        Box {
            Column {
                TopBar(
                    onBack = onBack,
                    title = uiState.gestureButton.let {
                        if (it == null) return@let ""
                        when (it.position) {
                            Position.Left -> stringResource(id = R.string.left_gesture_button)
                            Position.Right -> stringResource(id = R.string.right_gesture_button)
                            Position.Bottom -> stringResource(id = R.string.bottom_gesture_button)
                        }
                    },
                    postfixTitle = {
                        if (uiState.gestureButton != null) {
                            Box(
                                modifier = Modifier
                                    .padding(start = IconTextPadding)
                                    .size(MarkColorSize)
                                    .background(
                                        color = when (uiState.gestureButton.isDefault) {
                                            true -> MaterialTheme.colorScheme.primary.copy(alpha = GestureButtonColorAlpha)
                                            else -> Color(uiState.gestureButton.color).copy(alpha = GestureButtonColorAlpha)
                                        },
                                        shape = CircleShape
                                    )
                            )
                        }
                    },
                    actions = {
                        if (uiState.gestureButtonSettings.isSideButton) {
                            IconButton(onClick = { vm.showCopyAnotherSideGestureButtonDialog(true) }) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null
                                )
                            }
                        }
                        if (uiState.gestureButton != null && !uiState.gestureButton.isDefault) {
                            IconButton(onClick = { vm.showDeleteWarningDialog(true) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                )
                val gestureButton = uiState.gestureButton
                if (gestureButton != null) {
                    MyColumn {
                        SectionCard(title = stringResource(id = R.string.slide_action)) {
                            val navToActionSelect: (TriggerDirection) -> Unit = { direction ->
                                val actionSelect = ActionSelect(
                                    gestureButtonId = gestureButton.id,
                                    position = gestureButton.position,
                                    direction = direction,
                                    isLongSlide = false,
                                    isSideButton = uiState.gestureButtonSettings.isSideButton
                                )
                                pendingActionSelect = actionSelect
                                showActionSelect = true
                            }
                            MySideGestureSettings(
                                onClick = {
                                    navToActionSelect(Center)
                                },
                                gestureButton = gestureButton,
                                direction = Center,
                                isLongSlide = false,
                                secondaryText = gestureButton.slideActions.center.actionTextCompose()
                            )
                            MySideGestureSettings(
                                onClick = {
                                    navToActionSelect(Up)
                                },
                                gestureButton = gestureButton,
                                direction = Up,
                                isLongSlide = false,
                                secondaryText = gestureButton.slideActions.up.actionTextCompose()
                            )
                            MySideGestureSettings(
                                onClick = {
                                    navToActionSelect(Down)
                                },
                                gestureButton = gestureButton,
                                direction = Down,
                                isLongSlide = false,
                                secondaryText = gestureButton.slideActions.down.actionTextCompose()
                            )
                            MySideGestureSettings(
                                onClick = {
                                    navToActionSelect(Up2)
                                },
                                gestureButton = gestureButton,
                                direction = Up2,
                                isLongSlide = false,
                                secondaryText = gestureButton.slideActions.up2.actionTextCompose()
                            )
                            MySideGestureSettings(
                                onClick = {
                                    navToActionSelect(Down2)
                                },
                                gestureButton = gestureButton,
                                direction = Down2,
                                isLongSlide = false,
                                secondaryText = gestureButton.slideActions.down2.actionTextCompose()
                            )
                        }

                        SectionCard(
                            modifier = Modifier.padding(top = SectionPadding),
                            title = stringResource(id = R.string.long_slide_action)
                        ) {
                            val navToActionSelect: (TriggerDirection) -> Unit = { direction ->
                                val actionSelect = ActionSelect(
                                    gestureButtonId = gestureButton.id,
                                    position = gestureButton.position,
                                    direction = direction,
                                    isLongSlide = true,
                                    isSideButton = uiState.gestureButtonSettings.isSideButton
                                )
                                pendingActionSelect = actionSelect
                                showActionSelect = true
                            }
                            fun styleTrailing(direction: TriggerDirection): @Composable () -> Unit = {
                                StyleTrailingDropdown(
                                    currentStyle = gestureButton.longSlideActionPanelStyles.styleBy(direction),
                                    onStyleSelected = { style ->
                                        vm.updateLongSlideActionPanelStyle(direction, style)
                                    }
                                )
                            }
                            MySideGestureSettings(
                                onClick = { navToActionSelect(Center) },
                                gestureButton = gestureButton,
                                direction = Center,
                                isLongSlide = true,
                                secondaryText = gestureButton.longSlideActions.center.actionTextCompose(),
                                trailing = styleTrailing(Center)
                            )
                            MySideGestureSettings(
                                onClick = { navToActionSelect(Up) },
                                gestureButton = gestureButton,
                                direction = Up,
                                isLongSlide = true,
                                secondaryText = gestureButton.longSlideActions.up.actionTextCompose(),
                                trailing = styleTrailing(Up)
                            )
                            MySideGestureSettings(
                                onClick = { navToActionSelect(Down) },
                                gestureButton = gestureButton,
                                direction = Down,
                                isLongSlide = true,
                                secondaryText = gestureButton.longSlideActions.down.actionTextCompose(),
                                trailing = styleTrailing(Down)
                            )
                            MySideGestureSettings(
                                onClick = { navToActionSelect(Up2) },
                                gestureButton = gestureButton,
                                direction = Up2,
                                isLongSlide = true,
                                secondaryText = gestureButton.longSlideActions.up2.actionTextCompose(),
                                trailing = styleTrailing(Up2)
                            )
                            MySideGestureSettings(
                                onClick = { navToActionSelect(Down2) },
                                gestureButton = gestureButton,
                                direction = Down2,
                                isLongSlide = true,
                                secondaryText = gestureButton.longSlideActions.down2.actionTextCompose(),
                                trailing = styleTrailing(Down2)
                            )
                        }

                        SectionCard(
                            modifier = Modifier.padding(top = SectionPadding),
                            title = stringResource(id = R.string.tap_and_long_press_action)
                        ) {
                            val navToTapActionSelect: (TriggerDirection) -> Unit = { direction ->
                                val actionSelect = ActionSelect(
                                    gestureButtonId = gestureButton.id,
                                    position = gestureButton.position,
                                    direction = direction,
                                    isLongSlide = false,
                                    isSideButton = uiState.gestureButtonSettings.isSideButton,
                                    isTap = true
                                )
                                pendingActionSelect = actionSelect
                                showActionSelect = true
                            }
                            MySideGestureSettings(
                                onClick = {
                                    navToTapActionSelect(Center)
                                },
                                gestureButton = gestureButton,
                                direction = Center,
                                isLongSlide = false,
                                secondaryText = gestureButton.tapActions.center.actionTextCompose(),
                                text = stringResource(id = R.string.tap_action)
                            )
                            MySideGestureSettings(
                                onClick = {
                                    val actionSelect = ActionSelect(
                                        gestureButtonId = gestureButton.id,
                                        position = gestureButton.position,
                                        direction = Center2,
                                        isLongSlide = false,
                                        isSideButton = uiState.gestureButtonSettings.isSideButton
                                    )
                                    pendingActionSelect = actionSelect
                                    showActionSelect = true
                                },
                                gestureButton = gestureButton,
                                direction = Center2,
                                isLongSlide = false,
                                secondaryText = gestureButton.slideActions.center2.actionTextCompose(),
                                text = stringResource(id = R.string.long_press)
                            )
                        }

                        SectionCard(modifier = Modifier.padding(top = SectionPaddingNoTitle)) {
                            TextActionButton(
                                onClick = { showGestureAngles = true },
                                text = stringResource(id = R.string.gesture_angles),
                                secondaryText = stringResource(id = R.string.gesture_button_angles_hint)
                            )
                            MyTextSlider(
                                value = gestureButton.width.toFloat(),
                                onValueChange = { vm.onGestureButtonWidthChange(it) },
                                onValueChangeFinished = { vm.onGestureButtonAdjustFinish() },
                                text = stringResource(id = R.string.gesture_button_width),
                                sliderValueHint = stringResource(id = R.string.small) to stringResource(id = R.string.large),
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
                            if (uiState.gestureButtonSettings.isSideButton) {
                                LabeledSwitch(
                                    onCheckedChange = { vm.onGestureButtonAlignChange(it) },
                                    checked = uiState.alignRegion,
                                    text = stringResource(id = R.string.gesture_button_align),
                                    secondaryText = stringResource(id = R.string.gesture_button_align_hint)
                                )
                            }
                            if (uiState.canShowExcludeSystemGestureRects) {
                                LabeledSwitch(
                                    onCheckedChange = { vm.onExcludeSystemGestureRectsChange(it) },
                                    checked = gestureButton.excludeSystemGestureRects,
                                    text = stringResource(id = R.string.intercept_system_back_gesture),
                                    secondaryText = stringResource(id = R.string.intercept_system_back_gesture_hint)
                                )
                                LabeledSwitch(
                                    enabled = gestureButton.excludeSystemGestureRects,
                                    onCheckedChange = { vm.onLimitMaxExcludeSystemGestureLengthChange(it) },
                                    checked = gestureButton.limitMaxExcludeSystemGestureLength,
                                    text = stringResource(id = R.string.limit_max_intercept_length),
                                    secondaryText = stringResource(id = R.string.limit_max_intercept_length_hint)
                                )
                            }
                        }
                        if (!gestureButton.isDefault) {
                            SectionCard(modifier = Modifier.padding(top = SectionPaddingNoTitle)) {
                                TextActionButton(
                                    onClick = { vm.colorPickerDialog.show(true) },
                                    text = stringResource(id = R.string.gesture_button_color),
                                    prefix = {
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .background(
                                                    color = Color(gestureButton.color),
                                                    shape = CircleShape
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = MaterialTheme.colorScheme.outlineVariant,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            val colorScheme = MaterialTheme.colorScheme
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        uiState.gestureButtons.fastForEach { button ->
                            val bounds = button.bounds()
                            val color = when (button.isDefault) {
                                true -> colorScheme.primary
                                else -> Color(
                                    red = button.color.red,
                                    green = button.color.green,
                                    blue = button.color.blue
                                )
                            }
                            val highlight = uiState.isGestureButtonAdjusting &&
                                    button.id == uiState.gestureButton?.id
                            drawRect(
                                color = when (highlight) {
                                    true -> color
                                    else -> color.copy(alpha = GestureButtonColorAlpha)
                                },
                                topLeft = bounds.topLeft,
                                size = bounds.size
                            )
                        }
                    }
            )
        }

        if (showActionSelect && pendingActionSelect != null) {
            ModalBottomSheet(
                onDismissRequest = { showActionSelect = false },
                sheetState = rememberModalBottomSheetState(),
                sheetGesturesEnabled = false
            ) {
                BottomSheetNestedContent {
                    ActionSelectContent(
                        onDismiss = { showActionSelect = false },
                        actionSelect = pendingActionSelect!!
                    )
                }
            }
        }
        if (showGestureAngles) {
            val currentGestureButton = uiState.gestureButton
            if (currentGestureButton != null) {
                ModalBottomSheet(
                    onDismissRequest = { showGestureAngles = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ) {
                    BottomSheetNestedContent {
                        GestureButtonAngleContent(
                            gestureButton = currentGestureButton,
                            onDismiss = { showGestureAngles = false },
                            onSave = { angle ->
                                vm.updateGestureButtonAngle(angle)
                                showGestureAngles = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MySideGestureSettings(
    onClick: () -> Unit,
    gestureButton: GestureButton,
    direction: TriggerDirection,
    isLongSlide: Boolean,
    secondaryText: String,
    text: String? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    TextActionButton(
        onClick = onClick,
        text = text ?: when (direction) {
            Center -> when (gestureButton.position) {
                Position.Left -> stringResource(id = R.string.slide_to_right)
                Position.Right -> stringResource(id = R.string.slide_to_left)
                Position.Bottom -> stringResource(id = R.string.slide_to_top)
            }
            Up -> when (gestureButton.position) {
                Position.Left -> stringResource(id = R.string.slide_to_top_right)
                Position.Right -> stringResource(id = R.string.slide_to_top_left)
                Position.Bottom -> stringResource(id = R.string.slide_to_top_left)
            }
            Down -> when (gestureButton.position) {
                Position.Left -> stringResource(id = R.string.slide_to_bottom_right)
                Position.Right -> stringResource(id = R.string.slide_to_bottom_left)
                Position.Bottom -> stringResource(id = R.string.slide_to_top_right)
            }
            Center2 -> stringResource(R.string.long_press)
            Up2 -> when (gestureButton.position) {
                Position.Left, Position.Right -> stringResource(id = R.string.slide_to_top)
                Position.Bottom -> stringResource(id = R.string.slide_to_left)
            }
            Down2 -> when (gestureButton.position) {
                Position.Left, Position.Right -> stringResource(id = R.string.slide_to_bottom)
                Position.Bottom -> stringResource(id = R.string.slide_to_right)
            }
        },
        secondaryText = if (secondaryText.isNotEmpty()) secondaryText
            else stringResource(id = R.string.action_none),
        secondaryTextColor = MaterialTheme.colorScheme.primary,
        trailing = trailing,
        prefix = {
            val imageVector = when (direction) {
                Center2 -> Icons.Default.Adjust
                else -> Icons.AutoMirrored.Filled.ArrowForward
            }
            Icon(
                modifier = Modifier
                    .graphicsLayer {
                        val position = gestureButton.position
                        rotationZ = when (direction) {
                            Up -> when (position) {
                                Position.Left -> -45f
                                Position.Right -> -135f
                                Position.Bottom -> -135f
                            }
                            Center -> when (position) {
                                Position.Left -> 0f
                                Position.Right -> 180f
                                Position.Bottom -> -90f
                            }
                            Down -> when (position) {
                                Position.Left -> 45f
                                Position.Right -> 135f
                                Position.Bottom -> -45f
                            }
                            Up2 -> when (position) {
                                Position.Left, Position.Right -> -90f
                                Position.Bottom -> -180f
                            }
                            Center2 -> 0f
                            Down2 -> when (position) {
                                Position.Left, Position.Right -> 90f
                                Position.Bottom -> 0f
                            }
                        }
                    }
                    .size(20.dp)
                    .background(
                        color = when (isLongSlide) {
                            true -> MaterialTheme.colorScheme.outlineVariant
                            else -> MaterialTheme.colorScheme.surface
                        },
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = CircleShape
                    ),
                imageVector = imageVector,
                contentDescription = null,
                tint = LocalContentColor.current
            )
        }
    )
}

@Composable
private fun StyleTrailingDropdown(
    currentStyle: ActionPanelStyles,
    onStyleSelected: (ActionPanelStyles) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = actionPanelStyleText(currentStyle),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                onClick = {
                    onStyleSelected(ActionPanelStyles.arc())
                    expanded = false
                },
                text = { Text(stringResource(R.string.action_panel_style_arc)) }
            )
            DropdownMenuItem(
                onClick = {
                    onStyleSelected(ActionPanelStyles.grid())
                    expanded = false
                },
                text = { Text(stringResource(R.string.action_panel_style_grid)) }
            )
        }
    }
}

@Composable
private fun actionPanelStyleText(style: ActionPanelStyles): String {
    return when (style.type) {
        ActionPanelStyles.TYPE_GRID -> stringResource(R.string.action_panel_style_grid)
        else -> stringResource(R.string.action_panel_style_arc)
    }
}
