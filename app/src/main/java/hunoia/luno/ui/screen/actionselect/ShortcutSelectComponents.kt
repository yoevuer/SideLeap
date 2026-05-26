package hunoia.luno.ui.screen.actionselect

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import com.aaron.compose.ktx.onClick
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import androidx.compose.foundation.ExperimentalFoundationApi
import hunoia.luno.R
import hunoia.luno.settings.defaults.SettingsUiDefaults
import hunoia.luno.launcher.model.icon
import hunoia.luno.launcher.model.qualifiedName
import hunoia.luno.launcher.model.LauncherInfo
import hunoia.luno.ui.screen.actionselect.ActionSelectVM.UiState.SelectedRecord
import hunoia.luno.ui.theme.ContentPaddingHorizontal
import hunoia.luno.ui.theme.ContentPaddingVertical
import hunoia.luno.ui.theme.IconTextPadding
import hunoia.luno.ui.theme.ItemPadding
import hunoia.luno.ui.theme.MinInteractiveSize
import hunoia.luno.ui.theme.SubMinInteractiveSize
import hunoia.luno.ui.theme.TopBarPaddingExtra
import androidx.compose.foundation.background
import androidx.compose.ui.util.fastForEach

@OptIn(ExperimentalPermissionsApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun ShortcutPage(
    onClick: (LauncherInfo) -> Unit,
    onSelect: (LauncherInfo.ShortcutInfo, Boolean) -> Unit,
    createShortcuts: List<LauncherInfo>,
    launchShortcuts: List<LauncherInfo>,
    selectedRecord: SelectedRecord,
    snackbarHostState: SnackbarHostState,
    permissionState: PermissionState,
    selectSingle: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    maxSelectCount: Int = MAX_SELECT_COUNT
) {
    val selectedShortcuts = remember(selectedRecord.list) {
        selectedRecord.list.filterIsInstance<LauncherInfo.ShortcutInfo>()
    }
    Box(modifier = modifier) {
        if (permissionState.status.isGranted) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding
            ) {
                if (createShortcuts.isEmpty() && launchShortcuts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_available_shortcuts),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    if (!selectSingle && selectedShortcuts.isNotEmpty()) {
                        item(key = "selected_bar") {
                            SelectedBar(
                                selectedItems = selectedShortcuts,
                                maxSelectCount = maxSelectCount,
                                showMaxSelectCount = selectSingle,
                                itemLabel = { (it as LauncherInfo.ShortcutInfo).label },
                                onRemoveItem = { shortcutInfo -> onSelect(shortcutInfo as LauncherInfo.ShortcutInfo, false) },
                                onClearAll = { selectedShortcuts.toList().forEach { onSelect(it, false) } }
                            )
                        }
                    }
                    if (createShortcuts.isNotEmpty()) {
                        stickyHeader {
                            Text(
                                modifier = Modifier
                                    .background(color = MaterialTheme.colorScheme.background)
                                    .fillMaxWidth()
                                    .padding(vertical = ContentPaddingVertical)
                                    .padding(horizontal = ContentPaddingHorizontal * 2),
                                text = stringResource(R.string.create_shortcut),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    items(
                        items = createShortcuts,
                        key = { it.qualifiedName }
                    ) { item ->
                        LauncherInfoItem(
                            launcherInfo = item,
                            selectSingle = selectSingle,
                            canLauncherInfoEnabled = { canLauncherInfoEnabled(selectedRecord, it, maxSelectCount) },
                            canShortcutInfoEnabled = { canShortcutInfoEnabled(selectedRecord, it, maxSelectCount) },
                            isShortcutInfoSelected = { shortcutInfo ->
                                selectedRecord.isSelected(shortcutInfo)
                            },
                            onSelect = { shortcutInfo, selected ->
                                onSelect(shortcutInfo, selected)
                            },
                            onClick = {
                                onClick(item)
                            }
                        )
                    }
                    if (launchShortcuts.isNotEmpty()) {
                        stickyHeader {
                            Text(
                                modifier = Modifier
                                    .background(color = MaterialTheme.colorScheme.background)
                                    .fillMaxWidth()
                                    .padding(vertical = ContentPaddingVertical)
                                    .padding(horizontal = ContentPaddingHorizontal * 2),
                                text = stringResource(R.string.launch_shortcut),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    items(
                        items = launchShortcuts,
                        key = { it.qualifiedName }
                    ) { item ->
                        LauncherInfoItem(
                            launcherInfo = item,
                            selectSingle = selectSingle,
                            canLauncherInfoEnabled = { canLauncherInfoEnabled(selectedRecord, it, maxSelectCount) },
                            canShortcutInfoEnabled = { canShortcutInfoEnabled(selectedRecord, it, maxSelectCount) },
                            isShortcutInfoSelected = { shortcutInfo ->
                                selectedRecord.isSelected(shortcutInfo)
                            },
                            onSelect = { shortcutInfo, selected ->
                                onSelect(shortcutInfo, selected)
                            },
                            onClick = {
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

@Composable
internal fun LauncherInfoItem(
    canLauncherInfoEnabled: (LauncherInfo) -> Boolean,
    canShortcutInfoEnabled: (LauncherInfo.ShortcutInfo) -> Boolean,
    isShortcutInfoSelected: (LauncherInfo.ShortcutInfo) -> Boolean,
    onClick: () -> Unit,
    onSelect: (LauncherInfo.ShortcutInfo, Boolean) -> Unit,
    launcherInfo: LauncherInfo,
    selectSingle: Boolean
) {
    Column(
        modifier = Modifier
            .alpha(if (canLauncherInfoEnabled(launcherInfo)) 1f else SettingsUiDefaults.DisabledAlpha)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onClick(enabled = canLauncherInfoEnabled(launcherInfo)) {
                    onClick()
                }
                .padding(vertical = ContentPaddingVertical),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current
            AsyncImage(
                modifier = Modifier
                    .padding(start = ContentPaddingHorizontal * 2)
                    .size(MinInteractiveSize),
                model = launcherInfo.icon,
                contentDescription = null,
                imageLoader = context.imageLoader,
            )
            Column(
                modifier = Modifier
                    .padding(start = IconTextPadding, end = ItemPadding)
                    .weight(1f)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = launcherInfo.label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = launcherInfo.packageName,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Column {
            launcherInfo.shortcuts.fastForEach { shortcutInfo ->
                key(shortcutInfo) {
                    val selected = isShortcutInfoSelected(shortcutInfo)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onClick(enabled = canShortcutInfoEnabled(shortcutInfo)) {
                                onSelect(shortcutInfo, !selected)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val context = LocalContext.current
                        AsyncImage(
                            modifier = Modifier
                                .padding(start = ContentPaddingHorizontal * 3)
                                .size(SubMinInteractiveSize),
                            model = shortcutInfo.icon,
                            contentDescription = null,
                            imageLoader = context.imageLoader
                        )
                        Column(
                            modifier = Modifier
                                .padding(start = IconTextPadding, end = ItemPadding)
                                .weight(1f)
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = shortcutInfo.label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (!selectSingle) {
                            Checkbox(
                                modifier = Modifier.padding(end = TopBarPaddingExtra),
                                enabled = canShortcutInfoEnabled(shortcutInfo),
                                checked = selected,
                                onCheckedChange = { newSelected ->
                                    onSelect(shortcutInfo, newSelected)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
