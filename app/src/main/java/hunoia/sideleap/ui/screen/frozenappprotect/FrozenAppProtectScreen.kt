package hunoia.sideleap.ui.screen.frozenappprotect

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.component.UDFComponent
import hunoia.sideleap.R
import hunoia.sideleap.ui.screen.frozenappmanage.FrozenAppSearchField
import hunoia.sideleap.ui.screen.frozenappmanage.FrozenAppSelectableItem
import hunoia.sideleap.ui.theme.ScrollBottomPadding
import hunoia.sideleap.ui.widget.LabeledSwitch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FrozenAppProtectContent(
    onDismiss: () -> Unit,
    vm: FrozenAppProtectVM = viewModel()
) {
    DisposableEffect(Unit) {
        onDispose {
            vm.commitSelections()
        }
    }

    UDFComponent(component = vm.udfComponent, onEvent = { }) { uiState ->
        LaunchedEffect(Unit) {
            vm.reloadApps()
        }

        var controlsVisible by remember { mutableStateOf(true) }
        var controlsHeightPx by remember { mutableStateOf(0) }
        val density = LocalDensity.current
        val gridState = rememberLazyGridState()
        LaunchedEffect(gridState) {
            var prevIndex = 0
            var prevOffset = 0
            snapshotFlow {
                gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset
            }.collect { (index, offset) ->
                val scrollingDown = index > prevIndex || (index == prevIndex && offset > prevOffset)
                val scrollingUp = index < prevIndex || (index == prevIndex && offset < prevOffset)
                if (scrollingDown) {
                    controlsVisible = false
                } else if (scrollingUp || (index == 0 && offset == 0)) {
                    controlsVisible = true
                }
                prevIndex = index
                prevOffset = offset
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            val extraGridBottomPadding = if (controlsVisible) 0.dp else with(density) { controlsHeightPx.toDp() }

            AnimatedVisibility(visible = controlsVisible) {
                Column(
                    modifier = Modifier.onSizeChanged { controlsHeightPx = it.height }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.protected_list), style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = vm::reloadApps) {
                            Icon(Icons.Default.Refresh, contentDescription = stringResource(id = R.string.refresh))
                        }
                    }
                    LabeledSwitch(
                        onCheckedChange = vm::onShowSystemAppsChange,
                        checked = uiState.showSystemApps,
                        text = stringResource(id = R.string.show_system_apps)
                    )

                    FrozenAppSearchField(
                        query = uiState.query,
                        onQueryChange = vm::onQueryChange,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Adaptive(minSize = 64.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(
                    start = 8.dp,
                    end = 8.dp,
                    top = 4.dp,
                    bottom = ScrollBottomPadding + extraGridBottomPadding
                ),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                    if (!uiState.hasAnyAppInRange) {
                        item(span = { GridItemSpan(this.maxLineSpan) }, key = "empty") {
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
                        item(span = { GridItemSpan(this.maxLineSpan) }, key = "no_match") {
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
                        if (uiState.protectedApps.isNotEmpty()) {
                            item(span = { GridItemSpan(this.maxLineSpan) }, key = "protected_title") {
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    text = stringResource(id = R.string.protected_list),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            items(uiState.protectedApps, key = { it.packageName }) { app ->
                                val isPending = app.packageName in uiState.pendingProtectedPackageNames != app.packageName in uiState.protectedPackageNames
                                FrozenAppSelectableItem(
                                    app = app,
                                    isFrozen = uiState.frozenStateByPackage[app.packageName] == true,
                                    isPending = isPending,
                                    longClickEnabled = uiState.shizukuReady && uiState.frozenStateByPackage[app.packageName] == true && app.packageName !in uiState.runningPackageActions,
                                    onClick = { vm.onProtectedChecked(app.packageName, app.packageName !in uiState.pendingProtectedPackageNames) },
                                    onLongClick = { vm.onUnfreeze(app.packageName) }
                                )
                            }
                        }
                        if (uiState.unprotectedApps.isNotEmpty()) {
                            item(span = { GridItemSpan(this.maxLineSpan) }, key = "other_title") {
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    text = stringResource(id = R.string.other_apps),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            items(uiState.unprotectedApps, key = { it.packageName }) { app ->
                                val isPending = app.packageName in uiState.pendingProtectedPackageNames != app.packageName in uiState.protectedPackageNames
                                FrozenAppSelectableItem(
                                    app = app,
                                    isFrozen = uiState.frozenStateByPackage[app.packageName] == true,
                                    isPending = isPending,
                                    longClickEnabled = uiState.shizukuReady && uiState.frozenStateByPackage[app.packageName] == true && app.packageName !in uiState.runningPackageActions,
                                    onClick = { vm.onProtectedChecked(app.packageName, app.packageName !in uiState.pendingProtectedPackageNames) },
                                    onLongClick = { vm.onUnfreeze(app.packageName) }
                                )
                            }
                        }
                    }
            }
        }
    }
}