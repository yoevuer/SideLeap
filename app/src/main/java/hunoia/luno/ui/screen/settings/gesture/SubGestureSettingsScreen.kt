package hunoia.luno.ui.screen.settings.gesture

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
import hunoia.luno.action.display.actionText
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
import hunoia.luno.ui.component.SectionCard
import hunoia.luno.ui.component.TextActionButton
import hunoia.luno.ui.component.TopBar
import hunoia.luno.ui.screen.settings.gesture.SubGestureSettingsVM.UiEvent
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

            MyColumn {
                SectionCard(title = stringResource(id = R.string.direction_actions)) {
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
                            onClick = {
                                onNavToSubGestureActionSelect(SubGestureActionSelect(gesture.id, direction))
                            },
                            text = stringResource(id = direction.displayNameRes),
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

@Composable
private fun actionDisplayText(action: hunoia.luno.action.Action?, allSubGestures: List<SubGesture>?): String {
    if (action == null) return ""
    if (action.value != hunoia.luno.action.GlobalActions.SUB_GESTURE) {
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
