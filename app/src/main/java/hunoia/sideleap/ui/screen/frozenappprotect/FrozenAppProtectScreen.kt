package hunoia.sideleap.ui.screen.frozenappprotect

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
        var protectedExpanded by remember { mutableStateOf(false) }
        var controlsHeightPx by remember { mutableStateOf(0) }
        val density = LocalDensity.current
        val gridState = rememberLazyGridState()
        val hideControlsOnUserScroll = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (source == NestedScrollSource.UserInput && available.y < 0f && controlsVisible) {
                        controlsVisible = false
                    }
                    return Offset.Zero
                }
            }
        }
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
                } else if (scrollingUp) {
                    controlsVisible = true
                }
                prevIndex = index
                prevOffset = offset
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(hideControlsOnUserScroll)
        ) {
            val gridTopPadding by animateDpAsState(
                targetValue = if (controlsVisible) with(density) { controlsHeightPx.toDp() } + 4.dp else 4.dp,
                label = "frozenProtectGridTopPadding"
            )
            val showOtherApps = uiState.query.isNotBlank()

            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Adaptive(minSize = 64.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(
                    start = 8.dp,
                    end = 8.dp,
                    top = gridTopPadding,
                    bottom = ScrollBottomPadding
                ),
                modifier = Modifier.fillMaxSize()
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
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { protectedExpanded = !protectedExpanded }
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier.graphicsLayer {
                                            rotationX = if (protectedExpanded) 0f else 180f
                                        }
                                    )
                                    Spacer(Modifier.padding(4.dp))
                                    Text(
                                        text = stringResource(id = R.string.protected_list),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            if (protectedExpanded) {
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
                        }
                        if (showOtherApps && uiState.unprotectedApps.isNotEmpty()) {
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

            AnimatedVisibility(
                visible = controlsVisible,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .onSizeChanged { controlsHeightPx = it.height }
            ) {
                Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                    Column {
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
            }
        }
    }
}
