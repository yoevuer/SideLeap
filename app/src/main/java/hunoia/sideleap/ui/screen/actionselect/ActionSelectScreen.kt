package hunoia.sideleap.ui.screen.actionselect

import android.app.Activity
import android.content.Intent
import android.content.Intent.ShortcutIconResource
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import hunoia.sideleap.system.feedback.showToast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.Checkbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.imageLoader
import com.aaron.compose.component.UDFComponent
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.onClick
import com.aaron.compose.ktx.onSingleClick
import hunoia.sideleap.R
import hunoia.sideleap.constant.GlobalActions
import hunoia.sideleap.constant.GlobalSettings
import hunoia.sideleap.action.Action
import hunoia.sideleap.launcher.model.AppInfo
import hunoia.sideleap.ui.navigation.IconResize
import hunoia.sideleap.launcher.model.LauncherInfo
import hunoia.sideleap.action.OpenAppOrUrlData
import hunoia.sideleap.ktx.actionIcon
import hunoia.sideleap.ktx.actionText
import hunoia.sideleap.ktx.deniedForever
import hunoia.sideleap.ktx.gotoAppDetailSettings
import hunoia.sideleap.launcher.ext.icon
import hunoia.sideleap.system.intent.launchUrl
import hunoia.sideleap.system.intent.normalizeOpenAppOrUrl
import hunoia.sideleap.launcher.ext.qualifiedName
import hunoia.sideleap.system.packages.queryIntentActivitiesCompat
import hunoia.sideleap.ktx.rememberGetInstalledAppsPermissionState
import hunoia.sideleap.action.definition.ActionCatalog
import hunoia.sideleap.action.definition.ActionCategory
import hunoia.sideleap.ui.screen.actionselect.ActionSelectVM.UiEvent
import hunoia.sideleap.ui.screen.actionselect.ActionSelectVM.UiState.SelectedRecord
import hunoia.sideleap.ui.theme.ContentPaddingHorizontal
import hunoia.sideleap.ui.theme.ContentPaddingVertical
import hunoia.sideleap.ui.theme.IconTextPadding
import hunoia.sideleap.ui.theme.ItemPadding
import hunoia.sideleap.ui.theme.MinIconSize
import hunoia.sideleap.ui.theme.MinInteractiveSize
import hunoia.sideleap.ui.theme.ScrollBottomPadding
import hunoia.sideleap.ui.theme.SubMinInteractiveSize
import hunoia.sideleap.ui.theme.TopBarPaddingExtra
import hunoia.sideleap.ui.dialog.OpenAppOrUrlSettingsContent
import hunoia.sideleap.ui.widget.ActionSettingsDialog
import hunoia.sideleap.ui.widget.MySnackbarHost
import hunoia.sideleap.ui.widget.TopBar
import hunoia.sideleap.utils.JsonHelper
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.os.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.OpenInNew
import net.sourceforge.pinyin4j.BasePinyinHelper


