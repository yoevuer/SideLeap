package hunoia.sideleap.ui.screen.actionselect

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.Intent.ShortcutIconResource
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import hunoia.sideleap.utils.showToast
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.Checkbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.imageLoader
import com.aaron.compose.component.LoadingComponent
import com.aaron.compose.component.UDFComponent
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.onClick
import com.aaron.compose.ktx.onSingleClick
import hunoia.sideleap.R
import hunoia.sideleap.constant.GlobalActions
import hunoia.sideleap.constant.GlobalSettings
import hunoia.sideleap.entity.Action
import hunoia.sideleap.entity.AppInfo
import hunoia.sideleap.ui.navigation.IconResize
import hunoia.sideleap.entity.LauncherInfo
import hunoia.sideleap.entity.OpenAppOrUrlData
import hunoia.sideleap.ktx.actionIcon
import hunoia.sideleap.ktx.actionText
import hunoia.sideleap.ktx.deniedForever
import hunoia.sideleap.ktx.gotoAppDetailSettings
import hunoia.sideleap.ktx.icon
import hunoia.sideleap.ktx.qualifiedName
import hunoia.sideleap.ktx.queryIntentActivitiesCompat
import hunoia.sideleap.ktx.rememberGetInstalledAppsPermissionState
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
        if (uiState.actionSettingsDialog.show) {
            ActionSettingsDialog(
                onDismissRequest = { vm.actionSettingsDialog.show(false) },
                action = uiState.actionSettingsDialog.action,
                onActionDataChanged = { vm.updateActionData(uiState.actionSettingsDialog.action, it) }
            )
        }

        val snackbarHostState = remember { SnackbarHostState() }
        val pagerState = rememberPagerState { PAGES.size }
        val coroutineScope = rememberCoroutineScope()
        Scaffold(
            topBar = {
                TopBar(
                    onBack = {
                        if (pagerState.currentPage == PAGE_OPEN_APP_OR_URL) {
                            coroutineScope.launch { pagerState.animateScrollToPage(PAGE_UNIFIED) }
                        } else {
                            onBack()
                        }
                    },
                    title = if (pagerState.currentPage == PAGE_OPEN_APP_OR_URL) stringResource(R.string.tab_open_app_or_url) else uiState.title,
                    actions = {
                        if (!uiState.selectSingle || pagerState.currentPage == PAGE_OPEN_APP_OR_URL) {
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
                                onOpenAppOrUrl = { coroutineScope.launch { pagerState.animateScrollToPage(PAGE_OPEN_APP_OR_URL) } }
                            )
                        }
                        PAGE_OPEN_APP_OR_URL -> {
                            OpenAppOrUrlPage(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = run {
                                    val direction = LocalLayoutDirection.current
                                    PaddingValues(
                                        start = contentPadding.calculateStartPadding(direction),
                                        end = contentPadding.calculateEndPadding(direction),
                                        bottom = contentPadding.calculateBottomPadding() + ScrollBottomPadding
                                    )
                                },
                                selectedRecord = uiState.selectedRecord,
                                selectSingle = uiState.selectSingle,
                                onSelect = { action, selected ->
                                    vm.select(action, selected)
                                },
                                onMoveSelected = { fromIndex, toIndex ->
                                    vm.moveSelectedAction(fromIndex, toIndex)
                                },
                                onUpdateActionData = { action, data ->
                                    vm.updateActionData(action, data)
                                },
                                maxSelectCount = MAX_SELECT_COUNT
                            )
                        }
                        /*PAGE_SHORTCUTS -> {
                            LoadingComponent(
                                modifier = Modifier.fillMaxSize(),
                                component = vm.loadingComponent
                            ) {
                                val context = LocalContext.current
                                val activities = remember(context) {
                                    AppInfoUtils.queryCreateShortcutActivities(context)
                                }
                                val shortcuts = remember(context) {
                                    ShortcutUtils.getAllAppsWithShortcut(context)
                                }
                                var currentLauncherInfo: LauncherInfo? by remember {
                                    mutableStateOf(null)
                                }
                                val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                                    val launcherInfo = currentLauncherInfo
                                    if (launcherInfo != null) {
                                        val intent = it.data?.getParcelableExtra<Intent>(Intent.EXTRA_SHORTCUT_INTENT)?.toUri(Intent.URI_INTENT_SCHEME)
                                        val shortcutInfo = LauncherInfo.ShortcutInfo(
                                            packageName = launcherInfo.packageName,
                                            className = launcherInfo.className,
                                            label = it.data?.getStringExtra(Intent.EXTRA_SHORTCUT_NAME) ?: "",
                                            iconRes = 0,
                                            intents = intent?.let { listOf(it) } ?: emptyList()
                                        )
                                        vm.addNewShortcut(launcherInfo, shortcutInfo)
                                        currentLauncherInfo = null
                                    }
                                }
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    stickyHeader {
                                        Text(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(color = MaterialTheme.colorScheme.primary)
                                                .padding(16.dp),
                                            text = "Create Shortcuts",
                                            color = Color.White
                                        )
                                    }
                                    items(items = activities) { launcherInfo ->
                                        Column {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .onSingleClick {
                                                        try {
                                                            currentLauncherInfo = launcherInfo
                                                            val intent = Intent().apply {
                                                                setClassName(
                                                                    launcherInfo.packageName,
                                                                    launcherInfo.className
                                                                )
                                                            }
                                                            launcher.launch(intent)
                                                        } catch (ignored: Exception) {
                                                            currentLauncherInfo = null
                                                        }
                                                    },
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                AsyncImage(
                                                    modifier = Modifier.size(40.dp),
                                                    model = launcherInfo.icon,
                                                    contentDescription = null
                                                )
                                                Text(text = launcherInfo.label)
                                            }

                                            launcherInfo.shortcuts.fastForEach { shortcutInfo ->
                                                Row(
                                                    modifier = Modifier
                                                        .padding(start = 16.dp)
                                                        .fillMaxWidth()
                                                        .onSingleClick {
                                                        },
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    AsyncImage(
                                                        modifier = Modifier.size(40.dp),
                                                        model = shortcutInfo.getIcon(context),
                                                        contentDescription = null
                                                    )
                                                    Text(text = shortcutInfo.label)
                                                }
                                            }
                                        }
                                    }
                                    stickyHeader {
                                        Text(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(color = MaterialTheme.colorScheme.primary)
                                                .padding(16.dp),
                                            text = "Go to Shortcuts",
                                            color = Color.White
                                        )
                                    }
                                    items(items = shortcuts) { item ->
                                        Column {
                                            Text(text = item.label)
                                            Text(text = item.packageName)
                                            item.shortcuts.fastForEach { shortcut ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .onSingleClick {
                                                            try {
                                                                val intents = shortcut
                                                                    .intents
                                                                    .map {
                                                                        Intent.parseUri(
                                                                            it,
                                                                            Intent.URI_INTENT_SCHEME
                                                                        )
                                                                    }
                                                                    .toTypedArray()
                                                                context.startActivities(intents)
                                                            } catch (ex: Exception) {
                                                                ex.printStackTrace()
                                                            }
                                                        },
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val icon = item.icon
                                                    AsyncImage(
                                                        modifier = Modifier.size(40.dp),
                                                        model = icon,
                                                        contentDescription = null
                                                    )
                                                    Column {
                                                        Text(text = "package: ${shortcut.packageName}")
                                                        Text(text = "label: ${shortcut.label}")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }*/
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
    val metaByValue = remember { actionMetaList.associateBy { it.action.value } }
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
                    val cat = metaByValue[action.value]?.category ?: ActionCategory.TOOL
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
                    val cat = metaByValue[action.value]?.category ?: ActionCategory.TOOL
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
            val category = metaByValue[action.value]?.category ?: ActionCategory.TOOL
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
        if (selectedActions.isNotEmpty()) {
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
                            showSettings = actionMetaByValue[item.value]?.hasSettings == true,
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

private val ActionCategory.displayName: String
    get() = when (this) {
        ActionCategory.NONE -> "默认"
        ActionCategory.NAVIGATION -> "导航"
        ActionCategory.MEDIA -> "媒体"
        ActionCategory.SYSTEM -> "系统"
        ActionCategory.WINDOW -> "窗口"
        ActionCategory.LAUNCHER -> "启动"
        ActionCategory.TOOL -> "工具"
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
    val meta = actionMetaByValue[action.value]
    val coroutineScope = rememberCoroutineScope()
    val settingHintText = meta?.settingHintRes?.let { stringResource(it) }
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
        val icon = actionMetaByValue[action.value]?.icon ?: actionIcon(action)
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
            if (meta?.descRes != null) {
                Text(
                    text = stringResource(meta!!.descRes),
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OpenAppOrUrlPage(
    onSelect: (Action, Boolean) -> Unit,
    onMoveSelected: (Int, Int) -> Unit,
    onUpdateActionData: (Action, String) -> Unit,
    selectedRecord: SelectedRecord,
    selectSingle: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    maxSelectCount: Int = MAX_SELECT_COUNT
) {
    val context = LocalContext.current
    val launcherApps = remember(context) { queryOpenAppOrUrlLauncherApps(context) }
    val launcherAppIcons = remember(launcherApps, context) {
        launcherApps.associate { item ->
            item.packageName to runCatching { context.packageManager.getApplicationIcon(item.packageName) }.getOrNull()
        }
    }
    val activityOptionsByPackage = remember(context, launcherApps) {
        launcherApps.associate { item ->
            item.packageName to queryOpenAppOrUrlActivityOptions(
                context = context,
                packageName = item.packageName,
                selectedActivityClassName = "",
                launcherClassName = item.launcherClassName
            )
        }
    }
    var selectedPackageName by remember { mutableStateOf("") }
    var appQuery by remember { mutableStateOf("") }
    var recentPackageNames by remember { mutableStateOf(emptyList<String>()) }
    LaunchedEffect(launcherApps) {
        if (selectedPackageName.isBlank() && launcherApps.isNotEmpty()) {
            selectedPackageName = launcherApps.first().packageName
        }
    }
    val selectedLauncherApp by remember(launcherApps, selectedPackageName) {
        derivedStateOf {
            launcherApps.firstOrNull { it.packageName == selectedPackageName }
                ?: launcherApps.firstOrNull()
        }
    }
    val activityOptions = remember(selectedPackageName, selectedLauncherApp?.launcherClassName) {
        activityOptionsByPackage[selectedPackageName]
            ?: queryOpenAppOrUrlActivityOptions(
                context = context,
                packageName = selectedPackageName,
                selectedActivityClassName = "",
                launcherClassName = selectedLauncherApp?.launcherClassName
            )
    }
    var selectedActivityClassName by remember { mutableStateOf("") }
    var activityQuery by remember { mutableStateOf("") }
    var recentActivityClassNames by remember { mutableStateOf(emptyList<String>()) }
    LaunchedEffect(activityOptions) {
        if (selectedActivityClassName.isBlank() || activityOptions.none { it.className == selectedActivityClassName }) {
            selectedActivityClassName = activityOptions.firstOrNull()?.className.orEmpty()
        }
    }
    var showAppMenu by remember { mutableStateOf(false) }
    var showActivityMenu by remember { mutableStateOf(false) }
    var appSearchMode by remember { mutableStateOf(false) }
    var activitySearchMode by remember { mutableStateOf(false) }
    var urlInput by remember { mutableStateOf("") }
    var editingActionData by remember { mutableStateOf<String?>(null) }
    var editingUrl by remember { mutableStateOf("") }
    var overflowMenuActionData by remember { mutableStateOf<String?>(null) }

    val selectedOpenAppOrUrlActions by remember(selectedRecord.list) {
        derivedStateOf {
            selectedRecord.list.withIndex()
                .mapNotNull { entry ->
                    val action = entry.value as? Action ?: return@mapNotNull null
                    if (action.value != GlobalActions.OPEN_APP_OR_URL) return@mapNotNull null
                    entry.index to action
                }
        }
    }
    val selectedOpenAppOrUrlActionOrder by remember(selectedOpenAppOrUrlActions) {
        derivedStateOf {
            selectedOpenAppOrUrlActions.withIndex().associate { (index, entry) ->
                entry.second.data to index
            }
        }
    }
    val orderedLauncherApps by remember(launcherApps, selectedPackageName, recentPackageNames) {
        derivedStateOf {
            prioritizeOpenAppOrUrlLauncherApps(
                items = launcherApps,
                selectedPackageName = selectedPackageName,
                recentPackageNames = recentPackageNames
            )
        }
    }
    val launcherSearchIndex by remember(orderedLauncherApps) {
        derivedStateOf { orderedLauncherApps.associateWith { buildSearchIndex(context, "${it.label} ${it.packageName}") } }
    }
    val filteredLauncherApps by remember(orderedLauncherApps, launcherSearchIndex, appQuery) {
        derivedStateOf {
            filterByQuery(orderedLauncherApps, appQuery) { launcherSearchIndex[it] }
        }
    }
    val orderedActivityOptions by remember(activityOptions, selectedActivityClassName, recentActivityClassNames) {
        derivedStateOf {
            prioritizeOpenAppOrUrlActivities(
                items = activityOptions,
                selectedActivityClassName = selectedActivityClassName,
                recentActivityClassNames = recentActivityClassNames
            )
        }
    }
    val activityTextByClassName by remember(activityOptions, selectedPackageName) {
        derivedStateOf {
            activityOptions.associate { item ->
                item.className to formatOpenAppOrUrlActivityText(item, selectedPackageName)
            }
        }
    }
    val selectedActivityText by remember(activityOptions, selectedActivityClassName, selectedPackageName) {
        derivedStateOf {
            activityTextByClassName[selectedActivityClassName].orEmpty().ifBlank { selectedActivityClassName }
        }
    }
    val activitySearchIndex by remember(orderedActivityOptions, activityTextByClassName) {
        derivedStateOf {
            orderedActivityOptions.associateWith {
                val activityText = activityTextByClassName[it.className].orEmpty()
                buildSearchIndex(context, "$activityText ${it.className}")
            }
        }
    }
    val filteredActivityOptions by remember(orderedActivityOptions, activitySearchIndex, activityQuery, selectedPackageName) {
        derivedStateOf {
            filterByQuery(orderedActivityOptions, activityQuery) { activitySearchIndex[it] }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(ItemPadding)
    ) {
        item {
            Box(
                modifier = Modifier.padding(
                    top = 8.dp,
                    start = OpenAppOrUrlHorizontalPadding,
                    end = OpenAppOrUrlHorizontalPadding
                )
            ) {
                OpenAppOrUrlActivitySection(
                    selectedLauncherApp = selectedLauncherApp,
                    selectedPackageName = selectedPackageName,
                    selectedActivityText = selectedActivityText,
                    activityOptionsByPackage = activityOptionsByPackage,
                    activityTextByClassName = activityTextByClassName,
                    selectedRecord = selectedRecord,
                    maxSelectCount = maxSelectCount,
                    showAppMenu = showAppMenu,
                    showActivityMenu = showActivityMenu,
                    appSearchMode = appSearchMode,
                    activitySearchMode = activitySearchMode,
                    appQuery = appQuery,
                    activityQuery = activityQuery,
                    orderedLauncherApps = orderedLauncherApps,
                    orderedActivityOptions = orderedActivityOptions,
                    filteredLauncherApps = filteredLauncherApps,
                    filteredActivityOptions = filteredActivityOptions,
                    launcherAppIcons = launcherAppIcons,
                    onShowAppMenuChange = { showAppMenu = it },
                    onShowActivityMenuChange = { showActivityMenu = it },
                    onAppSearchModeChange = { appSearchMode = it },
                    onActivitySearchModeChange = { activitySearchMode = it },
                    onAppQueryChange = { appQuery = it },
                    onActivityQueryChange = { activityQuery = it },
                    onAppFocusChange = {},
                    onActivityFocusChange = {},
                    onSelectPackage = { packageName, activityClassName ->
                        selectedPackageName = packageName
                        selectedActivityClassName = activityClassName
                        recentPackageNames = updateRecentItems(recentPackageNames, packageName)
                    },
                    onSelectActivity = { className ->
                        selectedActivityClassName = className
                        recentActivityClassNames = updateRecentItems(recentActivityClassNames, className)
                    },
                    onAddActivity = {
                        val newAction = Action(
                            value = GlobalActions.OPEN_APP_OR_URL,
                            data = JsonHelper.encodeToString(
                                OpenAppOrUrlData(
                                    type = OpenAppOrUrlData.TYPE_ACTIVITY,
                                    packageName = selectedPackageName.trim(),
                                    activityClassName = selectedActivityClassName.trim(),
                                    url = ""
                                )
                            )
                        )
                        if (selectedRecord.list.none {
                                it is Action &&
                                    it.value == GlobalActions.OPEN_APP_OR_URL &&
                                    it.data == newAction.data
                            }
                        ) {
                            onSelect(newAction, true)
                        }
                    },
                    onBatchAddActivities = { classNames ->
                        classNames.forEach { className ->
                            val newAction = Action(
                                value = GlobalActions.OPEN_APP_OR_URL,
                                data = JsonHelper.encodeToString(
                                    OpenAppOrUrlData(
                                        type = OpenAppOrUrlData.TYPE_ACTIVITY,
                                        packageName = selectedPackageName.trim(),
                                        activityClassName = className.trim(),
                                        url = ""
                                    )
                                )
                            )
                            if (selectedRecord.list.none {
                                    it is Action &&
                                        it.value == GlobalActions.OPEN_APP_OR_URL &&
                                        it.data == newAction.data
                                }
                            ) {
                                onSelect(newAction, true)
                            }
                        }
                    },
                    onCopyText = { text, label ->
                        copyToClipboard(context, label, text)
                    }
                )
            }
        }

        item {
            Box(modifier = Modifier.padding(horizontal = OpenAppOrUrlHorizontalPadding)) {
                OpenAppOrUrlLinkSection(
                    urlInput = urlInput,
                    maxSelectCount = maxSelectCount,
                    selectedRecord = selectedRecord,
                    onUrlInputChange = { urlInput = it },
                    onTestUrl = {
                        val normalized = normalizeUrl(urlInput)
                        if (normalized == null) {
                            showToast(R.string.invalid_url)
                            return@OpenAppOrUrlLinkSection
                        }
                        runCatching {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalized))
                            context.startActivity(intent)
                        }.onFailure {
                            showToast(R.string.launch_failed)
                        }
                    },
                    onAddUrl = {
                        val trimmedUrl = normalizeUrl(urlInput)
                        if (trimmedUrl == null) {
                            showToast(R.string.invalid_url)
                            return@OpenAppOrUrlLinkSection
                        }
                        val newAction = Action(
                            value = GlobalActions.OPEN_APP_OR_URL,
                            data = JsonHelper.encodeToString(
                                OpenAppOrUrlData(
                                    type = OpenAppOrUrlData.TYPE_URL,
                                    url = trimmedUrl
                                )
                            )
                        )
                        if (selectedRecord.list.none {
                                it is Action &&
                                    it.value == GlobalActions.OPEN_APP_OR_URL &&
                                    it.data == newAction.data
                            }
                        ) {
                            onSelect(newAction, true)
                            urlInput = ""
                        }
                    }
                )
            }
        }

        item {
            Text(
                modifier = Modifier.padding(horizontal = OpenAppOrUrlHorizontalPadding),
                text = stringResource(R.string.selected_items),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (selectedOpenAppOrUrlActions.isEmpty()) {
            item {
                Text(
                    modifier = Modifier.padding(horizontal = OpenAppOrUrlHorizontalPadding),
                    text = stringResource(R.string.no_selected_items)
                )
            }
        } else {
            items(
                items = selectedOpenAppOrUrlActions,
                key = { it.second.data }
            ) { (absoluteIndex, action) ->
                Box(modifier = Modifier.padding(horizontal = OpenAppOrUrlHorizontalPadding)) {
                    val selected = selectedRecord.isSelected(action)
                    val openAppOrUrlIndex = selectedOpenAppOrUrlActionOrder[action.data] ?: 0
                    val canMoveUp = openAppOrUrlIndex > 0
                    val canMoveDown = openAppOrUrlIndex in 0 until selectedOpenAppOrUrlActions.lastIndex
                    val isEditing = editingActionData == action.data
                    OpenAppOrUrlSelectedItem(
                        action = action,
                        selected = selected,
                        selectSingle = selectSingle,
                        selectedRecord = selectedRecord,
                        launcherApps = launcherApps,
                        absoluteIndex = absoluteIndex,
                        canMoveUp = canMoveUp,
                        canMoveDown = canMoveDown,
                        isEditing = isEditing,
                        editingUrl = editingUrl,
                        overflowMenuActionData = overflowMenuActionData,
                        onOverflowMenuActionDataChange = { overflowMenuActionData = it },
                        onSelect = onSelect,
                        onMoveSelected = onMoveSelected,
                        onUpdateActionData = onUpdateActionData,
                        onStartEditUrl = { data, url ->
                            editingActionData = data
                            editingUrl = url
                            overflowMenuActionData = null
                        },
                        onEditUrlChange = { editingUrl = it },
                        onCancelEdit = { editingActionData = null },
                        onSaveEdit = { newData ->
                            onUpdateActionData(action, newData)
                            editingActionData = null
                        }
                    )
                }
            }
        }
    }
}

private val OpenAppOrUrlHorizontalPadding = 16.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OpenAppOrUrlActivitySection(
    selectedLauncherApp: OpenAppOrUrlLauncherAppOption?,
    selectedPackageName: String,
    selectedActivityText: String,
    activityOptionsByPackage: Map<String, List<OpenAppOrUrlActivityOption>>,
    activityTextByClassName: Map<String, String>,
    selectedRecord: SelectedRecord,
    maxSelectCount: Int,
    showAppMenu: Boolean,
    showActivityMenu: Boolean,
    appSearchMode: Boolean,
    activitySearchMode: Boolean,
    appQuery: String,
    activityQuery: String,
    orderedLauncherApps: List<OpenAppOrUrlLauncherAppOption>,
    orderedActivityOptions: List<OpenAppOrUrlActivityOption>,
    filteredLauncherApps: List<OpenAppOrUrlLauncherAppOption>,
    filteredActivityOptions: List<OpenAppOrUrlActivityOption>,
    launcherAppIcons: Map<String, Drawable?>,
    onShowAppMenuChange: (Boolean) -> Unit,
    onShowActivityMenuChange: (Boolean) -> Unit,
    onAppSearchModeChange: (Boolean) -> Unit,
    onActivitySearchModeChange: (Boolean) -> Unit,
    onAppQueryChange: (String) -> Unit,
    onActivityQueryChange: (String) -> Unit,
    onAppFocusChange: (Boolean) -> Unit,
    onActivityFocusChange: (Boolean) -> Unit,
    onSelectPackage: (String, String) -> Unit,
    onSelectActivity: (String) -> Unit,
    onAddActivity: () -> Unit,
    onBatchAddActivities: (List<String>) -> Unit,
    onCopyText: (text: String, label: String) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var showAppSelector by remember { mutableStateOf(false) }
    var showActivitySelector by remember { mutableStateOf(false) }
    val canAddActivity = canOpenAppOrUrlEnabled(selectedRecord, maxSelectCount) &&
        selectedPackageName.isNotBlank() && selectedActivityText.isNotBlank()

    OpenAppOrUrlSectionCard(title = stringResource(R.string.mode_activity)) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onClick {
                    onAppQueryChange("")
                    showAppSelector = true
                },
            value = selectedLauncherApp?.let { "${it.label} (${it.packageName})" } ?: selectedPackageName,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.select_app)) },
            singleLine = true,
            trailingIcon = { Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null) }
        )

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onClick {
                    if (selectedPackageName.isNotBlank()) {
                        onActivityQueryChange("")
                        showActivitySelector = true
                    }
                },
            value = selectedActivityText,
            onValueChange = {},
            readOnly = true,
            enabled = selectedPackageName.isNotBlank(),
            label = { Text(stringResource(R.string.select_activity)) },
            singleLine = true,
            trailingIcon = { Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null) }
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(enabled = canAddActivity, onClick = onAddActivity) {
                Text(text = stringResource(R.string.add_activity))
            }
        }

        if (showAppSelector) {
            AlertDialog(
                onDismissRequest = { showAppSelector = false },
                confirmButton = {},
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(ItemPadding)) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = appQuery,
                            onValueChange = onAppQueryChange,
                            singleLine = true,
                            label = { Text(text = stringResource(R.string.search_app_hint)) }
                        )
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(ItemPadding)) {
                            val appList = filteredLauncherApps
                            if (appList.isEmpty()) {
                                item {
                                    Text(text = stringResource(R.string.no_matching_results))
                                }
                            } else {
                                items(appList, key = { it.packageName }) { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onClick {
                                                onSelectPackage(
                                                    item.packageName,
                                                    activityOptionsByPackage[item.packageName]?.firstOrNull()?.className.orEmpty()
                                                )
                                                onAppQueryChange("")
                                                onActivityQueryChange("")
                                                showAppSelector = false
                                                keyboardController?.hide()
                                            },
                                        horizontalArrangement = Arrangement.spacedBy(ItemPadding),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OpenAppOrUrlLauncherDropdownItem(item, launcherAppIcons[item.packageName])
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }

        if (showActivitySelector) {
            AlertDialog(
                onDismissRequest = { showActivitySelector = false },
                confirmButton = {},
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(ItemPadding)) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = activityQuery,
                            onValueChange = onActivityQueryChange,
                            singleLine = true,
                            label = { Text(text = stringResource(R.string.search_activity_hint)) }
                        )
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(ItemPadding)) {
                            val activityList = filteredActivityOptions
                            if (activityList.isEmpty()) {
                                item {
                                    Text(text = stringResource(R.string.no_matching_results))
                                }
                            } else {
                                items(activityList, key = { it.className }) { item ->
                                    val activityText = activityTextByClassName[item.className].orEmpty().ifBlank { item.className }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onClick {
                                                onSelectActivity(item.className)
                                                onActivityQueryChange("")
                                                showActivitySelector = false
                                                keyboardController?.hide()
                                            },
                                        horizontalArrangement = Arrangement.spacedBy(ItemPadding),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OpenAppOrUrlActivityDropdownItem(
                                            item = item,
                                            activityText = activityText,
                                            selected = false,
                                            multiSelectMode = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OpenAppOrUrlLinkSection(
    urlInput: String,
    selectedRecord: SelectedRecord,
    maxSelectCount: Int,
    onUrlInputChange: (String) -> Unit,
    onTestUrl: () -> Unit,
    onAddUrl: () -> Unit,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var urlFieldFocused by remember { mutableStateOf(false) }
    LaunchedEffect(urlFieldFocused) {
        if (urlFieldFocused) {
            delay(180)
            bringIntoViewRequester.bringIntoView()
        }
    }
    val canAddUrl = canOpenAppOrUrlEnabled(selectedRecord, maxSelectCount) && urlInput.trim().isNotBlank()

    OpenAppOrUrlSectionCard(title = stringResource(R.string.mode_url)) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(bringIntoViewRequester)
                .onFocusChanged { focusState ->
                    urlFieldFocused = focusState.isFocused
                },
            value = urlInput,
            onValueChange = onUrlInputChange,
            label = { Text(stringResource(R.string.url_link)) },
            singleLine = true
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(enabled = urlInput.trim().isNotBlank(), onClick = onTestUrl) {
                Text(text = stringResource(R.string.test_open_link))
            }
            TextButton(enabled = canAddUrl, onClick = onAddUrl) {
                Text(text = stringResource(R.string.add_link))
            }
        }
    }
}

@Composable
private fun OpenAppOrUrlSelectedItem(
    action: Action,
    selected: Boolean,
    selectSingle: Boolean,
    selectedRecord: SelectedRecord,
    launcherApps: List<OpenAppOrUrlLauncherAppOption>,
    absoluteIndex: Int,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    isEditing: Boolean,
    editingUrl: String,
    overflowMenuActionData: String?,
    onOverflowMenuActionDataChange: (String?) -> Unit,
    onSelect: (Action, Boolean) -> Unit,
    onMoveSelected: (Int, Int) -> Unit,
    onUpdateActionData: (Action, String) -> Unit,
    onStartEditUrl: (String, String) -> Unit,
    onEditUrlChange: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: (String) -> Unit,
) {
    val data = decodeOpenAppOrUrlData(action.data)
    val isUrlAction = data?.type == OpenAppOrUrlData.TYPE_URL
    val expanded = overflowMenuActionData == action.data

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(horizontal = OpenAppOrUrlHorizontalPadding, vertical = ItemPadding),
            verticalArrangement = Arrangement.spacedBy(ItemPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = MinInteractiveSize),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .onClick { onSelect(action, !selected) }
                        .padding(end = ItemPadding),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = buildOpenAppOrUrlActionLabel(action, launcherApps),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = when (data?.type) {
                            OpenAppOrUrlData.TYPE_URL -> data.url
                            OpenAppOrUrlData.TYPE_ACTIVITY -> data.packageName
                            else -> action.data
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box {
                    IconButton(onClick = { onOverflowMenuActionDataChange(action.data) }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { onOverflowMenuActionDataChange(null) }
                    ) {
                        if (isUrlAction) {
                            DropdownMenuItem(
                                text = { Text(text = stringResource(R.string.edit_link)) },
                                leadingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    onStartEditUrl(action.data, data?.url.orEmpty())
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.move_up)) },
                            leadingIcon = { Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null) },
                            onClick = {
                                onMoveSelected(absoluteIndex, absoluteIndex - 1)
                                onOverflowMenuActionDataChange(null)
                            },
                            enabled = canMoveUp
                        )
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.move_down)) },
                            leadingIcon = { Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null) },
                            onClick = {
                                onMoveSelected(absoluteIndex, absoluteIndex + 1)
                                onOverflowMenuActionDataChange(null)
                            },
                            enabled = canMoveDown
                        )
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.delete)) },
                            leadingIcon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null) },
                            onClick = {
                                onSelect(action, false)
                                onOverflowMenuActionDataChange(null)
                            }
                        )
                    }
                }
                if (!selectSingle) {
                    Checkbox(
                        checked = selected,
                        onCheckedChange = { onSelect(action, it) }
                    )
                }
            }

            if (isEditing && isUrlAction) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = editingUrl,
                        onValueChange = onEditUrlChange,
                        singleLine = true,
                        label = { Text(text = stringResource(R.string.url_link)) }
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = {
                            val trimmed = editingUrl.trim()
                            if (trimmed.isBlank()) return@TextButton
                            val newData = JsonHelper.encodeToString(
                                OpenAppOrUrlData(
                                    type = OpenAppOrUrlData.TYPE_URL,
                                    url = trimmed
                                )
                            )
                            if (selectedRecord.list.any {
                                    it is Action &&
                                        it.value == GlobalActions.OPEN_APP_OR_URL &&
                                        it.data == newData &&
                                        it.data != action.data
                                }
                            ) {
                                return@TextButton
                            }
                            onSaveEdit(newData)
                        }) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Text(text = stringResource(R.string.confirm))
                        }
                        TextButton(onClick = onCancelEdit) {
                            Text(text = stringResource(id = R.string.cancel))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OpenAppOrUrlSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(horizontal = OpenAppOrUrlHorizontalPadding, vertical = ItemPadding),
            verticalArrangement = Arrangement.spacedBy(ItemPadding)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun OpenAppOrUrlDropdownField(
    query: String,
    selectedValue: String,
    label: String,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onQueryChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onManualSelectClick: () -> Unit,
    menuContent: @Composable () -> Unit,
) {
    val searchFocusRequester = remember { FocusRequester() }
    val searchBringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var isEditing by remember { mutableStateOf(false) }
    var suppressNextFocusEvent by remember { mutableStateOf(false) }
    LaunchedEffect(expanded) {
        if (expanded && isEditing) {
            searchBringIntoViewRequester.bringIntoView()
            searchFocusRequester.requestFocus()
        }
    }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {}
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(searchBringIntoViewRequester)
                .focusRequester(searchFocusRequester)
                .onFocusChanged { focusState ->
                    if (suppressNextFocusEvent) {
                        suppressNextFocusEvent = false
                        isEditing = false
                        return@onFocusChanged
                    }
                    isEditing = focusState.isFocused
                    onFocusChange(focusState.isFocused)
                    if (focusState.isFocused) {
                        coroutineScope.launch {
                            searchBringIntoViewRequester.bringIntoView()
                        }
                    }
                }
                .menuAnchor(
                    type = MenuAnchorType.PrimaryEditable,
                    enabled = true
                ),
            value = if (isEditing) query else selectedValue,
            onValueChange = {
                if (!expanded && it.isNotBlank()) {
                    onExpandedChange(true)
                } else if (expanded && it.isBlank()) {
                    onExpandedChange(false)
                }
                onQueryChange(it)
            },
            readOnly = false,
            label = { Text(label) },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = {
                    suppressNextFocusEvent = true
                    focusManager.clearFocus(force = true)
                    onManualSelectClick()
                }) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = Modifier.exposedDropdownSize(matchTextFieldWidth = true),
            properties = PopupProperties(focusable = true)
        ) {
            menuContent()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OpenAppOrUrlDropdownMenuContent(
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(ItemPadding)) { content() }
}

