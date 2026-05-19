package hunoia.sideleap.ui.screen.freeze

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import hunoia.sideleap.ui.screen.freeze.FrozenAppProtectContent
import hunoia.sideleap.ui.theme.ScrollBottomPadding
import hunoia.sideleap.ui.component.BottomSheetNestedContent
import hunoia.sideleap.ui.component.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrozenAppManageScreen(
    onBack: () -> Unit,
    vm: FrozenAppManageVM = viewModel()
) {
    var showProtectPage by remember { mutableStateOf(false) }
    UDFComponent(component = vm.udfComponent, onEvent = { }) { uiState ->
        var controlsExpanded by remember { mutableStateOf(false) }
        var controlsVisible by remember { mutableStateOf(true) }
        var oneKeyExpanded by remember { mutableStateOf(false) }
        var controlsHeightPx by remember { mutableStateOf(0) }
        val density = LocalDensity.current
        val gridState = rememberLazyGridState()
        val hideControlsOnUserScroll = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (source == NestedScrollSource.UserInput && available.y < 0f && controlsVisible) {
                        controlsVisible = false
                        controlsExpanded = false
                    }
                    return Offset.Zero
                }
            }
        }
        LaunchedEffect(Unit) {
            vm.reloadApps()
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
                    controlsExpanded = false
                } else if (scrollingUp) {
                    controlsVisible = true
                }
                prevIndex = index
                prevOffset = offset
            }
        }
        val fabEnabled = uiState.shizukuReady && uiState.oneKeyTargetCount > 0 && !uiState.bulkActionRunning
        Scaffold(
            topBar = {
                TopBar(
                    onBack = {
                        vm.commitSelections()
                        onBack()
                    },
                    title = stringResource(id = R.string.frozen_app_manage),
                    actions = {
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
                AnimatedVisibility(visible = controlsVisible) {
                    Column {
                        if (controlsExpanded) {
                            SmallFloatingActionButton(
                                onClick = {
                                    if (!fabEnabled) return@SmallFloatingActionButton
                                    vm.onOneKeyFreezeAll()
                                    controlsExpanded = false
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
                                    controlsExpanded = false
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
                            SmallFloatingActionButton(
                                onClick = {
                                    vm.onOneKeySelectFrozen()
                                    controlsExpanded = false
                                },
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = stringResource(id = R.string.frozen_one_key_select_frozen)
                                )
                            }
                        }
                        FloatingActionButton(
                            onClick = {
                                if (!fabEnabled) return@FloatingActionButton
                                controlsExpanded = !controlsExpanded
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
                val gridTopPadding by animateDpAsState(
                    targetValue = if (controlsVisible) with(density) { controlsHeightPx.toDp() } + 4.dp else 4.dp,
                    label = "frozenManageGridTopPadding"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .nestedScroll(hideControlsOnUserScroll)
                ) {
                    PullToRefreshBox(
                        isRefreshing = uiState.refreshing,
                        onRefresh = { vm.reloadApps() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(4),
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
                        if (uiState.oneKeyApps.isNotEmpty()) {
                            item(span = { GridItemSpan(this.maxLineSpan) }, key = "one_key_title") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { oneKeyExpanded = !oneKeyExpanded }
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .graphicsLayer {
                                                rotationX = if (oneKeyExpanded) 0f else 180f
                                            }
                                    )
                                    Spacer(Modifier.padding(4.dp))
                                    Text(
                                        text = stringResource(id = R.string.one_key_list),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            if (oneKeyExpanded) {
                                items(uiState.oneKeyApps, key = { it.packageName }) { app ->
                                val isPending = app.packageName in uiState.pendingOneKeyPackageNames != app.packageName in uiState.oneKeyPackageNames
                                FrozenAppSelectableItem(
                                    app = app,
                                    isFrozen = uiState.frozenStateByPackage[app.packageName] == true,
                                    isPending = isPending,
                                    longClickEnabled = uiState.shizukuReady && !uiState.bulkActionRunning && app.packageName !in uiState.runningPackageActions,
                                    onClick = { vm.onOneKeyChecked(app.packageName, app.packageName !in uiState.pendingOneKeyPackageNames) },
                                    onLongClick = { vm.onToggleFrozen(app.packageName) }
                                )
                                }
                            }
                        }
                        if (uiState.otherApps.isNotEmpty()) {
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
                            items(uiState.otherApps, key = { it.packageName }) { app ->
                                val isPending = app.packageName in uiState.pendingOneKeyPackageNames != app.packageName in uiState.oneKeyPackageNames
                                FrozenAppSelectableItem(
                                    app = app,
                                    isFrozen = uiState.frozenStateByPackage[app.packageName] == true,
                                    isPending = isPending,
                                    longClickEnabled = uiState.shizukuReady && !uiState.bulkActionRunning && app.packageName !in uiState.runningPackageActions,
                                    onClick = { vm.onOneKeyChecked(app.packageName, app.packageName !in uiState.pendingOneKeyPackageNames) },
                                    onLongClick = { vm.onToggleFrozen(app.packageName) }
                                )
                            }
                        }
                    }
                }
            }
                    androidx.compose.animation.AnimatedVisibility(
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
                                FrozenAppSearchField(
                                    query = uiState.query,
                                    onQueryChange = vm::onQueryChange,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(
                                            id = R.string.frozen_app_count_info,
                                            uiState.selectedCount,
                                            uiState.frozenCount
                                        ),
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Spacer(Modifier.weight(1f))
                                    Text(
                                        text = stringResource(id = R.string.show_system_apps),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Switch(
                                        checked = uiState.showSystemApps,
                                        onCheckedChange = vm::onShowSystemAppsChange
                                    )
                                }
                            }
                        }
                    }
        }
        }
        }

        BackHandler {
            vm.commitSelections()
            onBack()
        }

        if (showProtectPage) {
            ModalBottomSheet(
                onDismissRequest = { showProtectPage = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                BottomSheetNestedContent {
                    FrozenAppProtectContent(onDismiss = { showProtectPage = false })
                }
            }
        }
    }
}
