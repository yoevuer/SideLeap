package hunoia.sideleap.ui.screen.actionselect

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.onClick
import hunoia.sideleap.R
import hunoia.sideleap.settings.defaults.SettingsUiDefaults
import hunoia.sideleap.action.Action
import hunoia.sideleap.action.display.actionIcon
import hunoia.sideleap.action.display.actionText
import hunoia.sideleap.action.definition.ActionCatalog
import hunoia.sideleap.action.definition.ActionCategory
import hunoia.sideleap.launcher.ext.qualifiedName
import hunoia.sideleap.launcher.model.AppInfo
import hunoia.sideleap.launcher.model.LauncherInfo
import hunoia.sideleap.system.feedback.showToast
import hunoia.sideleap.ui.screen.actionselect.ActionSelectVM.UiState.SelectedRecord
import hunoia.sideleap.ui.theme.ContentPaddingHorizontal
import hunoia.sideleap.ui.theme.IconTextPadding
import hunoia.sideleap.ui.theme.ItemPadding
import hunoia.sideleap.ui.theme.MinIconSize
import hunoia.sideleap.ui.theme.MinInteractiveSize
import hunoia.sideleap.ui.theme.ScrollBottomPadding
import hunoia.sideleap.ui.theme.SubMinInteractiveSize
import hunoia.sideleap.ui.theme.TopBarPaddingExtra

