package hunoia.sideleap.ui.screen.actionselect

import android.app.Activity
import android.content.Intent
import android.content.Intent.ShortcutIconResource
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.component.UDFComponent
import hunoia.sideleap.R
import hunoia.sideleap.action.Action
import hunoia.sideleap.action.GlobalActions
import hunoia.sideleap.launcher.model.LauncherInfo
import hunoia.sideleap.launcher.query.LauncherIconQuery
import hunoia.sideleap.ui.navigation.IconResize
import hunoia.sideleap.ui.dialog.OpenAppOrUrlSettingsContent
import hunoia.sideleap.ui.widget.ActionSettingsDialog
import hunoia.sideleap.ui.widget.MySnackbarHost
import hunoia.sideleap.ui.widget.TopBar
import hunoia.sideleap.ui.screen.actionselect.ActionSelectVM.UiEvent
import hunoia.sideleap.ui.theme.ContentPaddingHorizontal
import hunoia.sideleap.ui.theme.ScrollBottomPadding
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.os.Build
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import hunoia.sideleap.ui.permission.rememberGetInstalledAppsPermissionState


/**
 * @author aaronzzxup@gmail.com
 * @since 2024/12/2
 */

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ActionSelectScreen(
    onBack: () -> Unit,
    onNavToIconResize: (IconResize) -> Unit,
    vm: ActionSelectVM = viewModel()
) {
    UDFComponent(
        component = vm.udfComponent,
        onEvent = { event ->
            when (event) {
                is UiEvent.GotoIconResize -> onNavToIconResize(event.iconResize)
            }
        }
    ) { uiState ->
        var showOpenAppOrUrlDialog by remember { mutableStateOf(false) }

        if (uiState.actionSettingsDialog.show) {
            ActionSettingsDialog(
                onDismissRequest = { vm.actionSettingsDialog.show(false) },
                action = uiState.actionSettingsDialog.action,
                onActionDataChanged = { vm.updateActionData(uiState.actionSettingsDialog.action, it) }
            )
        }

        if (showOpenAppOrUrlDialog && !uiState.actionSettingsDialog.show) {
            AlertDialog(
                onDismissRequest = { showOpenAppOrUrlDialog = false },
                containerColor = MaterialTheme.colorScheme.surface,
                title = { Text(stringResource(R.string.open_app_or_url_card_title)) },
                text = {
                    OpenAppOrUrlSettingsContent(
                        action = Action(value = GlobalActions.OPEN_APP_OR_URL, data = ""),
                        onConfirm = { data ->
                            vm.select(Action(value = GlobalActions.OPEN_APP_OR_URL, data = data), true)
                            showOpenAppOrUrlDialog = false
                        }
                    )
                },
                confirmButton = {},
                dismissButton = {}
            )
        }

        val snackbarHostState = remember { SnackbarHostState() }
        val pagerState = rememberPagerState { PAGES.size }
        val coroutineScope = rememberCoroutineScope()
        Scaffold(
            topBar = {
                TopBar(
                    onBack = onBack,
                    title = uiState.title,
                    actions = {
                        if (!uiState.selectSingle) {
                            IconButton(onClick = { vm.done() }) {
                                Icon(imageVector = Icons.Default.Done, contentDescription = null)
                            }
                        }
                    }
                )
            },
            snackbarHost = {
                MySnackbarHost(hostState = snackbarHostState)
            }
        ) { contentPadding ->
            Column(modifier = Modifier.padding(top = contentPadding.calculateTopPadding())) {

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
                HorizontalPager(
                    modifier = Modifier.fillMaxSize(),
                    state = pagerState
                ) { page ->
                    when (page) {
                        PAGE_UNIFIED -> {
                            val context = LocalContext.current
                            var currentLauncherInfo: LauncherInfo? by remember { mutableStateOf(null) }
                            val shortcutLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                                coroutineScope.launch {
                                    val launcherInfo = currentLauncherInfo
                                    if (result.resultCode == Activity.RESULT_OK && launcherInfo != null) {
                                        val bitmap = result.data?.shortcutParcelableExtraCompat(shortcutIconExtraKey(), Bitmap::class.java)
                                        val shortcutIconRes = result.data?.shortcutParcelableExtraCompat(shortcutIconResourceExtraKey(), ShortcutIconResource::class.java)
                                        val intent = result.data?.shortcutParcelableExtraCompat(shortcutIntentExtraKey(), Intent::class.java)?.toUri(Intent.URI_INTENT_SCHEME)
                                        val label = result.data?.shortcutStringExtraCompat(shortcutNameExtraKey()).orEmpty()
                                        val iconRes = if (shortcutIconRes != null) {
                                            withContext(Dispatchers.IO) {
                                                LauncherIconQuery.resolveShortcutIconResourceId(context, shortcutIconRes)
                                            }
                                        } else 0
                                        val shortcutInfo = LauncherInfo.ShortcutInfo(
                                            packageName = launcherInfo.packageName, className = launcherInfo.className,
                                            intents = intent?.let { listOf(it) } ?: emptyList(), label = label,
                                            iconRes = iconRes, iconPath = null, iconBitmap = bitmap
                                        )
                                        vm.addNewShortcut(launcherInfo, shortcutInfo)
                                        if (uiState.selectedRecord.size < MAX_SELECT_COUNT) vm.select(shortcutInfo, true)
                                    }
                                    currentLauncherInfo = null
                                }
                            }
                            ActionPage(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = run {
                                    val direction = LocalLayoutDirection.current
                                    PaddingValues(
                                        start = contentPadding.calculateStartPadding(direction),
                                        end = contentPadding.calculateEndPadding(direction),
                                        bottom = contentPadding.calculateBottomPadding() + ScrollBottomPadding
                                    )
                                },
                                actions = uiState.actions,
                                appInfos = uiState.apps,
                                createShortcuts = uiState.createShortcuts,
                                launchShortcuts = uiState.launchShortcuts,
                                selectedRecord = uiState.selectedRecord,
                                selectSingle = uiState.selectSingle,
                                snackbarHostState = snackbarHostState,
                                permissionState = permissionState,
                                onSelect = { action, selected -> vm.select(action, selected) },
                                onSettingsClick = { action -> vm.actionSettingsDialog.show(true, action) },
                                onSelectApp = { appInfo, selected -> vm.select(appInfo, selected) },
                                onSelectShortcut = { shortcutInfo, selected -> vm.select(shortcutInfo, selected) },
                                onAppLongClick = { appInfo -> vm.toggleMiniWindow(appInfo) },
                                onShortcutClick = { launcherInfo ->
                                    try {
                                        currentLauncherInfo = launcherInfo
                                        shortcutLauncher.launch(Intent().apply { setClassName(launcherInfo.packageName, launcherInfo.className) })
                                    } catch (ignored: Exception) { currentLauncherInfo = null }
                                },
                                onOpenAppOrUrl = { showOpenAppOrUrlDialog = true }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun <T : android.os.Parcelable> Intent.shortcutParcelableExtraCompat(
    key: String,
    clazz: Class<T>
): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        @Suppress("DEPRECATION")
        getParcelableExtra(key, clazz)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(key)
    }
}

private fun Intent.shortcutStringExtraCompat(key: String): String? {
    @Suppress("DEPRECATION")
    return getStringExtra(key)
}

@Suppress("DEPRECATION")
private fun shortcutIconExtraKey(): String = Intent.EXTRA_SHORTCUT_ICON

@Suppress("DEPRECATION")
private fun shortcutIconResourceExtraKey(): String = Intent.EXTRA_SHORTCUT_ICON_RESOURCE

@Suppress("DEPRECATION")
private fun shortcutIntentExtraKey(): String = Intent.EXTRA_SHORTCUT_INTENT

@Suppress("DEPRECATION")
private fun shortcutNameExtraKey(): String = Intent.EXTRA_SHORTCUT_NAME
