package hunoia.luno.ui.settings.gesture.icon
import hunoia.luno.ui.theme.*

import hunoia.luno.ui.settings.gesture.icon.IconResizeUiEvent
import hunoia.luno.ui.settings.gesture.icon.IconResizeUiState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.component.UDFComponent
import com.aaron.compose.component.UiBaseEvent
import hunoia.luno.R
import hunoia.luno.bridge.feedback.showToast
import hunoia.luno.ui.component.color.ColorPickerBottomSheet
import hunoia.luno.ui.component.color.ColorSelection
import hunoia.luno.ui.theme.resolveColor
import hunoia.luno.config.model.ThemeColorKey
import hunoia.luno.ui.component.MyAlertDialog



@Composable
fun IconResizeContent(
    onDismiss: () -> Unit,
    ids: List<String>,
    vm: IconResizeVM = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return IconResizeVM(ids) as T
            }
        }
    )
) {
    val defaultBgColor = MaterialTheme.colorScheme.primary
    UDFComponent(
        component = vm.udfComponent,
        onEvent = {},
        onBaseEvent = { baseEvent ->
            when (baseEvent) {
                is UiBaseEvent.Finish -> { onDismiss(); true }
                is UiBaseEvent.ResToast -> { showToast(baseEvent.res); true }
                is UiBaseEvent.StringToast -> { showToast(baseEvent.text); true }
                else -> false
            }
        }
    ) { uiState ->
        if (uiState.showResetWarningDialog) {
            MyAlertDialog(
                onDismissRequest = { vm.showResetWarningDialog(false) },
                onConfirmClick = { vm.reset() },
                title = stringResource(id = R.string.reset_default_settings_warning),
                text = stringResource(id = R.string.reset_icon_settings)
            )
        }
        if (uiState.showColorPickerDialog) {
            val scheme = MaterialTheme.colorScheme
            val themeColors = remember(scheme) {
                ThemeColorKey.entries.associateWith { it.resolveColor(scheme) }
            }
            ColorPickerBottomSheet(
                onDismissRequest = { vm.showColorPickerDialog(false) },
                onColorSelected = { selection ->
                    when (selection) {
                        is ColorSelection.Custom -> vm.onBgColorChange(selection.color)
                        is ColorSelection.Theme -> themeColors[selection.key]?.let { vm.onBgColorChange(it) }
                    }
                    vm.showColorPickerDialog(false)
                },
                initialColor = uiState.selectedBgColor?.color ?: defaultBgColor,
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing4, vertical = Spacing4),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.icon_resize), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { vm.showResetWarningDialog(true) }) {
                        Icon(imageVector = Icons.Default.Restore, contentDescription = null)
                    }
                    IconButton(onClick = { vm.done() }) {
                        Icon(imageVector = Icons.Default.Done, contentDescription = null)
                    }
                }

                IconThumbnailStrip(
                    ids = uiState.ids,
                    icons = uiState.icons,
                    scaleFactors = uiState.scaleFactors,
                    bgColors = uiState.bgColors,
                    onSelectedIdChange = { vm.onSelectedIdChange(it) },
                )
            }

            IconPreviewGrid(
                modifier = Modifier.align(Alignment.Center),
                selectedId = uiState.selectedId,
                selectedBgColor = uiState.selectedBgColor,
                scaleFactors = uiState.scaleFactors,
                icons = uiState.icons,
                onScaleChange = { vm.onScaleChange(it) },
                onShowColorPicker = { vm.showColorPickerDialog(true) },
                onBgColorEnabled = { enabled, color -> vm.onBgColorEnabled(enabled, color) },
                defaultBgColor = defaultBgColor,
            )
        }
    }
}