package hunoia.luno.ui.component
import hunoia.luno.ui.theme.*

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
import androidx.compose.material3.Slider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.onSingleClick
import com.aaron.compose.utils.SystemFontScaleHandler
import hunoia.luno.R
import hunoia.luno.action.GlobalActions
import hunoia.luno.settings.defaults.SettingsUiDefaults.DimAlpha
import hunoia.luno.action.Action
import hunoia.luno.ui.action.actionText
import hunoia.luno.core.event.Events
import hunoia.luno.system.event.WallpaperChangedEvent
import hunoia.luno.settings.model.ThemeColorKey
import hunoia.luno.ui.ext.resolveColor
import hunoia.luno.ui.theme.DialogHexTextWidth
import hunoia.luno.ui.theme.DialogTitlePadding
import hunoia.luno.ui.theme.ItemPadding
import hunoia.luno.ui.theme.SubMinInteractiveSize
import hunoia.luno.ui.ext.displayNameRes
import hunoia.luno.core.DensityProvider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import hunoia.luno.ui.dialog.GotoBottomSettingsContent
import hunoia.luno.ui.dialog.HideGestureButtonSettingsContent
import hunoia.luno.ui.dialog.MoveScreenSettingsContent
import hunoia.luno.ui.dialog.VolumeScrubSettingsContent
import hunoia.luno.ui.dialog.ActivitySettingsContent
import hunoia.luno.ui.dialog.UrlSettingsContent
import hunoia.luno.ui.dialog.PreviousAppSettingsContent
import hunoia.luno.ui.dialog.ShellCommandSettingsContent
import hunoia.luno.ui.dialog.VirtualMouseActionSettingsContent

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
    val context = LocalContext.current
    var alpha by remember { mutableStateOf(initialColor.alpha) }
    var hexInput by remember { mutableStateOf("") }
    var hexError by remember { mutableStateOf(false) }
    val colorController = rememberColorPickerController()
    LaunchedEffect(initialColor) {
        alpha = initialColor.alpha
        colorController.selectByColor(initialColor, false)
    }
    val resolvedColor by remember(colorController, alpha) {
        derivedStateOf { colorController.selectedColor.value.copy(alpha = alpha) }
    }
    val hexColor by remember(resolvedColor) {
        derivedStateOf {
            val nativeColor = resolvedColor.toArgb()
            val red = android.graphics.Color.red(nativeColor)
            val green = android.graphics.Color.green(nativeColor)
            val blue = android.graphics.Color.blue(nativeColor)
            val a = String.format("%02X", (alpha * 255).toInt())
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

                Slider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ItemPadding),
                    value = alpha,
                    onValueChange = { alpha = it },
                    valueRange = 0f..1f
                )
                Text(
                    text = stringResource(id = R.string.color_picker_alpha_label, (alpha * 100).toInt()),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium
                )

                Row(
                    modifier = Modifier.padding(ItemPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ItemPadding / 2)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .border(
                                width = Spacing1,
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
                                .background(color = resolvedColor)
                        )
                    }

                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = hexInput,
                        onValueChange = { hexInput = it.filter { c -> c.isDigit() || c in 'a'..'f' || c in 'A'..'F' }.take(8) },
                        prefix = {
                            Text(
                                text = "#",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = DimAlpha)
                            )
                        },
                        placeholder = { Text("$hexColor", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                        isError = hexError,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.labelLarge
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (hexInput.isNotEmpty()) {
                        try {
                            val newColor = Color("#$hexInput".toColorInt())
                            colorController.selectByColor(newColor, true)
                            hexError = false
                        } catch (e: Exception) {
                            hexError = true
                            val toast = android.widget.Toast.makeText(context, R.string.invalid_color_value, android.widget.Toast.LENGTH_SHORT)
                            toast.show()
                            return@TextButton
                        }
                    }
                    onColorPicked(resolvedColor)
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

                GlobalActions.OPEN_APP_ACTIVITY -> {
                    ActivitySettingsContent(
                        action = action,
                        onConfirm = {
                            onActionDataChanged(it)
                            if (autoDismiss) {
                                onDismissRequest()
                            }
                        }
                    )
                }
                GlobalActions.OPEN_URL -> {
                    UrlSettingsContent(
                        action = action,
                        onConfirm = {
                            onActionDataChanged(it)
                            if (autoDismiss) {
                                onDismissRequest()
                            }
                        }
                    )
                }

                GlobalActions.EXECUTE_SHELL_COMMAND -> {
                    ShellCommandSettingsContent(
                        action = action,
                        onConfirm = {
                            onActionDataChanged(it)
                            if (autoDismiss) {
                                onDismissRequest()
                            }
                        }
                    )
                }

                GlobalActions.VIRTUAL_MOUSE -> {
                    VirtualMouseActionSettingsContent(
                        action = action,
                        onConfirm = {
                            onActionDataChanged(it)
                            if (autoDismiss) {
                                onDismissRequest()
                            }
                        }
                    )
                }

                GlobalActions.HIDE_GESTURE_BUTTON -> {
                    HideGestureButtonSettingsContent()
                }

                GlobalActions.VOLUME_SCRUB -> {
                    VolumeScrubSettingsContent()
                }
            }
        },
        confirmButton = {
        },
        dismissButton = {
        }
    )
}

@Composable
fun ThemeColorPickerDialog(
    onDismissRequest: () -> Unit,
    onColorPicked: (ThemeColorKey) -> Unit,
) {
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(id = R.string.color_picker_title))
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ThemeColorKey.entries.size) { index ->
                    val themeKey = ThemeColorKey.entries[index]
                    val resolvedColor = themeKey.resolveColor()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onSingleClick { onColorPicked(themeKey); onDismissRequest() },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(SubMinInteractiveSize)
                                .clip(CircleShape)
                                .background(resolvedColor)
                        )
                        Text(
                            text = stringResource(id = themeKey.displayNameRes),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = Spacing4)
                        )
                    }
                }
            }
        },
        confirmButton = { },
        dismissButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismissRequest) {
                    Text(text = stringResource(id = R.string.cancel))
                }
                TextButton(onClick = { Events.post(WallpaperChangedEvent()) }) {
                    Text(text = stringResource(id = R.string.refresh_theme_colors))
                }
            }
        }
    )
}
