package hunoia.sideleap.ui.screen.frozenappmanage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.component.UDFComponent
import hunoia.sideleap.R
import hunoia.sideleap.ui.screen.frozenappprotect.FrozenAppProtectContent
import hunoia.sideleap.ui.theme.ScrollBottomPadding
import hunoia.sideleap.ui.widget.LabeledSwitch
import hunoia.sideleap.ui.widget.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrozenAppManageScreen(
    onBack: () -> Unit,
    vm: FrozenAppManageVM = viewModel()
) {
    var showProtectPage by remember { mutableStateOf(false) }
    UDFComponent(component = vm.udfComponent, onEvent = { }) { uiState ->
        var fabExpanded by remember { mutableStateOf(false) }
        var fabVisible by remember { mutableStateOf(true) }
        val listState = rememberLazyListState()
        LaunchedEffect(Unit) {
            vm.reloadApps()
        }
        LaunchedEffect(listState) {
            var prevIndex = 0
            var prevOffset = 0
            snapshotFlow {
                listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
            }.collect { (index, offset) ->
                val scrollingDown = index > prevIndex || (index == prevIndex && offset > prevOffset)
                val scrollingUp = index < prevIndex || (index == prevIndex && offset < prevOffset)
                if (scrollingDown) {
                    fabVisible = false
                    fabExpanded = false
                } else if (scrollingUp || (index == 0 && offset == 0)) {
                    fabVisible = true
                }
                prevIndex = index
                prevOffset = offset
            }
        }
        val fabEnabled = uiState.shizukuReady && uiState.oneKeyTargetCount > 0 && !uiState.bulkActionRunning
        Scaffold(
            topBar = {
                TopBar(
                    onBack = onBack,
                    title = stringResource(id = R.string.frozen_app_manage),
                    actions = {
                        IconButton(onClick = vm::reloadApps) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(id = R.string.refresh)
                            )
                        }
                        IconButton(onClick = { showProtectPage = true }) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = stringResource(id = R.string.protected_list)
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                AnimatedVisibility(visible = fabVisible) {
                    Column {
                        if (fabExpanded) {
                            SmallFloatingActionButton(
                                onClick = {
                                    if (!fabEnabled) return@SmallFloatingActionButton
                                    vm.onOneKeyFreezeAll()
                                    fabExpanded = false
                                },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .alpha(if (fabEnabled) 1f else 0.5f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AcUnit,
                                    contentDescription = stringResource(id = R.string.frozen_one_key_freeze)
                                )
                            }
                            SmallFloatingActionButton(
                                onClick = {
                                    if (!fabEnabled) return@SmallFloatingActionButton
                                    vm.onOneKeyUnfreezeAll()
                                    fabExpanded = false
                                },
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .alpha(if (fabEnabled) 1f else 0.5f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AcUnit,
                                    contentDescription = stringResource(id = R.string.frozen_one_key_unfreeze)
                                )
                            }
                        }
                        FloatingActionButton(
                            onClick = {
                                if (!fabEnabled) return@FloatingActionButton
                                fabExpanded = !fabExpanded
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.alpha(if (fabEnabled) 1f else 0.5f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AcUnit,
                                contentDescription = stringResource(id = R.string.frozen_one_key_actions)
                            )
                        }
                    }
                }
            }
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = contentPadding.calculateTopPadding())
            ) {
                LabeledSwitch(
                    onCheckedChange = vm::onShowSystemAppsChange,
                    checked = uiState.showSystemApps,
                    text = stringResource(id = R.string.show_system_apps)
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    text = stringResource(
                        id = R.string.frozen_app_count_info,
                        uiState.selectedCount,
                        uiState.frozenCount
                    ),
                    style = MaterialTheme.typography.titleMedium
                )

                FrozenAppSearchField(
                    query = uiState.query,
                    onQueryChange = vm::onQueryChange,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = ScrollBottomPadding)
                ) {
                    if (!uiState.hasAnyAppInRange) {
                        item(key = "empty") {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 24.dp),
                                text = stringResource(id = R.string.no_apps_available),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else if (!uiState.hasSearchResult) {
                        item(key = "no_match") {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 24.dp),
                                text = stringResource(id = R.string.no_matching_results),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        if (uiState.oneKeyApps.isNotEmpty()) {
                            item(key = "one_key_title") {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        text = stringResource(id = R.string.one_key_list),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            items(uiState.oneKeyApps, key = { it.packageName }) { app ->
                                FrozenAppSelectableItem(
                                    app = app,
                                    checked = app.packageName in uiState.oneKeyPackageNames,
                                    isFrozen = uiState.frozenStateByPackage[app.packageName] == true,
                                    showFrozenIndicator = true,
                                    frozenActionEnabled = uiState.shizukuReady && !uiState.bulkActionRunning && app.packageName !in uiState.runningPackageActions,
                                    onFrozenActionClick = { vm.onToggleFrozen(app.packageName) },
                                    onCheckedChange = { vm.onOneKeyChecked(app.packageName, it) }
                                )
                            }
                        }
                        if (uiState.otherApps.isNotEmpty()) {
                            item(key = "other_title") {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        text = stringResource(id = R.string.other_apps),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            items(uiState.otherApps, key = { it.packageName }) { app ->
                                FrozenAppSelectableItem(
                                    app = app,
                                    checked = app.packageName in uiState.oneKeyPackageNames,
                                    isFrozen = uiState.frozenStateByPackage[app.packageName] == true,
                                    showFrozenIndicator = true,
                                    frozenActionEnabled = uiState.shizukuReady && !uiState.bulkActionRunning && app.packageName !in uiState.runningPackageActions,
                                    onFrozenActionClick = { vm.onToggleFrozen(app.packageName) },
                                    onCheckedChange = { vm.onOneKeyChecked(app.packageName, it) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showProtectPage) {
            ModalBottomSheet(
                onDismissRequest = { showProtectPage = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                FrozenAppProtectContent(onDismiss = { showProtectPage = false })
            }
        }
    }
}
