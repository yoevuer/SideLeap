package hunoia.luno.ui.screen.actionselect

import android.app.Activity
import android.content.Intent
import android.content.Intent.ShortcutIconResource
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.component.UDFComponent
import com.aaron.compose.component.UiBaseEvent
import hunoia.luno.R
import hunoia.luno.action.Action
import hunoia.luno.ui.component.OptimizedBottomSheet
import hunoia.luno.ui.component.TopBar
import hunoia.luno.ui.component.OptimizedScrollState
import hunoia.luno.launcher.LauncherFacade
import hunoia.luno.launcher.model.LauncherInfo
import hunoia.luno.ui.navigation.ActionSelect
import hunoia.luno.ui.screen.settings.gesture.IconResizeContent
import hunoia.luno.ui.component.ActionSettingsDialog
import hunoia.luno.ui.component.MySnackbarHost
import hunoia.luno.ui.screen.actionselect.ActionSelectVM.UiEvent
import hunoia.luno.system.feedback.showToast
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.os.Build
import hunoia.luno.ui.permission.rememberGetInstalledAppsPermissionState
import hunoia.luno.ui.theme.ScrollBottomPadding


/**
 * @author aaronzzxup@gmail.com
 * @since 2024/12/2
 */

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ActionSelectContent(
    onDismiss: () -> Unit,
    actionSelect: ActionSelect,
    vm: ActionSelectVM = viewModel(
        key = "action_select_${actionSelect.gestureButtonId}_${actionSelect.direction}_${actionSelect.isLongSlide}_${actionSelect.isTap}",
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ActionSelectVM(actionSelect) as T
            }
        }
    )
) {
    var showIconResize by remember { mutableStateOf(false) }
    var iconResizeIds by remember { mutableStateOf<List<String>>(emptyList()) }

    UDFComponent(
        component = vm.udfComponent,
        onEvent = { event ->
            when (event) {
                is UiEvent.GotoIconResize -> {
                    iconResizeIds = event.iconResize.ids
                    showIconResize = true
                }
            }
        },
        onBaseEvent = { baseEvent ->
            when (baseEvent) {
                is UiBaseEvent.Finish -> { onDismiss(); true }
                is UiBaseEvent.ResToast -> { showToast(baseEvent.res); true }
                is UiBaseEvent.StringToast -> { showToast(baseEvent.text); true }
                else -> false
            }
        }
    ) { uiState ->
        if (uiState.actionSettingsDialog.show) {
            ActionSettingsDialog(
                onDismissRequest = { vm.actionSettingsDialog.show(false) },
                action = uiState.actionSettingsDialog.action,
                onActionDataChanged = { vm.select(uiState.actionSettingsDialog.action.copy(data = it), true) }
            )
        }

        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopBar(
                    onBack = onDismiss,
                    title = uiState.title,
                    actions = {
                        if (!uiState.selectSingle) {
                            IconButton(onClick = { vm.done() }) {
                                Icon(imageVector = Icons.Default.Done, contentDescription = null)
                            }
                        }
                    }
                )

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
                val context = LocalContext.current
                var currentLauncherInfo: LauncherInfo? by remember { mutableStateOf(null) }
                val shortcutLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    coroutineScope.launch {
                        val launcherInfo = currentLauncherInfo
                        if (result.resultCode == Activity.RESULT_OK && launcherInfo != null) {
                            val bitmap = result.data?.getParcelableExtra(shortcutIconExtraKey(), Bitmap::class.java)
                            val shortcutIconRes = result.data?.getParcelableExtra(shortcutIconResourceExtraKey(), ShortcutIconResource::class.java)
                            val intent = result.data?.getParcelableExtra(shortcutIntentExtraKey(), Intent::class.java)?.toUri(Intent.URI_INTENT_SCHEME)
                            val label = result.data?.getStringExtra(shortcutNameExtraKey()).orEmpty()
                            val iconRes = if (shortcutIconRes != null) {
                                withContext(Dispatchers.IO) {
                                    LauncherFacade.resolveShortcutIconResourceId(context, shortcutIconRes)
                                }
                            } else 0
                            val shortcutInfo = LauncherInfo.ShortcutInfo(
                                packageName = launcherInfo.packageName, className = launcherInfo.className,
                                intents = intent?.let { listOf(it) } ?: emptyList(), label = label,
                                iconRes = iconRes, iconPath = null, iconBitmap = bitmap
                            )
                            vm.addNewShortcut(launcherInfo, shortcutInfo)
                            if (uiState.longPressTargetIndex != null) {
                                vm.selectLongPressAction(shortcutInfo)
                            } else if (uiState.selectedRecord.size < uiState.maxSelectCount) {
                                vm.select(shortcutInfo, true)
                            }
                        }
                        currentLauncherInfo = null
                    }
                }
                ActionPage(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = ScrollBottomPadding),
                    actions = uiState.actions,
                    subGestures = uiState.subGestures,
                    appInfos = uiState.apps,
                    createShortcuts = uiState.createShortcuts,
                    launchShortcuts = uiState.launchShortcuts,
                    selectedRecord = uiState.selectedRecord,
                    maxSelectCount = uiState.maxSelectCount,
                    longPressTargetIndex = uiState.longPressTargetIndex,
                    selectSingle = uiState.selectSingle,
                    snackbarHostState = snackbarHostState,
                    permissionState = permissionState,
                    onSelect = { action, selected -> vm.select(action, selected) },
                    onSelectLongPress = { obj -> vm.selectLongPressAction(obj) },
                    onSetLongPress = { index -> vm.startSetLongPressAction(index) },
                    onClearLongPress = { index -> vm.clearLongPressAction(index) },
                    onCancelLongPress = { vm.cancelSetLongPressAction() },
                    onMoveSelected = { from, to -> vm.moveSelectedAction(from, to) },
                    onSettingsClick = { action -> vm.actionSettingsDialog.show(true, action) },
                    onSelectApp = { appInfo, selected -> vm.select(appInfo, selected) },
                    onSelectShortcut = { shortcutInfo, selected -> vm.select(shortcutInfo, selected) },
                    onAppLongClick = { appInfo -> vm.toggleMiniWindow(appInfo) },
                    onShortcutClick = { launcherInfo ->
                        try {
                            currentLauncherInfo = launcherInfo
                            shortcutLauncher.launch(Intent().apply { setClassName(launcherInfo.packageName, launcherInfo.className) })
                        } catch (ignored: Exception) { currentLauncherInfo = null }
                    }
                )
            }

            if (showIconResize) {
                OptimizedBottomSheet(
                    onDismissRequest = { showIconResize = false }
                ) {
                    IconResizeContent(
                        onDismiss = { showIconResize = false },
                        ids = iconResizeIds
                    )
                }
            }
        }
    }
}


@Suppress("DEPRECATION")
private fun shortcutIconExtraKey(): String = Intent.EXTRA_SHORTCUT_ICON

@Suppress("DEPRECATION")
private fun shortcutIconResourceExtraKey(): String = Intent.EXTRA_SHORTCUT_ICON_RESOURCE

@Suppress("DEPRECATION")
private fun shortcutIntentExtraKey(): String = Intent.EXTRA_SHORTCUT_INTENT

@Suppress("DEPRECATION")
private fun shortcutNameExtraKey(): String = Intent.EXTRA_SHORTCUT_NAME
