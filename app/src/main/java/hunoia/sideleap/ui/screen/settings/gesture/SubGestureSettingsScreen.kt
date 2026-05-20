package hunoia.sideleap.ui.screen.settings.gesture

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import hunoia.sideleap.R
import hunoia.sideleap.action.display.actionText
import hunoia.sideleap.action.payload.SubGestureActionData
import hunoia.sideleap.gesture.SubGestureDirection
import hunoia.sideleap.settings.defaults.SettingsUiDefaults.GestureButtonColorAlpha
import hunoia.sideleap.settings.model.SubGesture
import hunoia.sideleap.ui.component.BottomSheetNestedContent
import hunoia.sideleap.ui.component.ColorPickerDialog
import hunoia.sideleap.ui.component.MyAlertDialog
import hunoia.sideleap.ui.component.MyColumn
import hunoia.sideleap.ui.component.SectionCard
import hunoia.sideleap.ui.component.TextActionButton
import hunoia.sideleap.ui.component.TopBar
import hunoia.sideleap.ui.screen.settings.gesture.SubGestureSettingsVM.UiEvent
import hunoia.sideleap.ui.theme.IconTextPadding
import hunoia.sideleap.ui.theme.MarkColorSize
import hunoia.sideleap.ui.theme.SectionPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubGestureSettingsScreen(
    onBack: () -> Unit,
    onNavToSubGestureActionSelect: (subGestureId: String, direction: SubGestureDirection) -> Unit,
    vm: SubGestureSettingsVM = viewModel()
) {
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showGestureAngles by remember { mutableStateOf(false) }
    UDFComponent(component = vm.udfComponent, onEvent = { }) { uiState ->
        if (uiState.showDeleteWarningDialog) {
            MyAlertDialog(
                onDismissRequest = { vm.showDeleteWarningDialog(false) },
                title = stringResource(id = R.string.delete_sub_gesture_warning),
                text = stringResource(id = R.string.delete_sub_gesture_warning_desc),
                onConfirmClick = { vm.deleteSubGesture() }
            )
        }

        val gesture = uiState.subGesture ?: return@UDFComponent

        if (showEditNameDialog) {
            AlertDialog(
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
            ModalBottomSheet(
                onDismissRequest = { showGestureAngles = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                BottomSheetNestedContent {
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
                    IconButton(onClick = { vm.showDeleteWarningDialog(true) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null
                        )
                    }
                }
            )

            MyColumn {
                SectionCard(title = "方向动作") {
                    val directions = listOf(
                        SubGestureDirection.Up, SubGestureDirection.Down,
                        SubGestureDirection.Left, SubGestureDirection.Right,
                        SubGestureDirection.UpRight, SubGestureDirection.DownRight,
                        SubGestureDirection.DownLeft, SubGestureDirection.UpLeft
                    )
                    directions.fastForEach { direction ->
                        val action = gesture.actionFor(direction)
                        val text = actionDisplayText(action, uiState.allSubGestures)
                        TextActionButton(
                            onClick = { onNavToSubGestureActionSelect(gesture.id, direction) },
                            text = direction.displayName,
                            secondaryText = text.ifEmpty { stringResource(id = R.string.action_none) }
                        )
                    }
                }

                SectionCard(
                    modifier = Modifier.padding(top = SectionPadding),
                    title = stringResource(id = R.string.sub_gesture_angles)
                ) {
                    TextActionButton(
                        onClick = { showGestureAngles = true },
                        text = stringResource(id = R.string.sub_gesture_angles)
                    )
                }

                SectionCard(modifier = Modifier.padding(top = SectionPadding)) {
                    TextActionButton(
                        onClick = { vm.colorPickerDialog.show(true) },
                        text = stringResource(id = R.string.gesture_button_color),
                        prefix = {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(
                                        color = Color(gesture.color),
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

private val SubGestureDirection.displayName: String get() = when (this) {
    SubGestureDirection.Up -> "上"
    SubGestureDirection.Down -> "下"
    SubGestureDirection.Left -> "左"
    SubGestureDirection.Right -> "右"
    SubGestureDirection.UpRight -> "右上"
    SubGestureDirection.DownRight -> "右下"
    SubGestureDirection.DownLeft -> "左下"
    SubGestureDirection.UpLeft -> "左上"
}

@Composable
private fun actionDisplayText(action: hunoia.sideleap.action.Action?, allSubGestures: List<SubGesture>?): String {
    if (action == null) return ""
    if (action.value != hunoia.sideleap.action.GlobalActions.SUB_GESTURE) {
        return actionText(action)
    }
    val data = remember(action.data) {
        try {
            kotlinx.serialization.json.Json.decodeFromString<SubGestureActionData>(action.data)
        } catch (_: Exception) { null }
    } ?: return stringResource(id = R.string.action_sub_gesture)
    val name = allSubGestures?.find { it.id == data.id }?.name
    return name ?: stringResource(id = R.string.action_sub_gesture)
}
