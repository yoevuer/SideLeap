package hunoia.sideleap.ui.screen.appblacklist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.imageLoader
import com.aaron.compose.component.LoadingComponent
import com.aaron.compose.component.UDFComponent
import com.aaron.compose.ktx.onClick
import hunoia.sideleap.R
import hunoia.sideleap.launcher.model.AppInfo
import hunoia.sideleap.ktx.deniedForever
import hunoia.sideleap.ktx.gotoAppDetailSettings
import hunoia.sideleap.ktx.icon
import hunoia.sideleap.ktx.qualifiedName
import hunoia.sideleap.ktx.rememberGetInstalledAppsPermissionState
import hunoia.sideleap.ui.theme.ContentPaddingHorizontal
import hunoia.sideleap.ui.theme.ContentPaddingVertical
import hunoia.sideleap.ui.theme.IconTextPadding
import hunoia.sideleap.ui.theme.ItemPadding
import hunoia.sideleap.ui.theme.MinInteractiveSize
import hunoia.sideleap.ui.theme.ScrollBottomPadding
import hunoia.sideleap.ui.theme.TopBarPaddingExtra
import hunoia.sideleap.ui.widget.MyAlertDialog
import hunoia.sideleap.ui.widget.MySnackbarHost
import hunoia.sideleap.ui.widget.TopBar
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import kotlinx.coroutines.launch

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/12/1
 */

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppBlacklistScreen(
    onBack: () -> Unit,
    vm: AppBlacklistVM = viewModel()
) {
    UDFComponent(component = vm.udfComponent, onEvent = { }) { uiState ->
        if (uiState.showResetWarningDialog) {
            MyAlertDialog(
                onDismissRequest = {
                    vm.showResetWarningDialog(false)
                },
                title = stringResource(id = R.string.reset_default_settings_warning),
                text = stringResource(id = R.string.reset_exclude_apps_warning_desc),
                onConfirmClick = { vm.reset() }
            )
        }

        val permissionState = rememberGetInstalledAppsPermissionState { granted ->
            if (granted) {
                vm.updateAppInfos()
            }
        }
        LaunchedEffect(vm, permissionState) {
            if (!permissionState.status.isGranted) {
                permissionState.launchPermissionRequest()
            } else {
                vm.updateAppInfos()
            }
        }
        val snackbarHostState = remember { SnackbarHostState() }
        Scaffold(
            topBar = {
                TopBar(
                    onBack = onBack,
                    title = stringResource(id = R.string.exclude_app),
                    actions = {
                        if (permissionState.status.isGranted) {
                            IconButton(onClick = { vm.showResetWarningDialog(true) }) {
                                Icon(
                                    imageVector = Icons.Default.Restore,
                                    contentDescription = "Reset"
                                )
                            }
                            IconButton(onClick = { vm.done() }) {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = "Done"
                                )
                            }
                        }
                    }
                )
            },
            snackbarHost = {
                MySnackbarHost(hostState = snackbarHostState)
            }
        ) { contentPadding ->
            Box(modifier = Modifier.padding(top = contentPadding.calculateTopPadding())) {
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
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = run {
                                val direction = LocalLayoutDirection.current
                                PaddingValues(
                                    start = contentPadding.calculateStartPadding(direction),
                                    end = contentPadding.calculateEndPadding(direction),
                                    bottom = contentPadding.calculateBottomPadding() + ScrollBottomPadding
                                )
                            }
                        ) {
                            item {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = ContentPaddingHorizontal, vertical = 4.dp),
                                    placeholder = { Text(stringResource(R.string.search_app_hint)) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.search_clear_cd))
                                            }
                                        }
                                    },
                                    singleLine = true
                                )
                            }
                            if (!hasAnyMatch && searchQuery.isNotBlank()) {
                                item {
                                    Text(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 24.dp),
                                        text = stringResource(R.string.no_matching_results),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            listOf(selectedFiltered, unselectedFiltered).fastForEach { list ->
                                items(
                                    items = list,
                                    key = { it.qualifiedName }
                                ) { item ->
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
                } else {
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
            .onClick {
                onSelect(!selected)
            }
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
                color = MaterialTheme.colorScheme.secondary,
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