/**
 * @author aaronzzxup@gmail.com
 * @since 2024/12/2
 */

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ActionSelectScreen(
    onBack: () -> Unit,
    onNavToIconResize: (IconResize) -> Unit,
    vm: ActionSelectVM = viewModel()
) {
    UDFComponent(
        component = vm.udfComponent,
        onEvent = { event ->
            when (event) {
                is UiEvent.GotoIconResize -> onNavToIconResize(event.iconResize)
            }
        }
    ) { uiState ->
        var showOpenAppOrUrlDialog by remember { mutableStateOf(false) }

        if (uiState.actionSettingsDialog.show) {
            ActionSettingsDialog(
                onDismissRequest = { vm.actionSettingsDialog.show(false) },
                action = uiState.actionSettingsDialog.action,
                onActionDataChanged = { vm.updateActionData(uiState.actionSettingsDialog.action, it) }
            )
        }

        if (showOpenAppOrUrlDialog && !uiState.actionSettingsDialog.show) {
            AlertDialog(
                onDismissRequest = { showOpenAppOrUrlDialog = false },
                containerColor = MaterialTheme.colorScheme.surface,
                title = { Text(stringResource(R.string.open_app_or_url_card_title)) },
                text = {
                    OpenAppOrUrlSettingsContent(
                        action = Action(value = GlobalActions.OPEN_APP_OR_URL, data = ""),
                        onConfirm = { data ->
                            vm.select(Action(value = GlobalActions.OPEN_APP_OR_URL, data = data), true)
                            showOpenAppOrUrlDialog = false
                        }
                    )
                },
                confirmButton = {},
                dismissButton = {}
            )
        }

        val snackbarHostState = remember { SnackbarHostState() }
        val pagerState = rememberPagerState { PAGES.size }
        val coroutineScope = rememberCoroutineScope()
        Scaffold(
            topBar = {
                TopBar(
                    onBack = onBack,
                    title = uiState.title,
                    actions = {
                        if (!uiState.selectSingle) {
                            IconButton(onClick = { vm.done() }) {
                                Icon(imageVector = Icons.Default.Done, contentDescription = null)
                            }
                        }
                    }
                )
            },
            snackbarHost = {
                MySnackbarHost(hostState = snackbarHostState)
            }
        ) { contentPadding ->
            Column(modifier = Modifier.padding(top = contentPadding.calculateTopPadding())) {

                val permissionState = rememberGetInstalledAppsPermissionState { granted ->
                    if (granted) {
                        vm.updateAppInfos()
                        vm.updateShortcutInfos()
                    }
                }
                LaunchedEffect(Unit) {
                    if (permissionState.status.isGranted) {
                        vm.updateAppInfos()
                        vm.updateShortcutInfos()
                    }
                }
                HorizontalPager(
                    modifier = Modifier.fillMaxSize(),
                    state = pagerState
                ) { page ->
                    when (page) {
                        PAGE_UNIFIED -> {
                            val context = LocalContext.current
                            var currentLauncherInfo: LauncherInfo? by remember { mutableStateOf(null) }
                            val shortcutLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                                coroutineScope.launch {
                                    val launcherInfo = currentLauncherInfo
                                    if (result.resultCode == Activity.RESULT_OK && launcherInfo != null) {
                                        val bitmap = result.data?.shortcutParcelableExtraCompat(shortcutIconExtraKey(), Bitmap::class.java)
                                        val shortcutIconRes = result.data?.shortcutParcelableExtraCompat(shortcutIconResourceExtraKey(), ShortcutIconResource::class.java)
                                        val intent = result.data?.shortcutParcelableExtraCompat(shortcutIntentExtraKey(), Intent::class.java)?.toUri(Intent.URI_INTENT_SCHEME)
                                        val label = result.data?.shortcutStringExtraCompat(shortcutNameExtraKey()).orEmpty()
                                        val iconRes = if (shortcutIconRes != null) {
                                            withContext(Dispatchers.IO) {
                                                val res = context.packageManager.getResourcesForApplication(shortcutIconRes.packageName)
                                                @Suppress("DiscouragedApi")
                                                res.getIdentifier(shortcutIconRes.resourceName, null, null)
                                            }
                                        } else 0
                                        val shortcutInfo = LauncherInfo.ShortcutInfo(
                                            packageName = launcherInfo.packageName, className = launcherInfo.className,
                                            intents = intent?.let { listOf(it) } ?: emptyList(), label = label,
                                            iconRes = iconRes, iconPath = null, iconBitmap = bitmap
                                        )
                                        vm.addNewShortcut(launcherInfo, shortcutInfo)
                                        if (uiState.selectedRecord.size < MAX_SELECT_COUNT) vm.select(shortcutInfo, true)
                                    }
                                    currentLauncherInfo = null
                                }
                            }
                            ActionPage(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = run {
                                    val direction = LocalLayoutDirection.current
                                    PaddingValues(
                                        start = contentPadding.calculateStartPadding(direction),
                                        end = contentPadding.calculateEndPadding(direction),
                                        bottom = contentPadding.calculateBottomPadding() + ScrollBottomPadding
                                    )
                                },
                                actions = uiState.actions,
                                appInfos = uiState.apps,
                                createShortcuts = uiState.createShortcuts,
                                launchShortcuts = uiState.launchShortcuts,
                                selectedRecord = uiState.selectedRecord,
                                selectSingle = uiState.selectSingle,
                                snackbarHostState = snackbarHostState,
                                permissionState = permissionState,
                                onSelect = { action, selected -> vm.select(action, selected) },
                                onSettingsClick = { action -> vm.actionSettingsDialog.show(true, action) },
                                onSelectApp = { appInfo, selected -> vm.select(appInfo, selected) },
                                onSelectShortcut = { shortcutInfo, selected -> vm.select(shortcutInfo, selected) },
                                onAppLongClick = { appInfo -> vm.toggleMiniWindow(appInfo) },
                                onShortcutClick = { launcherInfo ->
                                    try {
                                        currentLauncherInfo = launcherInfo
                                        shortcutLauncher.launch(Intent().apply { setClassName(launcherInfo.packageName, launcherInfo.className) })
                                    } catch (ignored: Exception) { currentLauncherInfo = null }
                                },
                                onOpenAppOrUrl = { showOpenAppOrUrlDialog = true }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun <T : android.os.Parcelable> Intent.shortcutParcelableExtraCompat(
    key: String,
    clazz: Class<T>
): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(key, clazz)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(key)
    }
}

private fun Intent.shortcutStringExtraCompat(key: String): String? {
    @Suppress("DEPRECATION")
    return getStringExtra(key)
}

@Suppress("DEPRECATION")
private fun shortcutIconExtraKey(): String = Intent.EXTRA_SHORTCUT_ICON

@Suppress("DEPRECATION")
private fun shortcutIconResourceExtraKey(): String = Intent.EXTRA_SHORTCUT_ICON_RESOURCE

@Suppress("DEPRECATION")
private fun shortcutIntentExtraKey(): String = Intent.EXTRA_SHORTCUT_INTENT

@Suppress("DEPRECATION")
private fun shortcutNameExtraKey(): String = Intent.EXTRA_SHORTCUT_NAME

@OptIn(ExperimentalPermissionsApi::class, ExperimentalFoundationApi::class)
@Composable
private fun ActionPage(
    onSettingsClick: (Action) -> Unit,
    onSelect: (Action, Boolean) -> Unit,
    onSelectApp: (AppInfo, Boolean) -> Unit,
    onSelectShortcut: (LauncherInfo.ShortcutInfo, Boolean) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    onShortcutClick: (LauncherInfo) -> Unit,
    modifier: Modifier = Modifier,
    onOpenAppOrUrl: (() -> Unit)? = null,
    actions: List<Action>,
    appInfos: List<AppInfo>,
    createShortcuts: List<LauncherInfo>,
    launchShortcuts: List<LauncherInfo>,
    selectedRecord: SelectedRecord,
    selectSingle: Boolean,
    snackbarHostState: SnackbarHostState,
    permissionState: PermissionState,
    contentPadding: PaddingValues = PaddingValues(),
    maxSelectCount: Int = MAX_SELECT_COUNT
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf<ActionCategory?>(null) }
    var selectedType by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val categoryChips = remember {
        listOf<Pair<Any?, String>>(
            null to context.getString(R.string.all_categories),
            ActionCategory.NONE to "默认/导航",
            ActionCategory.MEDIA to ActionCategory.MEDIA.displayName,
            ActionCategory.SYSTEM to ActionCategory.SYSTEM.displayName,
            ActionCategory.WINDOW to ActionCategory.WINDOW.displayName,
            ActionCategory.LAUNCHER to ActionCategory.LAUNCHER.displayName,
            ActionCategory.TOOL to ActionCategory.TOOL.displayName,
            "app" to context.getString(R.string.tab_apps),
            "shortcut" to context.getString(R.string.tab_shortcuts),
        )
    }
    val filteredActions = remember(actions, query, selectedCategory, selectedType) {
        if (query.isNotBlank()) {
            var result = actions
            if (selectedCategory != null) {
                result = result.filter { action ->
                    val cat = ActionCatalog.byId(action.value)?.category ?: ActionCategory.TOOL
                    when (selectedCategory) {
                        ActionCategory.NONE -> cat == ActionCategory.NONE || cat == ActionCategory.NAVIGATION
                        else -> cat == selectedCategory
                    }
                }
            }
            result = result.filter { context.actionText(it, emptyIfNone = false).contains(query, ignoreCase = true) }
            result
        } else if (selectedType == "app" || selectedType == "shortcut") emptyList()
        else {
            var result = actions
            if (selectedCategory != null) {
                result = result.filter { action ->
                    val cat = ActionCatalog.byId(action.value)?.category ?: ActionCategory.TOOL
                    when (selectedCategory) {
                        ActionCategory.NONE -> cat == ActionCategory.NONE || cat == ActionCategory.NAVIGATION
                        else -> cat == selectedCategory
                    }
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
    val selectedActions = remember(selectedRecord.list) {
        selectedRecord.list.filterIsInstance<Action>()
    }
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
        modifier = modifier.fillMaxSize(),
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
        if (!selectSingle && selectedActions.isNotEmpty()) {
            item(key = "selected_bar") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ContentPaddingHorizontal * 2, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.selected_count, selectedActions.size, maxSelectCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    LazyRow(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(selectedActions, key = { it.value }) { action ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(start = 10.dp, end = 2.dp, top = 2.dp, bottom = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = actionText(action),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    IconButton(
                                        onClick = { onSelect(action, false) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "移除",
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    TextButton(
                        onClick = { selectedActions.toList().forEach { onSelect(it, false) } },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(stringResource(R.string.clear_all))
                    }
                }
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
                            selectSingle = selectSingle,
                            enabled = canActionEnabled(selectedRecord, item, maxSelectCount),
                            snackbarHostState = snackbarHostState,
                            onSelect = { selected ->
                                onSelect(item, selected)
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
                    AppItem(appInfo = item, selected = selectedRecord.isSelected(item), selectSingle = selectSingle,
                        enabled = canAppInfoEnabled(selectedRecord, item, maxSelectCount),
                        onSelect = { selected -> onSelectApp(item, selected) }, onLongClick = { onAppLongClick(item) })
                }
            }
            if (filteredCreateShortcuts.isNotEmpty()) {
                stickyHeader(key = "create_shortcuts") {
                    Text(stringResource(R.string.create_shortcut), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = ContentPaddingHorizontal * 2, vertical = 8.dp))
                }
                items(items = filteredCreateShortcuts, key = { "cs_${it.qualifiedName}" }) { item ->
                    LauncherInfoItem(launcherInfo = item, selectSingle = selectSingle,
                        canLauncherInfoEnabled = { canLauncherInfoEnabled(selectedRecord, it, maxSelectCount) },
                        canShortcutInfoEnabled = { canShortcutInfoEnabled(selectedRecord, it, maxSelectCount) },
                        isShortcutInfoSelected = { selectedRecord.isSelected(it) },
                        onSelect = { s, sel -> onSelectShortcut(s, sel) }, onClick = { onShortcutClick(item) })
                }
            }
            if (filteredLaunchShortcuts.isNotEmpty()) {
                stickyHeader(key = "launch_shortcuts") {
                    Text(stringResource(R.string.launch_shortcut), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = ContentPaddingHorizontal * 2, vertical = 8.dp))
                }
                items(items = filteredLaunchShortcuts, key = { "ls_${it.qualifiedName}" }) { item ->
                    LauncherInfoItem(launcherInfo = item, selectSingle = selectSingle,
                        canLauncherInfoEnabled = { canLauncherInfoEnabled(selectedRecord, it, maxSelectCount) },
                        canShortcutInfoEnabled = { canShortcutInfoEnabled(selectedRecord, it, maxSelectCount) },
                        isShortcutInfoSelected = { selectedRecord.isSelected(it) },
                        onSelect = { s, sel -> onSelectShortcut(s, sel) }, onClick = {})
                }
            }
        }
        if (selectedType == null && query.isBlank() && onOpenAppOrUrl != null) {
            item(key = "open_app_or_url_card") {
                Surface(onClick = onOpenAppOrUrl, shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = ContentPaddingHorizontal * 2, vertical = 8.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.OpenInNew, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(MinIconSize))
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
private fun ActionItem(
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
    val coroutineScope = rememberCoroutineScope()
    val settingHintText = def?.let { actionSettingHintResMap[it.configKind]?.let { res -> stringResource(res) } }
    Row(
        modifier = Modifier
            .graphicsLayer {
                alpha = if (enabled) 1f else GlobalSettings.DisabledAlpha
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
private fun SelectedBar(
    selectedItems: List<Any>,
    maxSelectCount: Int,
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
            text = stringResource(R.string.selected_count, selectedItems.size, maxSelectCount),
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun AppPage(
    onLongClick: (AppInfo) -> Unit,
    onSelect: (AppInfo, Boolean) -> Unit,
    appInfos: List<AppInfo>,
    selectedRecord: SelectedRecord,
    snackbarHostState: SnackbarHostState,
    permissionState: PermissionState,
    selectSingle: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    maxSelectCount: Int = MAX_SELECT_COUNT
) {
    val selectedApps = remember(selectedRecord.list) {
        selectedRecord.list.filterIsInstance<AppInfo>()
    }
    Box(modifier = modifier) {
        if (permissionState.status.isGranted) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding
            ) {
                if (appInfos.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_available_apps),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    if (!selectSingle && selectedApps.isNotEmpty()) {
                        item(key = "selected_bar") {
                            SelectedBar(
                                selectedItems = selectedApps,
                                maxSelectCount = maxSelectCount,
                                itemLabel = { (it as AppInfo).label },
                                onRemoveItem = { appInfo -> onSelect(appInfo as AppInfo, false) },
                                onClearAll = { selectedApps.toList().forEach { onSelect(it, false) } }
                            )
                        }
                    }
                    items(
                        items = appInfos,
                        key = { it.qualifiedName }
                    ) { item ->
                        AppItem(
                            appInfo = item,
                            selected = selectedRecord.isSelected(item),
                            selectSingle = selectSingle,
                            enabled = canAppInfoEnabled(selectedRecord, item, maxSelectCount),
                            onSelect = { selected ->
                                onSelect(item, selected)
                            },
                            onLongClick = {
                                onLongClick(item)
                            }
                        )
                    }
                }
            }
        } else {
            PermissionPage(
                snackbarHostState = snackbarHostState,
                permissionState = permissionState
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalFoundationApi::class)
@Composable
private fun ShortcutPage(
    onClick: (LauncherInfo) -> Unit,
    onSelect: (LauncherInfo.ShortcutInfo, Boolean) -> Unit,
    createShortcuts: List<LauncherInfo>,
    launchShortcuts: List<LauncherInfo>,
    selectedRecord: SelectedRecord,
    snackbarHostState: SnackbarHostState,
    permissionState: PermissionState,
    selectSingle: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    maxSelectCount: Int = MAX_SELECT_COUNT
) {
    val selectedShortcuts = remember(selectedRecord.list) {
        selectedRecord.list.filterIsInstance<LauncherInfo.ShortcutInfo>()
    }
    Box(modifier = modifier) {
        if (permissionState.status.isGranted) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding
            ) {
                if (createShortcuts.isEmpty() && launchShortcuts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_available_shortcuts),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    if (!selectSingle && selectedShortcuts.isNotEmpty()) {
                        item(key = "selected_bar") {
                            SelectedBar(
                                selectedItems = selectedShortcuts,
                                maxSelectCount = maxSelectCount,
                                itemLabel = { (it as LauncherInfo.ShortcutInfo).label },
                                onRemoveItem = { shortcutInfo -> onSelect(shortcutInfo as LauncherInfo.ShortcutInfo, false) },
                                onClearAll = { selectedShortcuts.toList().forEach { onSelect(it, false) } }
                            )
                        }
                    }
                    if (createShortcuts.isNotEmpty()) {
                        stickyHeader {
                            Text(
                                modifier = Modifier
                                    .background(color = MaterialTheme.colorScheme.background)
                                    .fillMaxWidth()
                                    .padding(vertical = ContentPaddingVertical)
                                    .padding(horizontal = ContentPaddingHorizontal * 2),
                                text = stringResource(R.string.create_shortcut),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    items(
                        items = createShortcuts,
                        key = { it.qualifiedName }
                    ) { item ->
                        LauncherInfoItem(
                            launcherInfo = item,
                            selectSingle = selectSingle,
                            canLauncherInfoEnabled = { canLauncherInfoEnabled(selectedRecord, it, maxSelectCount) },
                            canShortcutInfoEnabled = { canShortcutInfoEnabled(selectedRecord, it, maxSelectCount) },
                            isShortcutInfoSelected = { shortcutInfo ->
                                selectedRecord.isSelected(shortcutInfo)
                            },
                            onSelect = { shortcutInfo, selected ->
                                onSelect(shortcutInfo, selected)
                            },
                            onClick = {
                                onClick(item)
                            }
                        )
                    }
                    if (launchShortcuts.isNotEmpty()) {
                        stickyHeader {
                            Text(
                                modifier = Modifier
                                    .background(color = MaterialTheme.colorScheme.background)
                                    .fillMaxWidth()
                                    .padding(vertical = ContentPaddingVertical)
                                    .padding(horizontal = ContentPaddingHorizontal * 2),
                                text = stringResource(R.string.launch_shortcut),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    items(
                        items = launchShortcuts,
                        key = { it.qualifiedName }
                    ) { item ->
                        LauncherInfoItem(
                            launcherInfo = item,
                            selectSingle = selectSingle,
                            canLauncherInfoEnabled = { canLauncherInfoEnabled(selectedRecord, it, maxSelectCount) },
                            canShortcutInfoEnabled = { canShortcutInfoEnabled(selectedRecord, it, maxSelectCount) },
                            isShortcutInfoSelected = { shortcutInfo ->
                                selectedRecord.isSelected(shortcutInfo)
                            },
                            onSelect = { shortcutInfo, selected ->
                                onSelect(shortcutInfo, selected)
                            },
                            onClick = {
                            }
                        )
                    }
                }
            }
        } else {
            PermissionPage(
                snackbarHostState = snackbarHostState,
                permissionState = permissionState
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionPage(
    snackbarHostState: SnackbarHostState,
    permissionState: PermissionState
) {
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppItem(
    onLongClick: () -> Unit,
    onSelect: (Boolean) -> Unit,
    selected: Boolean,
    appInfo: AppInfo,
    selectSingle: Boolean,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .graphicsLayer {
                alpha = if (enabled) 1f else GlobalSettings.DisabledAlpha
            }
            .fillMaxWidth()
            .combinedClickable(
                enabled = enabled,
                onLongClick = onLongClick,
                onClick = {
                    onSelect(!selected)
                }
            )
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (appInfo.miniWindow) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = Icons.Default.Window,
                        contentDescription = null
                    )
                }
                Text(
                    modifier = Modifier.weight(1f),
                    text = appInfo.label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = appInfo.packageName,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium
            )
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
private fun LauncherInfoItem(
    canLauncherInfoEnabled: (LauncherInfo) -> Boolean,
    canShortcutInfoEnabled: (LauncherInfo.ShortcutInfo) -> Boolean,
    isShortcutInfoSelected: (LauncherInfo.ShortcutInfo) -> Boolean,
    onClick: () -> Unit,
    onSelect: (LauncherInfo.ShortcutInfo, Boolean) -> Unit,
    launcherInfo: LauncherInfo,
    selectSingle: Boolean
) {
    Column(
        modifier = Modifier
            .graphicsLayer {
                alpha =
                    if (canLauncherInfoEnabled(launcherInfo)) 1f else GlobalSettings.DisabledAlpha
            }
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onClick(enabled = canLauncherInfoEnabled(launcherInfo)) {
                    onClick()
                }
                .padding(vertical = ContentPaddingVertical),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current
            AsyncImage(
                modifier = Modifier
                    .padding(start = ContentPaddingHorizontal * 2)
                    .size(MinInteractiveSize),
                model = launcherInfo.icon,
                contentDescription = null,
                imageLoader = context.imageLoader
            )
            Column(
                modifier = Modifier
                    .padding(start = IconTextPadding, end = ItemPadding)
                    .weight(1f)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = launcherInfo.label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = launcherInfo.packageName,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Column {
            launcherInfo.shortcuts.fastForEach { shortcutInfo ->
                key(shortcutInfo) {
                    val selected = isShortcutInfoSelected(shortcutInfo)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onClick(enabled = canShortcutInfoEnabled(shortcutInfo)) {
                                onSelect(shortcutInfo, !selected)
                            }
                        /*.padding(start = ContentPaddingHorizontal * 2 + MinInteractiveSize)*/,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val context = LocalContext.current
                        AsyncImage(
                            modifier = Modifier
                                .padding(start = ContentPaddingHorizontal * 3)
                                .size(SubMinInteractiveSize),
                            model = shortcutInfo.icon,
                            contentDescription = null,
                            imageLoader = context.imageLoader
                        )
                        Column(
                            modifier = Modifier
                                .padding(start = IconTextPadding, end = ItemPadding)
                                .weight(1f)
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = shortcutInfo.label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (!selectSingle) {
                            Checkbox(
                                modifier = Modifier.padding(end = TopBarPaddingExtra),
                                enabled = canShortcutInfoEnabled(shortcutInfo),
                                checked = selected,
                                onCheckedChange = { newSelected ->
                                    onSelect(shortcutInfo, newSelected)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun canActionEnabled(
    selectedRecord: SelectedRecord,
    item: Action,
    maxSelectCount: Int
): Boolean {
    val isMoveScreenSelected = selectedRecord.list.find {
        (it as? Action)?.value == GlobalActions.MOVE_SCREEN
    } != null
    val isMoveScreenAction = item.value == GlobalActions.MOVE_SCREEN
    if (isMoveScreenSelected && !isMoveScreenAction) {
        return false
    } else if (!isMoveScreenSelected &&
        selectedRecord.list.isNotEmpty() &&
        isMoveScreenAction
    ) {
        return false
    }

    // 走完移动屏幕的检查后再走其他
    return !(selectedRecord.size >= maxSelectCount && !selectedRecord.isSelected(item))
}

private fun canAppInfoEnabled(
    selectedRecord: SelectedRecord,
    item: AppInfo,
    maxSelectCount: Int
): Boolean {
    val isMoveScreenSelected = selectedRecord.list.find {
        (it as? Action)?.value == GlobalActions.MOVE_SCREEN
    } != null
    if (isMoveScreenSelected) {
        return false
    }

    // 走完移动屏幕的检查后再走其他
    return !(selectedRecord.size >= maxSelectCount && !selectedRecord.isSelected(item))
}

private fun canLauncherInfoEnabled(
    selectedRecord: SelectedRecord,
    item: LauncherInfo,
    maxSelectCount: Int
): Boolean {
    val isMoveScreenSelected = selectedRecord.list.find {
        (it as? Action)?.value == GlobalActions.MOVE_SCREEN
    } != null
    if (isMoveScreenSelected) {
        return false
    }

    // 走完移动屏幕的检查后再走其他
    return !(selectedRecord.size >= maxSelectCount && !item.shortcuts.any { selectedRecord.isSelected(it) })
}

private fun canShortcutInfoEnabled(
    selectedRecord: SelectedRecord,
    item: LauncherInfo.ShortcutInfo,
    maxSelectCount: Int
): Boolean {
    val isMoveScreenSelected = selectedRecord.list.find {
        (it as? Action)?.value == GlobalActions.MOVE_SCREEN
    } != null
    if (isMoveScreenSelected) {
        return false
    }

    // 走完移动屏幕的检查后再走其他
    return !(selectedRecord.size >= maxSelectCount && !selectedRecord.isSelected(item))
}

private const val MAX_SELECT_COUNT = 5

private const val PAGE_UNIFIED = 0

private val PAGES = listOf(PAGE_UNIFIED)
