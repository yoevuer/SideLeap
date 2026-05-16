package hunoia.sideleap.ui.screen.actionselect

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import androidx.compose.foundation.combinedClickable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import hunoia.sideleap.R
import hunoia.sideleap.settings.api.SettingsUiDefaults
import hunoia.sideleap.launcher.ext.icon
import hunoia.sideleap.launcher.ext.qualifiedName
import hunoia.sideleap.launcher.model.AppInfo
import hunoia.sideleap.system.api.gotoAppDetailSettings
import hunoia.sideleap.ui.permission.deniedForever
import hunoia.sideleap.ui.screen.actionselect.ActionSelectVM.UiState.SelectedRecord
import hunoia.sideleap.ui.theme.ContentPaddingHorizontal
import hunoia.sideleap.ui.theme.ContentPaddingVertical
import hunoia.sideleap.ui.theme.IconTextPadding
import hunoia.sideleap.ui.theme.ItemPadding
import hunoia.sideleap.ui.theme.MinInteractiveSize
import hunoia.sideleap.ui.theme.TopBarPaddingExtra
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.SnackbarResult

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun AppPage(
    onLongClick: (AppInfo) -> Unit,
    onSelect: (AppInfo, Boolean) -> Unit,
    appInfos: List<AppInfo>,
    selectedRecord: SelectedRecord,
    snackbarHostState: SnackbarHostState,
    permissionState: PermissionState,
    selectSingle: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    maxSelectCount: Int = MAX_SELECT_COUNT
) {
    val selectedApps = remember(selectedRecord.list) {
        selectedRecord.list.filterIsInstance<AppInfo>()
    }
    Box(modifier = modifier) {
        if (permissionState.status.isGranted) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding
            ) {
                if (appInfos.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_available_apps),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    if (!selectSingle && selectedApps.isNotEmpty()) {
                        item(key = "selected_bar") {
                            SelectedBar(
                                selectedItems = selectedApps,
                                maxSelectCount = maxSelectCount,
                                itemLabel = { (it as AppInfo).label },
                                onRemoveItem = { appInfo -> onSelect(appInfo as AppInfo, false) },
                                onClearAll = { selectedApps.toList().forEach { onSelect(it, false) } }
                            )
                        }
                    }
                    items(
                        items = appInfos,
                        key = { it.qualifiedName }
                    ) { item ->
                        AppItem(
                            appInfo = item,
                            selected = selectedRecord.isSelected(item),
                            selectSingle = selectSingle,
                            enabled = canAppInfoEnabled(selectedRecord, item, maxSelectCount),
                            onSelect = { selected ->
                                onSelect(item, selected)
                            },
                            onLongClick = {
                                onLongClick(item)
                            }
                        )
                    }
                }
            }
        } else {
            PermissionPage(
                snackbarHostState = snackbarHostState,
                permissionState = permissionState
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AppItem(
    onLongClick: () -> Unit,
    onSelect: (Boolean) -> Unit,
    selected: Boolean,
    appInfo: AppInfo,
    selectSingle: Boolean,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .graphicsLayer {
                alpha = if (enabled) 1f else SettingsUiDefaults.DisabledAlpha
            }
            .fillMaxWidth()
            .combinedClickable(
                enabled = enabled,
                onLongClick = onLongClick,
                onClick = {
                    onSelect(!selected)
                }
            )
            .padding(vertical = ContentPaddingVertical),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val context = LocalContext.current
        AsyncImage(
            modifier = Modifier
                .padding(start = ContentPaddingHorizontal * 2)
                .size(MinInteractiveSize),
            model = appInfo.icon,
            contentDescription = null,
            imageLoader = context.imageLoader,
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .padding(start = IconTextPadding, end = ItemPadding)
                .weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (appInfo.miniWindow) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = Icons.Default.Window,
                        contentDescription = null
                    )
                }
                Text(
                    modifier = Modifier.weight(1f),
                    text = appInfo.label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = appInfo.packageName,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium
            )
        }
        if (!selectSingle) {
            Checkbox(
                modifier = Modifier.padding(end = TopBarPaddingExtra),
                enabled = enabled,
                checked = selected,
                onCheckedChange = onSelect
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun PermissionPage(
    snackbarHostState: SnackbarHostState,
    permissionState: PermissionState
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        TextButton(
            onClick = {
                if (permissionState.status.deniedForever) {
                    coroutineScope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = context.getString(R.string.goto_grant_get_apps_permission),
                            actionLabel = context.getString(R.string.goto_enable_settings),
                            withDismissAction = true
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            context.gotoAppDetailSettings()
                        }
                    }
                } else {
                    permissionState.launchPermissionRequest()
                }
            }
        ) {
            Text(text = stringResource(id = R.string.request_get_apps_permission))
        }
    }
}
