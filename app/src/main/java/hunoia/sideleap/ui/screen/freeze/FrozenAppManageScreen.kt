package hunoia.sideleap.ui.screen.freeze

import hunoia.sideleap.ui.component.AppSearchBar
import hunoia.sideleap.ui.component.EmptyState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.imageLoader
import com.aaron.compose.component.UDFComponent
import com.aaron.compose.component.UiBaseEvent
import hunoia.sideleap.R
import hunoia.sideleap.launcher.model.AppInfo
import hunoia.sideleap.launcher.model.icon
import hunoia.sideleap.system.feedback.showToast
import hunoia.sideleap.ui.theme.ContentPaddingHorizontal
import hunoia.sideleap.ui.theme.ContentPaddingVertical
import hunoia.sideleap.ui.theme.IconTextPadding
import hunoia.sideleap.ui.theme.ItemPadding
import hunoia.sideleap.ui.theme.MinInteractiveSize
import hunoia.sideleap.ui.theme.ScrollBottomPadding
import hunoia.sideleap.ui.theme.TopBarPaddingExtra

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FrozenAppManageContent(
    onDismiss: () -> Unit,
    vm: FrozenAppManageVM = viewModel()
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
        var selectedExpanded by remember { mutableStateOf(false) }
        var showMenu by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { vm.reloadApps() }

        val context = LocalContext.current
        var searchQuery by remember { mutableStateOf("") }

        val selectedFiltered = remember(searchQuery, uiState.oneKeyApps) {
            if (searchQuery.isBlank()) uiState.oneKeyApps
            else uiState.oneKeyApps.filter {
                it.label.contains(searchQuery, ignoreCase = true) ||
                        it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
        val unselectedFiltered = remember(searchQuery, uiState.otherApps) {
            if (searchQuery.isBlank()) uiState.otherApps
            else uiState.otherApps.filter {
                it.label.contains(searchQuery, ignoreCase = true) ||
                        it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
        val hasAnyMatch = selectedFiltered.isNotEmpty() || unselectedFiltered.isNotEmpty()

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 4.dp),
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
                IconButton(onClick = {
                    vm.clearSelections()
                }) {
                    Icon(Icons.Default.Restore, contentDescription = stringResource(R.string.reset))
                }
                IconButton(onClick = { vm.reloadApps() }) {
                    Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh))
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more))
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            onClick = { showMenu = false; vm.onOneKeyFreezeAll() },
                            text = { Text(stringResource(R.string.frozen_one_key_freeze)) }
                        )
                        DropdownMenuItem(
                            onClick = { showMenu = false; vm.onOneKeyUnfreezeAll() },
                            text = { Text(stringResource(R.string.frozen_one_key_unfreeze)) }
                        )
                        DropdownMenuItem(
                            onClick = { showMenu = false; vm.onOneKeySelectFrozen() },
                            text = { Text(stringResource(R.string.frozen_one_key_select_frozen)) }
                        )
                    }
                }
            }

            AppSearchBar(
                query = searchQuery,
                onQueryChange = { vm.onQueryChange(it); searchQuery = it },
                modifier = Modifier.padding(horizontal = ContentPaddingHorizontal, vertical = 4.dp),
                placeholder = stringResource(R.string.search_app_hint),
            )

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
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.graphicsLayer {
                                    rotationX = if (selectedExpanded) 0f else 180f
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
                    if (selectedExpanded) {
                        items(selectedFiltered, key = { it.packageName }) { app ->
                            val isFrozen = uiState.frozenStateByPackage[app.packageName] == true
                            val checked = app.packageName in uiState.pendingOneKeyPackageNames
                            FrozenAppItem(
                                app = app,
                                isFrozen = isFrozen,
                                checked = checked,
                                onCheckedChange = { vm.onOneKeyChecked(app.packageName, it) },
                                onLongClick = { vm.onToggleFrozen(app.packageName) }
                            )
                        }
                    }
                }
                if (unselectedFiltered.isNotEmpty()) {
                    item(key = "unselected_header") {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            text = stringResource(id = R.string.other_apps),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    items(unselectedFiltered, key = { it.packageName }) { app ->
                        val isFrozen = uiState.frozenStateByPackage[app.packageName] == true
                        val checked = app.packageName in uiState.pendingOneKeyPackageNames
                        FrozenAppItem(
                            app = app,
                            isFrozen = isFrozen,
                            checked = checked,
                            onCheckedChange = { vm.onOneKeyChecked(app.packageName, it) },
                            onLongClick = { vm.onToggleFrozen(app.packageName) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FrozenAppItem(
    app: AppInfo,
    isFrozen: Boolean,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onLongClick: () -> Unit,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onCheckedChange(!checked) },
                onLongClick = onLongClick
            )
            .padding(vertical = ContentPaddingVertical),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            modifier = Modifier
                .padding(start = ContentPaddingHorizontal * 2)
                .size(MinInteractiveSize)
                .clip(RoundedCornerShape(12.dp)),
            model = app.icon,
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
                text = app.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = app.packageName,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium
            )
        }
        if (isFrozen) {
            Icon(
                modifier = Modifier.padding(end = 4.dp),
                imageVector = Icons.Default.AcUnit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Checkbox(
            modifier = Modifier.padding(end = TopBarPaddingExtra),
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