@OptIn(ExperimentalPermissionsApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun ActionPage(
    onSettingsClick: (Action) -> Unit,
    onSelect: (Action, Boolean) -> Unit,
    onSelectLongPress: (Any) -> Unit,
    onSelectApp: (AppInfo, Boolean) -> Unit,
    onSelectShortcut: (LauncherInfo.ShortcutInfo, Boolean) -> Unit,
    onSetLongPress: (Int) -> Unit,
    onClearLongPress: (Int) -> Unit,
    onCancelLongPress: () -> Unit,
    onMoveSelected: (Int, Int) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    onShortcutClick: (LauncherInfo) -> Unit,
    modifier: Modifier = Modifier,
    onOpenAppOrUrl: (() -> Unit)? = null,
    actions: List<Action>,
    appInfos: List<AppInfo>,
    createShortcuts: List<LauncherInfo>,
    launchShortcuts: List<LauncherInfo>,
    selectedRecord: SelectedRecord,
    longPressTargetIndex: Int?,
    selectSingle: Boolean,
    snackbarHostState: SnackbarHostState,
    permissionState: com.google.accompanist.permissions.PermissionState,
    contentPadding: PaddingValues = PaddingValues(),
    maxSelectCount: Int = MAX_SELECT_COUNT
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf<ActionCategory?>(null) }
    var selectedType by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val categoryChips = remember(selectSingle) {
        buildList {
            add(null to context.getString(R.string.all_categories))
            if (!selectSingle) add("selected" to context.getString(R.string.tab_selected))
            add(ActionCategory.NAVIGATION to ActionCategory.NAVIGATION.displayName)
            add(ActionCategory.SYSTEM to ActionCategory.SYSTEM.displayName)
            add(ActionCategory.TOOL to ActionCategory.TOOL.displayName)
            add("app" to context.getString(R.string.tab_apps))
            add("shortcut" to context.getString(R.string.tab_shortcuts))
        }
    }
    LaunchedEffect(selectSingle) {
        if (selectSingle) selectedType = null
    }
    val filteredActions = remember(actions, query, selectedCategory, selectedType) {
        if (query.isNotBlank()) {
            var result = actions
            if (selectedCategory != null) {
                result = result.filter { action ->
                    val cat = ActionCatalog.byId(action.value)?.category ?: ActionCategory.TOOL
                    cat == selectedCategory
                }
            }
            result = result.filter { context.actionText(it, emptyIfNone = false).contains(query, ignoreCase = true) }
            result
        } else if (selectedType == "app" || selectedType == "shortcut" || selectedType == "selected") emptyList()
        else {
            var result = actions
            if (selectedCategory != null) {
                result = result.filter { action ->
                    val cat = ActionCatalog.byId(action.value)?.category ?: ActionCategory.TOOL
                    cat == selectedCategory
                }
            }
            result
        }
    }
    val grouped = remember(filteredActions) {
        val map = LinkedHashMap<ActionCategory, MutableList<Action>>()
        filteredActions.forEach { action ->
            val category = ActionCatalog.byId(action.value)?.category ?: ActionCategory.TOOL
            map.getOrPut(category) { mutableListOf() }.add(action)
        }
        map
    }
    val selectedItems = selectedRecord.list
    val selectingLongPress = longPressTargetIndex != null
    val filteredApps = remember(appInfos, query, selectedType) {
        if (query.isNotBlank()) appInfos.filter { it.label.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true) }
        else if (selectedType != "app") emptyList()
        else appInfos
    }
    val filteredCreateShortcuts = remember(createShortcuts, query, selectedType) {
        if (query.isNotBlank()) createShortcuts.filter { it.label.contains(query, ignoreCase = true) || it.shortcuts.any { s -> s.label.contains(query, ignoreCase = true) } }
        else if (selectedType != "shortcut") emptyList()
        else createShortcuts
    }
    val filteredLaunchShortcuts = remember(launchShortcuts, query, selectedType) {
        if (query.isNotBlank()) launchShortcuts.filter { it.label.contains(query, ignoreCase = true) || it.shortcuts.any { s -> s.label.contains(query, ignoreCase = true) } }
        else if (selectedType != "shortcut") emptyList()
        else launchShortcuts
    }
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding
    ) {
        item(key = "search") {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ContentPaddingHorizontal * 2, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.search_hint_all)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.search_clear_cd))
                        }
                    }
                },
                singleLine = true
            )
        }
        item(key = "category_chips") {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ContentPaddingHorizontal * 2, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categoryChips) { (chipKey, label) ->
                    val isSelected = when (chipKey) {
                        null -> selectedType == null && selectedCategory == null
                        is String -> chipKey == selectedType
                        is ActionCategory? -> chipKey == selectedCategory
                        else -> false
                    }
                    Surface(
                        onClick = {
                            when (chipKey) {
                                null -> { selectedType = null; selectedCategory = null }
                                is String -> {
                                    selectedType = if (isSelected) null else chipKey
                                    if (selectedType != null) selectedCategory = null
                                }
                                is ActionCategory? -> {
                                    selectedCategory = if (isSelected) null else chipKey
                                    if (selectedCategory != null) selectedType = null
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
        if (selectedType == "selected" && !selectSingle && selectedItems.isNotEmpty()) {
            item(key = "selected_bar") {
                SelectedBar(
                    selectedItems = selectedItems,
                    maxSelectCount = maxSelectCount,
                    showMaxSelectCount = selectSingle,
                    itemLabel = { context.selectedItemLabel(it) },
                    onRemoveItem = { item ->
                        when (item) {
                            is Action -> onSelect(item, false)
                            is AppInfo -> onSelectApp(item, false)
                            is LauncherInfo.ShortcutInfo -> onSelectShortcut(item, false)
                        }
                    },
                    onClearAll = {
                        selectedItems.toList().forEach { item ->
                            when (item) {
                                is Action -> onSelect(item, false)
                                is AppInfo -> onSelectApp(item, false)
                                is LauncherInfo.ShortcutInfo -> onSelectShortcut(item, false)
                            }
                        }
                    }
                )
            }
            item(key = "selected_action_settings") {
                SelectedActionSettings(
                    selectedItems = selectedItems,
                    longPressTargetIndex = longPressTargetIndex,
                    itemLabel = { context.selectedItemLabel(it) },
                    onSetLongPress = onSetLongPress,
                    onClearLongPress = onClearLongPress,
                    onCancelLongPress = onCancelLongPress,
                    onMoveSelected = onMoveSelected
                )
            }
        }
        val hasAnyContent = grouped.isNotEmpty() || filteredApps.isNotEmpty() || filteredCreateShortcuts.isNotEmpty() || filteredLaunchShortcuts.isNotEmpty()
        if ((query.isNotEmpty() || selectedCategory != null || selectedType != null) && !hasAnyContent) {
            item {
                Text(
                    text = stringResource(R.string.no_matching_results),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            if (grouped.isNotEmpty()) {
                grouped.forEach { (category, categoryActions) ->
                    stickyHeader(key = "cat_${category.name}") {
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = ContentPaddingHorizontal * 2, vertical = 8.dp)
                        )
                    }
                    items(
                        items = categoryActions,
                        key = { it.value }
                    ) { item ->
                        ActionItem(
                            action = item,
                            selected = selectedRecord.isSelected(item),
                            selectSingle = selectSingle || selectingLongPress,
                            enabled = selectingLongPress || canActionEnabled(selectedRecord, item, maxSelectCount),
                            snackbarHostState = snackbarHostState,
                            onSelect = { selected ->
                                if (selectingLongPress) onSelectLongPress(item) else onSelect(item, selected)
                            },
                            showSettings = ActionCatalog.hasConfig(item.value),
                            onSettingsClick = {
                                if (!selectedRecord.isSelected(item)) {
                                    onSelect(item, true)
                                }
                                onSettingsClick(item)
                            }
                        )
                    }
                }
            }
            if (filteredApps.isNotEmpty()) {
                items(items = filteredApps, key = { "app_${it.qualifiedName}" }) { item ->
                    AppItem(appInfo = item, selected = selectedRecord.isSelected(item), selectSingle = selectSingle || selectingLongPress,
                        enabled = selectingLongPress || canAppInfoEnabled(selectedRecord, item, maxSelectCount),
                        onSelect = { selected ->
                            if (selectingLongPress) onSelectLongPress(item) else onSelectApp(item, selected)
                        }, onLongClick = { onAppLongClick(item) })
                }
            }
            if (filteredCreateShortcuts.isNotEmpty()) {
                stickyHeader(key = "create_shortcuts") {
                    Text(stringResource(R.string.create_shortcut), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = ContentPaddingHorizontal * 2, vertical = 8.dp))
                }
                items(items = filteredCreateShortcuts, key = { "cs_${it.qualifiedName}" }) { item ->
                    LauncherInfoItem(launcherInfo = item, selectSingle = selectSingle || selectingLongPress,
                        canLauncherInfoEnabled = { selectingLongPress || canLauncherInfoEnabled(selectedRecord, it, maxSelectCount) },
                        canShortcutInfoEnabled = { selectingLongPress || canShortcutInfoEnabled(selectedRecord, it, maxSelectCount) },
                        isShortcutInfoSelected = { selectedRecord.isSelected(it) },
                        onSelect = { s, sel ->
                            if (selectingLongPress) onSelectLongPress(s) else onSelectShortcut(s, sel)
                        }, onClick = { onShortcutClick(item) })
                }
            }
            if (filteredLaunchShortcuts.isNotEmpty()) {
                stickyHeader(key = "launch_shortcuts") {
                    Text(stringResource(R.string.launch_shortcut), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = ContentPaddingHorizontal * 2, vertical = 8.dp))
                }
                items(items = filteredLaunchShortcuts, key = { "ls_${it.qualifiedName}" }) { item ->
                    LauncherInfoItem(launcherInfo = item, selectSingle = selectSingle || selectingLongPress,
                        canLauncherInfoEnabled = { selectingLongPress || canLauncherInfoEnabled(selectedRecord, it, maxSelectCount) },
                        canShortcutInfoEnabled = { selectingLongPress || canShortcutInfoEnabled(selectedRecord, it, maxSelectCount) },
                        isShortcutInfoSelected = { selectedRecord.isSelected(it) },
                        onSelect = { s, sel ->
                            if (selectingLongPress) onSelectLongPress(s) else onSelectShortcut(s, sel)
                        }, onClick = {})
                }
            }
        }
        if (selectedType == null && query.isBlank() && onOpenAppOrUrl != null) {
            item(key = "open_app_or_url_card") {
                Surface(onClick = onOpenAppOrUrl, shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = ContentPaddingHorizontal * 2, vertical = 8.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(MinIconSize))
                        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                            Text(stringResource(R.string.open_app_or_url_card_title), style = MaterialTheme.typography.bodyLarge)
                            Text(stringResource(R.string.open_app_or_url_card_desc), style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ActionItem(
    onSelect: (Boolean) -> Unit,
    selected: Boolean,
    action: Action,
    selectSingle: Boolean,
    snackbarHostState: SnackbarHostState,
    enabled: Boolean = true,
    showSettings: Boolean = false,
    onSettingsClick: (() -> Unit)? = null
) {
    val def = ActionCatalog.byId(action.value)
    val settingHintText = def?.let { actionSettingHintResMap[it.configKind]?.let { res -> stringResource(res) } }
    Row(
        modifier = Modifier
            .graphicsLayer {
                alpha = if (enabled) 1f else SettingsUiDefaults.DisabledAlpha
            }
            .fillMaxWidth()
            .heightIn(min = MinInteractiveSize)
            .onClick(enabled = enabled) {
                onSelect(!selected)
            }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val context = LocalContext.current
        val icon = actionIcon(action)
        Box(
            modifier = Modifier
                .padding(start = ContentPaddingHorizontal * 2)
                .size(MinIconSize)
        ) {
            if (icon is ImageVector) {
                Image(
                    imageVector = icon,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
            } else {
                AsyncImage(
                    model = icon,
                    contentDescription = null,
                    imageLoader = context.imageLoader,
                    contentScale = ContentScale.Crop,
                    colorFilter = null
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(horizontal = ItemPadding)
                .weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ItemPadding)
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f, false)
                        .basicMarquee(velocity = 50.dp),
                    text = actionText(action = action, emptyIfNone = false),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (showSettings) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .combinedClickable(
                                enabled = enabled,
                                onClick = {
                                    onSettingsClick?.invoke()
                                },
                                onLongClick = if (settingHintText != null) {
                                    { showToast(settingHintText) }
                                } else null
                            )
                            .clipToBackground(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            val descRes = def?.let { actionDescResMap[it.titleKey] }
            if (descRes != null) {
                Text(
                    text = stringResource(descRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
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

@Composable
internal fun SelectedActionSettings(
    selectedItems: List<Any>,
    longPressTargetIndex: Int?,
    itemLabel: (Any) -> String,
    onSetLongPress: (Int) -> Unit,
    onClearLongPress: (Int) -> Unit,
    onCancelLongPress: () -> Unit,
    onMoveSelected: (Int, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ContentPaddingHorizontal * 2, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.selected_action_settings),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (longPressTargetIndex != null) {
                TextButton(onClick = onCancelLongPress) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
        selectedItems.forEachIndexed { index, item ->
            val action = item as? Action
            val longPressAction = action?.longPressAction
            val shortPressText = itemLabel(item)
            val longPressText = if (longPressAction != null) {
                LocalContext.current.actionText(longPressAction, emptyIfNone = false)
            } else {
                stringResource(R.string.long_press_action_fallback)
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (longPressTargetIndex == index) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${index + 1}. ${shortPressText.take(2)} / ${longPressText.take(2)}",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (longPressAction != null) {
                            TextButton(
                                onClick = { onClearLongPress(index) },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                            ) {
                                Text(stringResource(R.string.clear_long_press_action))
                            }
                        } else {
                            TextButton(
                                onClick = { onSetLongPress(index) },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                            ) {
                                Text(stringResource(R.string.set_long_press_action))
                            }
                        }
                        IconButton(
                            enabled = index > 0,
                            onClick = { onMoveSelected(index, index - 1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = stringResource(R.string.move_up))
                        }
                        IconButton(
                            enabled = index < selectedItems.lastIndex,
                            onClick = { onMoveSelected(index, index + 1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(R.string.move_down))
                        }
                    }
                    if (longPressTargetIndex == index) {
                        Text(
                            text = stringResource(R.string.choose_long_press_action_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SelectedBar(
    selectedItems: List<Any>,
    maxSelectCount: Int,
    showMaxSelectCount: Boolean,
    itemLabel: (Any) -> String,
    onRemoveItem: (Any) -> Unit,
    onClearAll: () -> Unit,
) {
    if (selectedItems.isEmpty()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ContentPaddingHorizontal * 2, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (showMaxSelectCount) {
                stringResource(R.string.selected_count, selectedItems.size, maxSelectCount)
            } else {
                stringResource(R.string.selected_count_no_limit, selectedItems.size)
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        LazyRow(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(selectedItems, key = { it.hashCode() }) { item ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(start = 10.dp, end = 2.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = itemLabel(item),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall
                        )
                        IconButton(
                            onClick = { onRemoveItem(item) },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
        TextButton(
            onClick = onClearAll,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(stringResource(R.string.clear_all))
        }
    }
}

private fun Context.selectedItemLabel(item: Any): String {
    return when (item) {
        is Action -> actionText(item, emptyIfNone = false)
        is AppInfo -> item.label
        is LauncherInfo.ShortcutInfo -> item.label
        else -> ""
    }
}
