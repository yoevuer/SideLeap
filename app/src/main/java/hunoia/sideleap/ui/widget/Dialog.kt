package hunoia.sideleap.ui.widget

import android.view.Gravity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.onSingleClick
import com.aaron.compose.utils.SystemFontScaleHandler
import hunoia.sideleap.R
import hunoia.sideleap.action.GlobalActions
import hunoia.sideleap.settings.SettingsUiDefaults.DimAlpha
import hunoia.sideleap.action.Action
import hunoia.sideleap.action.display.actionText
import hunoia.sideleap.ui.theme.DialogTitleFontSize
import hunoia.sideleap.ui.theme.DialogTitlePadding
import hunoia.sideleap.ui.theme.ItemPadding
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.ToastUtils
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import hunoia.sideleap.ui.dialog.GotoBottomSettingsContent
import hunoia.sideleap.ui.dialog.MoveScreenSettingsContent
import hunoia.sideleap.ui.dialog.OpenAppOrUrlSettingsContent
import hunoia.sideleap.ui.dialog.PreviousAppSettingsContent

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/12/1
 */

@Composable
fun MyAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit,
    title: String?,
    text: String,
    onCancelClick: (() -> Unit)? = null,
    autoDismissWhenClick: Boolean = true
) {
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismissRequest,
        title = {
            if (!title.isNullOrEmpty()) {
                Text(text = title)
            }
        },
        text = {
            Text(text = text)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (autoDismissWhenClick) {
                        onDismissRequest()
                    }
                    onConfirmClick()
                }
            ) {
                Text(text = stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (autoDismissWhenClick) {
                        onDismissRequest()
                    }
                    onCancelClick?.invoke()
                }
            ) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    )
}

@Composable
fun ColorPickerDialog(
    onDismissRequest: () -> Unit,
    onColorPicked: (Color) -> Unit,
    initialColor: Color = MaterialTheme.colorScheme.primary,
    autoDismiss: Boolean = true
) {
    var showModifyColorValueDialog by remember {
        mutableStateOf(false)
    }
    val colorController = rememberColorPickerController()
    LaunchedEffect(initialColor) {
        colorController.selectByColor(initialColor, false)
    }
    val hexColor by remember(colorController) {
        derivedStateOf {
            val selectedColor = colorController.selectedColor.value
            val nativeColor = selectedColor.toArgb()
            val red = android.graphics.Color.red(nativeColor)
            val green = android.graphics.Color.green(nativeColor)
            val blue = android.graphics.Color.blue(nativeColor)
            val alpha = android.graphics.Color.alpha(nativeColor)
            val a = String.format("%02X", alpha)
            val r = String.format("%02X", red)
            val g = String.format("%02X", green)
            val b = String.format("%02X", blue)
            "$a$r$g$b"
        }
    }
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismissRequest,
        title = { },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
//                    initialColor = initialColor,
                    controller = colorController,
                    onColorChanged = {
                    }
                )

                Row(
                    modifier = Modifier
                        .onSingleClick {
                            showModifyColorValueDialog = true
                        }
                        .padding(ItemPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ItemPadding / 2)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = CircleShape
                            )
                            .clip(CircleShape)
                    ) {
                        LazyVerticalGrid(
                            modifier = Modifier.matchParentSize(),
                            columns = GridCells.Fixed(5),
                            userScrollEnabled = false
                        ) {
                            items(5 * 5) { index ->
                                val color = when (index % 2 == 0) {
                                    true -> Color.LightGray
                                    else -> Color.White
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .background(color = color)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(color = colorController.selectedColor.value)
                        )
                    }

                    SystemFontScaleHandler(false) {
                        Text(
                            modifier = Modifier.width(120.dp),
                            text = "#$hexColor",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onColorPicked(colorController.selectedColor.value)
                    if (autoDismiss) {
                        onDismissRequest()
                    }
                }
            ) {
                Text(text = stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    )
    if (showModifyColorValueDialog) {
        var textFieldValue by remember {
            mutableStateOf(TextFieldValue(hexColor, selection = TextRange(hexColor.length)))
        }
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showModifyColorValueDialog = false },
            title = { },
            text = {
                val focusRequester = remember { FocusRequester() }
                LaunchedEffect(focusRequester) {
                    focusRequester.requestFocus()
                }
                OutlinedTextField(
                    modifier = Modifier.focusRequester(focusRequester),
                    value = textFieldValue,
                    onValueChange = onValueChange@{ newVal ->
                        val maxLength = 8
                        textFieldValue = if (newVal.text.length > maxLength) {
                            newVal.copy(newVal.text.take(maxLength))
                        } else {
                            newVal
                        }

                    },
                    prefix = {
                        Text(
                            text = "#",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = DimAlpha)
                        )
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            val colorValue = textFieldValue.text
                            val newColor = Color("#$colorValue".toColorInt())
                            colorController.selectByColor(newColor, true)
                            showModifyColorValueDialog = false
                        } catch (ignored: Exception) {
                            ToastUtils
                                .getDefaultMaker()
                                .setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, ConvertUtils.dp2px(100f))
                            ToastUtils.showShort(R.string.invalid_color_value)
                        }
                    }
                ) {
                    Text(text = stringResource(id = R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showModifyColorValueDialog = false }
                ) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun ActionSettingsDialog(
    onDismissRequest: () -> Unit,
    action: Action,
    autoDismiss: Boolean = true,
    onActionDataChanged: (String) -> Unit = {}
) {
    val context = LocalContext.current
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = context.actionText(action))
        },
        text = {
            when (action.value) {
                GlobalActions.MOVE_SCREEN -> {
                    MoveScreenSettingsContent()
                }

                GlobalActions.PREVIOUS_APP -> {
                    PreviousAppSettingsContent()
                }

                GlobalActions.GOTO_BOTTOM -> {
                    GotoBottomSettingsContent()
                }

                GlobalActions.OPEN_APP_OR_URL -> {
                    OpenAppOrUrlSettingsContent(
                        action = action,
                        onConfirm = {
                            onActionDataChanged(it)
                            if (autoDismiss) {
                                onDismissRequest()
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
        },
        dismissButton = {
        }
    )
}
