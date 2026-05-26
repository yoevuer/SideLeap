package hunoia.luno.ui.screen.actionselect

import android.content.Context
import hunoia.luno.ui.component.AppSearchBar
import hunoia.luno.ui.component.EmptyState
import androidx.compose.foundation.ExperimentalFoundationApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.onClick
import hunoia.luno.R
import kotlin.math.roundToInt
import hunoia.luno.settings.defaults.SettingsUiDefaults
import hunoia.luno.action.Action
import hunoia.luno.action.GlobalActions
import hunoia.luno.action.display.actionIcon
import hunoia.luno.action.display.actionText
import hunoia.luno.action.payload.SubGestureActionData
import hunoia.luno.action.definition.ActionCatalog
import hunoia.luno.action.definition.ActionCategory
import hunoia.luno.core.serialization.JsonHelper
import hunoia.luno.launcher.model.qualifiedName
import hunoia.luno.launcher.model.AppInfo
import hunoia.luno.launcher.model.LauncherInfo
import hunoia.luno.settings.model.SubGesture
import hunoia.luno.system.feedback.showToast
import hunoia.luno.ui.screen.actionselect.ActionSelectVM.UiState.SelectedRecord
import hunoia.luno.ui.theme.ContentPaddingHorizontal
import hunoia.luno.ui.theme.IconTextPadding
import hunoia.luno.ui.theme.ItemPadding
import hunoia.luno.ui.theme.MinIconSize
import hunoia.luno.ui.theme.MinInteractiveSize
import hunoia.luno.ui.theme.ScrollBottomPadding
import hunoia.luno.ui.theme.SubMinInteractiveSize
import hunoia.luno.ui.theme.TopBarPaddingExtra

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
    subGestures: List<SubGesture> = emptyList(),
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
            add(ActionCategory.SUB_GESTURE to ActionCategory.SUB_GESTURE.displayName)
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
            result = result.filter {
                context.actionTextWithSubGesture(it, subGestures, emptyIfNone = false)
                    .contains(query, ignoreCase = true)
            }
            result
        } else if (selectedType == "app" || selectedType == "shortcut" || selectedType == "selected") emptyList()
        else {
            var result = actions
            if (selectedType == "sub_gesture") {
                result = result.filter { it.value == GlobalActions.SUB_GESTURE }
            }
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
            AppSearchBar(
                query = query,
                onQueryChange = { query = it },
                modifier = Modifier.padding(horizontal = ContentPaddingHorizontal * 2, vertical = 8.dp),
                placeholder = stringResource(R.string.search_hint_all),
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
            item(key = "selected_action_settings") {
                SelectedActionSettings(
                    selectedItems = selectedItems,
                    longPressTargetIndex = longPressTargetIndex,
                    subGestures = subGestures,
                    itemLabel = { context.selectedItemLabel(it, subGestures) },
                    onSetLongPress = onSetLongPress,
                    onClearLongPress = onClearLongPress,
                    onCancelLongPress = onCancelLongPress,
                    onMoveSelected = onMoveSelected,
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
        }
        val hasAnyContent = grouped.isNotEmpty() || filteredApps.isNotEmpty() || filteredCreateShortcuts.isNotEmpty() || filteredLaunchShortcuts.isNotEmpty()
        if ((query.isNotEmpty() || selectedCategory != null || selectedType != null) && !hasAnyContent) {
            item {
                EmptyState(message = stringResource(R.string.no_matching_results))
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
                        key = { "${it.value}:${it.data}" }
                    ) { item ->
                        ActionItem(
                            action = item,
                            actionLabel = context.actionTextWithSubGesture(item, subGestures, emptyIfNone = false),
                            selected = selectedRecord.isSelected(item),
                            selectSingle = selectSingle || selectingLongPress,
                            enabled = selectingLongPress || canActionEnabled(selectedRecord, item, maxSelectCount),
                            snackbarHostState = snackbarHostState,
                            onSelect = { selected ->
                                if (selectingLongPress) onSelectLongPress(item) else onSelect(item, selected)
                            },
                            showSettings = ActionCatalog.hasConfig(item.value),
                            onSettingsClick = {
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

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ActionItem(
    onSelect: (Boolean) -> Unit,
    selected: Boolean,
    action: Action,
    actionLabel: String,
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
            .alpha(if (enabled) 1f else SettingsUiDefaults.DisabledAlpha)
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
                    text = actionLabel,
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
    subGestures: List<SubGesture>,
    itemLabel: (Any) -> String,
    onSetLongPress: (Int) -> Unit,
    onClearLongPress: (Int) -> Unit,
    onCancelLongPress: () -> Unit,
    onMoveSelected: (Int, Int) -> Unit,
    onRemoveItem: (Any) -> Unit,
    onClearAll: () -> Unit,
) {
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }
    var itemHeight by remember { mutableStateOf(0f) }
    val density = LocalDensity.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ContentPaddingHorizontal * 2, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.selected_count_no_limit, selectedItems.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.weight(1f))
            if (longPressTargetIndex != null) {
                TextButton(onClick = onCancelLongPress) {
                    Text(stringResource(R.string.cancel))
                }
            }
            TextButton(onClick = onClearAll) {
                Text(stringResource(R.string.clear_all))
            }
        }
        selectedItems.forEachIndexed { index, item ->
            val action = item as? Action
            val longPressAction = action?.longPressAction
            val shortPressText = itemLabel(item)
            val longPressText = if (longPressAction != null) {
                LocalContext.current.actionTextWithSubGesture(longPressAction, subGestures, emptyIfNone = false)
            } else {
                stringResource(R.string.long_press_action_fallback)
            }
            val isDragging = draggedIndex == index
            Surface(
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        if (itemHeight == 0f) itemHeight = coordinates.size.height.toFloat()
                    }
                    .graphicsLayer {
                        if (isDragging) {
                            translationY = dragOffset
                        }
                    }
                    .pointerInput(selectedItems.size) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggedIndex = index
                                dragOffset = 0f
                            },
                            onDrag = { change, dragAmount ->
                                if (draggedIndex == index) {
                                    change.consume()
                                    dragOffset += dragAmount.y
                                }
                            },
                            onDragEnd = {
                                draggedIndex?.let { from ->
                                    val spacingPx = with(density) { 6.dp.toPx() }
                                    val step = (itemHeight + spacingPx).coerceAtLeast(1f)
                                    val delta = (dragOffset / step).roundToInt()
                                    val to = (from + delta).coerceIn(0, selectedItems.lastIndex)
                                    if (to != from) onMoveSelected(from, to)
                                }
                                draggedIndex = null
                                dragOffset = 0f
                            },
                            onDragCancel = {
                                draggedIndex = null
                                dragOffset = 0f
                            }
                        )
                    },
                shape = RoundedCornerShape(12.dp),
                color = if (longPressTargetIndex == index) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${index + 1}. ${shortPressText} / ${longPressText}",
                        modifier = Modifier
                            .weight(1f)
                            .basicMarquee(velocity = 50.dp),
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
                        onClick = { onRemoveItem(item) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.delete),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            if (longPressTargetIndex == index) {
                Text(
                    text = stringResource(R.string.choose_long_press_action_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 34.dp)
                )
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
        Spacer(Modifier.weight(1f))
        TextButton(
            onClick = onClearAll,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(stringResource(R.string.clear_all))
        }
    }
}

private fun Context.selectedItemLabel(item: Any, subGestures: List<SubGesture>): String {
    return when (item) {
        is Action -> actionTextWithSubGesture(item, subGestures, emptyIfNone = false)
        is AppInfo -> item.label
        is LauncherInfo.ShortcutInfo -> item.label
        else -> ""
    }
}

private fun Context.actionTextWithSubGesture(
    action: Action,
    subGestures: List<SubGesture>,
    emptyIfNone: Boolean
): String {
    if (action.value != GlobalActions.SUB_GESTURE) {
        return actionText(action, emptyIfNone)
    }
    val data = runCatching {
        JsonHelper.decodeFromString<SubGestureActionData>(action.data)
    }.getOrNull() ?: return getString(R.string.action_sub_gesture)
    return subGestures.firstOrNull { it.id == data.id }?.name ?: getString(R.string.action_sub_gesture)
}