@Composable
private fun OpenAppOrUrlLauncherDropdownItem(
    item: OpenAppOrUrlLauncherAppOption,
    icon: Drawable?
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(ItemPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            AsyncImage(
                modifier = Modifier.size(MinInteractiveSize),
                model = icon,
                contentDescription = null,
                contentScale = ContentScale.Fit
            )
        } else {
            Icon(imageVector = Icons.Default.Window, contentDescription = null)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = item.packageName,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun OpenAppOrUrlActivityDropdownItem(
    item: OpenAppOrUrlActivityOption,
    activityText: String,
    selected: Boolean,
    multiSelectMode: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(ItemPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Default.Settings, contentDescription = null)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activityText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.className,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (multiSelectMode) {
            Checkbox(checked = selected, onCheckedChange = null)
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

private fun canOpenAppOrUrlEnabled(
    selectedRecord: SelectedRecord,
    maxSelectCount: Int
): Boolean {
    val isMoveScreenSelected = selectedRecord.list.find {
        (it as? Action)?.value == GlobalActions.MOVE_SCREEN
    } != null
    if (isMoveScreenSelected) {
        return false
    }
    return selectedRecord.size < maxSelectCount
}

private data class OpenAppOrUrlLauncherAppOption(
    val packageName: String,
    val launcherClassName: String,
    val label: String
)

private data class OpenAppOrUrlActivityOption(
    val className: String,
    val label: String
)

private fun queryOpenAppOrUrlLauncherApps(context: android.content.Context): List<OpenAppOrUrlLauncherAppOption> {
    val packageManager = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    val result = mutableListOf<OpenAppOrUrlLauncherAppOption>()
    val seenPackages = mutableSetOf<String>()
    for (resolveInfo in packageManager.queryIntentActivitiesCompat(intent, PackageManager.MATCH_ALL)) {
        val activityInfo = resolveInfo.activityInfo ?: continue
        val packageName = activityInfo.packageName ?: continue
        if (!seenPackages.add(packageName)) continue
        result += OpenAppOrUrlLauncherAppOption(
            packageName = packageName,
            launcherClassName = activityInfo.name,
            label = activityInfo.loadLabel(packageManager).toString()
        )
    }
    return result.sortedWith(compareBy<OpenAppOrUrlLauncherAppOption> { it.label }.thenBy { it.packageName })
}

private fun queryOpenAppOrUrlActivityOptions(
    context: android.content.Context,
    packageName: String,
    selectedActivityClassName: String,
    launcherClassName: String?
): List<OpenAppOrUrlActivityOption> {
    if (packageName.isBlank()) return emptyList()
    val packageManager = context.packageManager
    val exportedActivities = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_ACTIVITIES.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        }
    } catch (_: Exception) {
        null
    }?.activities
        ?.filter { it.exported }
        ?.map {
            OpenAppOrUrlActivityOption(
                className = it.name,
                label = it.loadLabel(packageManager).toString()
            )
        }
        .orEmpty()

    return if (exportedActivities.isNotEmpty()) {
        exportedActivities
            .distinctBy { it.className }
            .sortedWith(compareBy<OpenAppOrUrlActivityOption> { it.label }.thenBy { it.className })
    } else {
        listOfNotNull(
            selectedActivityClassName.takeIf { it.isNotBlank() }?.let { OpenAppOrUrlActivityOption(it, it) },
            launcherClassName?.takeIf { it.isNotBlank() }?.let { OpenAppOrUrlActivityOption(it, it) }
        ).distinctBy { it.className }
    }
}

private fun formatOpenAppOrUrlActivityText(
    option: OpenAppOrUrlActivityOption?,
    packageName: String
): String {
    if (option == null) return ""
    val shortClassName = if (packageName.isNotBlank() && option.className.startsWith("$packageName.")) {
        option.className.removePrefix("$packageName.")
    } else {
        option.className
    }
    return shortClassName
}

private fun buildOpenAppOrUrlActionLabel(
    action: Action,
    launcherApps: List<OpenAppOrUrlLauncherAppOption>
): String {
    val data = decodeOpenAppOrUrlData(action.data) ?: return action.data
    return when (data.type) {
        OpenAppOrUrlData.TYPE_ACTIVITY -> {
            val appLabel = launcherApps.firstOrNull { it.packageName == data.packageName }?.label
                ?: data.packageName
            val shortActivityName = if (data.packageName.isNotBlank() && data.activityClassName.startsWith("${data.packageName}.")) {
                data.activityClassName.removePrefix("${data.packageName}.")
            } else {
                data.activityClassName
            }
            "$appLabel / $shortActivityName"
        }
        OpenAppOrUrlData.TYPE_URL -> data.url
        else -> action.data
    }
}

private fun decodeOpenAppOrUrlData(data: String): OpenAppOrUrlData? {
    return runCatching {
        JsonHelper.decodeFromString<OpenAppOrUrlData>(data)
    }.getOrNull()
}

private fun updateRecentItems(items: List<String>, value: String): List<String> {
    return listOf(value) + items.filterNot { it == value }.take(MAX_RECENT_ITEMS - 1)
}

private data class OpenAppOrUrlSearchIndex(
    val raw: String,
    val pinyin: String,
    val initials: String
)

private fun buildSearchIndex(context: android.content.Context, text: String): OpenAppOrUrlSearchIndex {
    val normalized = text.lowercase()
    val pinyinBuilder = StringBuilder()
    val initialsBuilder = StringBuilder()
    text.forEach { char ->
        val pinyin = runCatching {
            BasePinyinHelper.toHanyuPinyinStringArray(context, char)?.firstOrNull()
        }.getOrNull()?.takeIf { it.isNotBlank() }
        if (pinyin != null) {
            val plain = pinyin.filter { it.isLetter() }.lowercase()
            if (plain.isNotBlank()) {
                pinyinBuilder.append(plain)
                initialsBuilder.append(plain.first())
            }
        } else {
            val lower = char.lowercaseChar()
            if (lower.isLetterOrDigit()) {
                pinyinBuilder.append(lower)
                initialsBuilder.append(lower)
            }
        }
    }
    return OpenAppOrUrlSearchIndex(
        raw = normalized,
        pinyin = pinyinBuilder.toString(),
        initials = initialsBuilder.toString()
    )
}

private fun <T> filterByQuery(
    items: List<T>,
    query: String,
    indexProvider: (T) -> OpenAppOrUrlSearchIndex?
): List<T> {
    val token = query.trim().lowercase()
    if (token.isBlank()) return items
    return items.filter { item ->
        val index = indexProvider(item) ?: return@filter false
        index.raw.contains(token) || index.pinyin.contains(token) || index.initials.contains(token)
    }
}

private fun copyToClipboard(context: android.content.Context, label: String, text: String) {
    val clipboardManager = context.getSystemService(ClipboardManager::class.java)
    val clip = ClipData.newPlainText(label, text)
    clipboardManager?.setPrimaryClip(clip)
    showToast(R.string.copy_success)
}

private fun normalizeUrl(raw: String): String? {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) return null
    val candidate = if (trimmed.contains("://")) trimmed else "https://$trimmed"
    val uri = runCatching { Uri.parse(candidate) }.getOrNull() ?: return null
    if (uri.scheme.isNullOrBlank() || uri.host.isNullOrBlank()) return null
    return candidate
}

private fun prioritizeOpenAppOrUrlLauncherApps(
    items: List<OpenAppOrUrlLauncherAppOption>,
    selectedPackageName: String,
    recentPackageNames: List<String>
): List<OpenAppOrUrlLauncherAppOption> {
    return items.sortedWith(
        compareBy<OpenAppOrUrlLauncherAppOption> {
            when {
                it.packageName == selectedPackageName -> 0
                recentPackageNames.contains(it.packageName) -> 1
                else -> 2
            }
        }.thenBy {
            recentPackageNames.indexOf(it.packageName).takeIf { index -> index >= 0 } ?: Int.MAX_VALUE
        }.thenBy { it.label }
            .thenBy { it.packageName }
    )
}

private fun prioritizeOpenAppOrUrlActivities(
    items: List<OpenAppOrUrlActivityOption>,
    selectedActivityClassName: String,
    recentActivityClassNames: List<String>
): List<OpenAppOrUrlActivityOption> {
    return items.sortedWith(
        compareBy<OpenAppOrUrlActivityOption> {
            when {
                it.className == selectedActivityClassName -> 0
                recentActivityClassNames.contains(it.className) -> 1
                else -> 2
            }
        }.thenBy {
            recentActivityClassNames.indexOf(it.className).takeIf { index -> index >= 0 } ?: Int.MAX_VALUE
        }.thenBy { it.label }
            .thenBy { it.className }
    )
}

private const val MAX_SELECT_COUNT = 5
private const val MAX_RECENT_ITEMS = 8

private const val PAGE_UNIFIED = 0
private const val PAGE_OPEN_APP_OR_URL = 1
private const val PAGE_APPS = 1
private const val PAGE_SHORTCUTS = 2

private val PAGES = listOf(PAGE_UNIFIED, PAGE_OPEN_APP_OR_URL)
