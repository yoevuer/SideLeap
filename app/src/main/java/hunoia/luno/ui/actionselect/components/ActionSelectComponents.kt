package hunoia.luno.ui.actionselect

import android.content.Context
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import com.aaron.compose.ktx.clipToBackground
import hunoia.luno.action.api.ActionFacade
import hunoia.luno.action.definition.ActionCategory
import hunoia.luno.action.definition.ActionConfigKind
import hunoia.luno.bridge.feedback.showToast
import hunoia.luno.config.defaults.SettingsUiDefaults
import hunoia.luno.config.model.Action
import hunoia.luno.config.model.SubGesture
import hunoia.luno.quicklaunch.model.AppInfo
import hunoia.luno.quicklaunch.model.LauncherInfo
import hunoia.luno.quicklaunch.model.qualifiedName
import hunoia.luno.R
import hunoia.luno.ui.actionselect.actionTextWithSubGesture
import hunoia.luno.ui.component.AppSearchBar
import hunoia.luno.ui.component.EmptyState
import hunoia.luno.ui.component.actionIcon
import hunoia.luno.ui.component.displayNameRes
import hunoia.luno.ui.actionselect.UiState.SelectedRecord
import hunoia.luno.ui.theme.*


val actionTitleResMap: Map<String, Int> = mapOf(
    "none" to R.string.action_none,
    "back" to R.string.action_back,
    "home" to R.string.action_home,
    "recent" to R.string.action_recent,
    "volume_up" to R.string.action_volume_up,
    "volume_down" to R.string.action_volume_down,
    "mute" to R.string.action_mute,
    "play_pause" to R.string.action_play_pause_song,
    "last_song" to R.string.action_last_song,
    "next_song" to R.string.action_next_song,
    "previous_app" to R.string.action_previous_app,
    "open_notification" to R.string.action_open_notification_panel,
    "open_quick_settings" to R.string.action_open_quick_panel,
    "lock_screen" to R.string.action_lock_screen,
    "flashlight" to R.string.action_flashlight,
    "assist_app" to R.string.action_assist_app,
    "screenshot" to R.string.action_screenshot,
    "power_button" to R.string.action_power_button,
    "keep_screen_on" to R.string.action_keep_screen_on,
    "popup_screen" to R.string.action_popup_screen,
    "back_to_top" to R.string.action_back_to_top,
    "goto_bottom" to R.string.action_goto_bottom,
    "pointer" to R.string.action_pointer,
    "volume_scrub" to R.string.action_volume_scrub,
    "shell_command" to R.string.action_shell_command,
    "open_activity" to R.string.action_open_activity,
    "open_url" to R.string.action_open_url,
    "quick_app_launcher" to R.string.action_quick_app_panel,
    "random_name" to R.string.action_random_name,
    "one_key_freeze" to R.string.action_one_key_freeze_apps,
    "generate_password_copy" to R.string.action_generate_password_copy,
    "open_password_generator" to R.string.action_open_password_generator,
    "sub_gesture" to R.string.action_sub_gesture,
)

val actionDescResMap: Map<String, Int> = mapOf(
    "back" to R.string.action_desc_back,
    "home" to R.string.action_desc_home,
    "recent" to R.string.action_desc_recent,
    "open_notification" to R.string.action_desc_notification,
    "lock_screen" to R.string.action_desc_lock_screen,
    "flashlight" to R.string.action_desc_flashlight,
    "screenshot" to R.string.action_desc_screenshot,
    "open_activity" to R.string.action_desc_open_activity,
    "open_url" to R.string.action_desc_open_url,
    "quick_app_launcher" to R.string.action_desc_quick_launcher,
    "pointer" to R.string.action_desc_pointer,
    "volume_scrub" to R.string.action_desc_volume_scrub,
    "shell_command" to R.string.action_desc_shell_command,
    "sub_gesture" to R.string.action_desc_sub_gesture,
)

val actionSettingHintResMap: Map<ActionConfigKind, Int> = mapOf(
    ActionConfigKind.PREVIOUS_APP to R.string.action_setting_hint_previous_app,
    ActionConfigKind.GOTO_BOTTOM to R.string.action_setting_hint_goto_bottom,
    ActionConfigKind.OPEN_APP_OR_URL to R.string.action_setting_hint_open_activity,
    ActionConfigKind.SHELL_COMMAND to R.string.action_setting_hint_shell_command,
    ActionConfigKind.POINTER to R.string.action_setting_hint_pointer,
    ActionConfigKind.VOLUME_SCRUB to R.string.action_setting_hint_volume_scrub,
)

