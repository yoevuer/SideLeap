package hunoia.luno.ui.screen.freeze
import hunoia.luno.ui.theme.*

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.imageLoader
import com.aaron.compose.component.LoadingComponent
import com.aaron.compose.component.UDFComponent
import com.aaron.compose.component.UiBaseEvent
import com.aaron.compose.ktx.onClick
import hunoia.luno.R
import hunoia.luno.launcher.model.AppInfo
import hunoia.luno.ui.permission.deniedForever
import hunoia.luno.system.intent.gotoAppDetailSettings
import hunoia.luno.system.feedback.showToast
import hunoia.luno.launcher.model.icon
import hunoia.luno.launcher.model.qualifiedName
import hunoia.luno.ui.permission.rememberGetInstalledAppsPermissionState
import hunoia.luno.ui.theme.ContentPaddingHorizontal
import hunoia.luno.ui.theme.ContentPaddingVertical
import hunoia.luno.ui.theme.IconTextPadding
import hunoia.luno.ui.theme.ItemPadding
import hunoia.luno.ui.theme.MinInteractiveSize
import hunoia.luno.ui.theme.ScrollBottomPadding
import hunoia.luno.ui.theme.TopBarPaddingExtra
import hunoia.luno.ui.component.AppSearchBar
import hunoia.luno.ui.component.EmptyState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import kotlinx.coroutines.launch

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/12/1
 */

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppBlacklistContent(
    onDismiss: () -> Unit,
    vm: AppBlacklistVM = viewModel()
) {
    UDFComponent(
        component = vm.udfComponent,
        onEvent = { },
        onBaseEvent = { baseEvent ->
            when (baseEvent) {
                is UiBaseEvent.Finish -> { onDismiss(); true }
                is UiBaseEvent.ResToast -> { showToast(baseEvent.res); true }
                is UiBaseEvent.StringToast -> { showToast(baseEvent.text); true }
                else -> false
            }
        }
    ) { uiState ->
        val permissionState = rememberGetInstalledAppsPermissionState { granted ->
            if (granted) vm.reloadApps()
        }
        LaunchedEffect(vm, permissionState) {
            if (!permissionState.status.isGranted) {
                permissionState.launchPermissionRequest()
            } else {
                vm.reloadApps()
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            if (permissionState.status.isGranted) {
                LoadingComponent(
                    modifier = Modifier.fillMaxSize(),
                    component = vm.loadingComponent
                ) {
                    var searchQuery by remember { mutableStateOf("") }
                    val selectedFiltered = remember(searchQuery, uiState.selectedAppInfos) {
                        if (searchQuery.isBlank()) uiState.selectedAppInfos
                        else uiState.selectedAppInfos.filter {
                            it.label.contains(searchQuery, ignoreCase = true) ||
                                    it.packageName.contains(searchQuery, ignoreCase = true)
                        }
                    }
                    val unselectedFiltered = remember(searchQuery, uiState.unselectedAppInfos) {
                        if (searchQuery.isBlank()) uiState.unselectedAppInfos
                        else uiState.unselectedAppInfos.filter {
                            it.label.contains(searchQuery, ignoreCase = true) ||
                                    it.packageName.contains(searchQuery, ignoreCase = true)
                        }
                    }
                    val hasAnyMatch = selectedFiltered.isNotEmpty() || unselectedFiltered.isNotEmpty()
                    var selectedExpanded by remember { mutableStateOf(false) }

                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = ContentPaddingHorizontal, end = Spacing4, top = Spacing4),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppSearchBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                modifier = Modifier.weight(1f).padding(end = Spacing4),
                                placeholder = stringResource(R.string.search_app_hint),
                            )
                            IconButton(onClick = { vm.reset() }) {
                                Icon(Icons.Default.Restore, contentDescription = stringResource(R.string.reset))
                            }
                            IconButton(onClick = { vm.reloadApps() }) {
                                Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh))
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = ScrollBottomPadding)
                        ) {
                            if (!hasAnyMatch && searchQuery.isNotBlank()) {
                                item {
                                    EmptyState(message = stringResource(R.string.no_matching_results))
                                }
                            }
                            if (selectedFiltered.isNotEmpty()) {
                                item(key = "selected_header") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedExpanded = !selectedExpanded }
                                            .padding(horizontal = Spacing16, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            modifier = Modifier.graphicsLayer {
                                                rotationX = if (selectedExpanded) 0f else 180f
                                            }
                                        )
                                        Spacer(Modifier.padding(Spacing4))
                                        Text(
                                            text = "已选",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.weight(1f))
                                        Text(
                                            text = "${uiState.excludeApps.size}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (selectedExpanded) {
                                    items(selectedFiltered, key = { it.qualifiedName }) { item ->
                                        AppBlacklistItem(
                                            appInfo = item,
                                            selected = item.packageName in uiState.excludeApps,
                                            onSelect = { selected ->
                                                vm.selectApp(item, selected)
                                            }
                                        )
                                    }
                                }
                            }
                            if (unselectedFiltered.isNotEmpty()) {
                                item(key = "unselected_header") {
                                    Text(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = Spacing16, vertical = 8.dp),
                                        text = "未选",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                items(unselectedFiltered, key = { it.qualifiedName }) { item ->
                                    AppBlacklistItem(
                                        appInfo = item,
                                        selected = item.packageName in uiState.excludeApps,
                                        onSelect = { selected ->
                                            vm.selectApp(item, selected)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val context = LocalContext.current
                    TextButton(
                        onClick = {
                            if (permissionState.status.deniedForever) {
                                context.gotoAppDetailSettings()
                            } else {
                                permissionState.launchPermissionRequest()
                            }
                        }
                    ) {
                        Text(text = stringResource(id = R.string.request_get_apps_permission))
                    }
                }
            }
        }
    }
}

@Composable
private fun AppBlacklistItem(
    onSelect: (Boolean) -> Unit,
    selected: Boolean,
    appInfo: AppInfo
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onClick { onSelect(!selected) }
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
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = appInfo.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = appInfo.packageName,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium
            )
        }
        Checkbox(
            modifier = Modifier.padding(end = TopBarPaddingExtra),
            checked = selected,
            onCheckedChange = onSelect
        )
    }
}
