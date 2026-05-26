package hunoia.sideleap.ui.screen.settings.gesture

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.component.UDFComponent
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import hunoia.sideleap.R
import hunoia.sideleap.action.Action
import hunoia.sideleap.action.definition.ActionCatalog
import hunoia.sideleap.gesture.SubGestureDirection
import hunoia.sideleap.ui.component.ActionSettingsDialog
import hunoia.sideleap.ui.component.TopBar
import hunoia.sideleap.ui.screen.actionselect.ActionPage
import hunoia.sideleap.ui.screen.actionselect.ActionSelectVM.UiState.SelectedRecord
import hunoia.sideleap.ui.theme.ScrollBottomPadding
import hunoia.sideleap.ui.permission.rememberGetInstalledAppsPermissionState

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SubGestureActionSelectContent(
    subGestureId: String,
    direction: SubGestureDirection,
    onDismiss: () -> Unit,
    vm: SubGestureActionSelectVM = viewModel(
        key = "sub_gesture_action_select_${subGestureId}_${direction.name}",
        factory = SubGestureActionSelectVM.Factory(subGestureId, direction)
    )
) {
    val snackbarHostState = remember { SnackbarHostState() }

    UDFComponent(
        component = vm.udfComponent,
        onEvent = { },
        onBaseEvent = { baseEvent ->
            when (baseEvent) {
                is com.aaron.compose.component.UiBaseEvent.Finish -> { onDismiss(); true }
                else -> false
            }
        }
    ) { uiState ->
        val permissionState = rememberGetInstalledAppsPermissionState { granted ->
            if (granted) {
                vm.updateAppInfos()
                vm.updateShortcutInfos()
            }
        }
        LaunchedEffect(Unit) {
            if (permissionState.status.isGranted) {
                vm.updateAppInfos()
                vm.updateShortcutInfos()
            }
        }

        if (uiState.actionSettingsDialog.show) {
            ActionSettingsDialog(
                onDismissRequest = { vm.actionSettingsDialog.show(false) },
                action = uiState.actionSettingsDialog.action,
                onActionDataChanged = { vm.select(uiState.actionSettingsDialog.action.copy(data = it), true) }
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopBar(
                    onBack = onDismiss,
                    title = direction.displayName
                )

                ActionPage(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = ScrollBottomPadding),
                    subGestures = uiState.subGestures,
                    actions = uiState.actions,
                    appInfos = uiState.apps,
                    createShortcuts = uiState.createShortcuts,
                    launchShortcuts = uiState.launchShortcuts,
                    selectedRecord = uiState.selectedRecord,
                    maxSelectCount = 1,
                    longPressTargetIndex = null,
                    selectSingle = true,
                    snackbarHostState = snackbarHostState,
                    permissionState = permissionState,
                    onSelect = { action, selected -> vm.select(action, selected) },
                    onSelectLongPress = {},
                    onSetLongPress = {},
                    onClearLongPress = {},
                    onCancelLongPress = {},
                    onMoveSelected = { _, _ -> },
                    onSettingsClick = { action -> vm.showActionSettingsDialog(action) },
                    onSelectApp = { appInfo, selected -> vm.select(appInfo, selected) },
                    onSelectShortcut = { shortcutInfo, selected -> vm.select(shortcutInfo, selected) },
                    onAppLongClick = { appInfo -> vm.toggleMiniWindow(appInfo) },
                    onShortcutClick = { }
                )
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