val actionPermissionHintResMap: Map<String, Int> = mapOf(
    "flashlight" to R.string.action_permission_hint_flashlight,
    "screenshot" to R.string.action_permission_hint_screenshot,
    "power_button" to R.string.action_permission_hint_power_button,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActionItem(
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
    val def = ActionFacade.byId(action.value)
    val settingHintText = def?.let { actionSettingHintResMap[it.configKind]?.let { res -> stringResource(res) } }
    Surface(
        modifier = Modifier
            .alpha(if (enabled) 1f else SettingsUiDefaults.DisabledAlpha)
            .fillMaxWidth()
            .padding(horizontal = Spacing12, vertical = Spacing4),
        onClick = { onSelect(!selected) },
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MinInteractiveSize)
                .padding(vertical = Spacing8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current
            val icon = actionIcon(action)
            Surface(
                modifier = Modifier
                    .padding(start = ContentPaddingHorizontal)
                    .size(Spacing40),
                shape = MaterialTheme.shapes.medium,
                color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(modifier = Modifier.padding(Spacing8), contentAlignment = Alignment.Center) {
                    if (icon is ImageVector) {
                        Image(
                            imageVector = icon,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(
                                if (selected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onPrimaryContainer
                            )
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
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (showSettings) {
                        Box(
                            modifier = Modifier
                                .size(Spacing32)
                                .combinedClickable(
                                    enabled = enabled,
                                    onClick = { onSettingsClick?.invoke() },
                                    onLongClick = if (settingHintText != null) {
                                        { showToast(settingHintText) }
                                    } else null
                                )
                                .clipToBackground(
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                modifier = Modifier.size(Spacing20),
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
}

@Composable
fun SelectedBar(
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
            .padding(horizontal = ContentPaddingHorizontal * 2, vertical = Spacing4),
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
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = Spacing2)
        ) {
            Text(stringResource(R.string.clear_all))
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ActionPage(
    onSettingsClick: (Action) -> Unit,
    onSelect: (Action, Boolean) -> Unit,
    onSelectLongPress: (Any) -> Unit = {},
    onSelectApp: (AppInfo, Boolean) -> Unit,
    onSelectShortcut: (LauncherInfo.ShortcutInfo, Boolean) -> Unit,
    onSetLongPress: (Int) -> Unit = {},
    onClearLongPress: (Int) -> Unit = {},
    onCancelLongPress: () -> Unit = {},
    onMoveSelected: (Int, Int) -> Unit = { _, _ -> },
    onAppLongClick: (AppInfo) -> Unit,
    onShortcutClick: (LauncherInfo) -> Unit = {},
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
    permissionState: hunoia.luno.ui.permission.PermissionState,
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
            add(ActionCategory.NAVIGATION to context.getString(ActionCategory.NAVIGATION.displayNameRes))
            add(ActionCategory.SYSTEM to context.getString(ActionCategory.SYSTEM.displayNameRes))
            add(ActionCategory.TOOL to context.getString(ActionCategory.TOOL.displayNameRes))
            add(ActionCategory.SUB_GESTURE to context.getString(ActionCategory.SUB_GESTURE.displayNameRes))
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
                    val cat = ActionFacade.byId(action.value)?.category ?: ActionCategory.TOOL
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
                result = result.filter { it.value == ActionFacade.SUB_GESTURE }
            }
            if (selectedCategory != null) {
                result = result.filter { action ->
                    val cat = ActionFacade.byId(action.value)?.category ?: ActionCategory.TOOL
                    cat == selectedCategory
                }
            }
            result
        }
    }
    val grouped = remember(filteredActions) {
        val map = LinkedHashMap<ActionCategory, MutableList<Action>>()
        filteredActions.forEach { action ->
            val category = ActionFacade.byId(action.value)?.category ?: ActionCategory.TOOL
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
                    .padding(horizontal = ContentPaddingHorizontal * 2, vertical = Spacing4),
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
                        shape = RoundedCornerShape(Spacing16),
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = Spacing12, vertical = Spacing6),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            text = stringResource(id = category.displayNameRes),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            showSettings = ActionFacade.hasConfig(item.value),
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
                    Text(stringResource(R.string.create_shortcut), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    Text(stringResource(R.string.launch_shortcut), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
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




