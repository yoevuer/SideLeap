package hunoia.luno.ui.screen.actionselect

import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import com.aaron.compose.base.BaseComposeVM
import hunoia.luno.R
import hunoia.luno.core.AppContext
import hunoia.luno.action.GlobalActions
import hunoia.luno.action.payload.SubGestureActionData
import hunoia.luno.core.Paths
import hunoia.luno.action.definition.ActionCatalog
import hunoia.luno.action.Action
import hunoia.luno.quicklaunch.QuickLaunchFacade
import hunoia.luno.quicklaunch.model.AppInfo
import hunoia.luno.config.model.GestureButton
import hunoia.luno.quicklaunch.model.LauncherInfo
import hunoia.luno.ui.navigation.ActionSelect
import hunoia.luno.ui.navigation.IconResize
import hunoia.luno.config.model.Position
import hunoia.luno.config.model.TriggerDirection
import hunoia.luno.ui.event.IconResizeEvent
import hunoia.luno.action.appInfo
import hunoia.luno.quicklaunch.model.getIcon
import hunoia.luno.quicklaunch.model.qualifiedName
import hunoia.luno.quicklaunch.model.qualifiedNameWithIntents
import hunoia.luno.action.shortcutInfo
import hunoia.luno.ui.event.subscribeEvent
import hunoia.luno.ui.screen.actionselect.ActionSelectVM.UiEvent
import hunoia.luno.ui.screen.actionselect.ActionSelectVM.UiState
import hunoia.luno.freeze.FreezeFacade
import hunoia.luno.config.ConfigProvider
import hunoia.luno.config.model.SubGesture
import hunoia.luno.core.JsonHelper
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream


class ActionSelectVM(
    private val actionSelect: ActionSelect
) : BaseComposeVM<UiState, UiEvent>() {

    override val initialState: UiState = UiState(
        title = createTitle(),
        selectSingle = !actionSelect.isLongSlide || actionSelect.isTap
    )

    private val eventHandler = EventHandler()

    val actionSettingsDialog = ActionSettingsDialog()

    init {
        eventHandler.init()
        loadData()
    }

    fun addNewShortcut(launcherInfo: LauncherInfo, shortcutInfo: LauncherInfo.ShortcutInfo) {
        updateUiState {
            val index = it.createShortcuts.indexOfFirst { info ->
                info.qualifiedName == launcherInfo.qualifiedName
            }
            if (index < 0) {
                return@updateUiState it
            }
            val newList = it.createShortcuts.toMutableList().apply {
                val cache = it.createShortcuts[index]
                set(index, cache.copy(shortcuts = cache.shortcuts + shortcutInfo))
            }
            it.copy(createShortcuts = newList)
        }
    }

    fun select(obj: Any, selected: Boolean) {
        if (uiState.longPressTargetIndex != null && selected) {
            selectLongPressAction(obj)
            return
        }
        val uiState = uiState
        if (obj is AppInfo) {
            selectAppInfo(obj, selected)
            if (uiState.selectSingle) {
                saveSettings()
            }
        } else if (obj is LauncherInfo.ShortcutInfo) {
            selectShortcutInfo(obj, selected)
            if (uiState.selectSingle) {
                saveSettings()
            }
        } else if (obj is Action) {
            selectAction(obj, selected)
            val needsData = obj.value == GlobalActions.OPEN_APP_ACTIVITY ||
                obj.value == GlobalActions.OPEN_URL ||
                obj.value == GlobalActions.EXECUTE_SHELL_COMMAND
            if (uiState.selectSingle && (!needsData || obj.data.isNotEmpty())) {
                saveSettings()
            }
        }
    }

    fun startSetLongPressAction(index: Int) {
        if (index !in uiState.selectedRecord.list.indices) return
        updateUiState { it.copy(longPressTargetIndex = index) }
    }

    fun cancelSetLongPressAction() {
        updateUiState { it.copy(longPressTargetIndex = null) }
    }

    fun clearLongPressAction(index: Int) {
        updateSelectedAction(index) { it.copy(longPressAction = null) }
    }

    fun selectLongPressAction(obj: Any) {
        val index = uiState.longPressTargetIndex ?: return
        val longPressAction = obj.toAction()
        updateSelectedAction(index) { it.copy(longPressAction = longPressAction) }
        updateUiState { it.copy(longPressTargetIndex = null) }
    }

    fun updateActionData(action: Action, data: String) {
        val currentState = uiState
        val newList = currentState.selectedRecord.list.toMutableList()
        val index = newList.indexOfFirst { obj -> obj is Action && obj.sameAction(action) }
        if (index == -1) {
            return
        }
        val current = newList[index] as Action
        if (current.data == data) {
            return
        }
        newList[index] = current.copy(data = data)
        updateUiState {
            it.copy(selectedRecord = it.selectedRecord.copy(list = newList))
        }
        if (uiState.selectSingle) {
            saveSettings()
        }
    }

    fun moveSelectedAction(fromIndex: Int, toIndex: Int) {
        val currentState = uiState
        val list = currentState.selectedRecord.list.toMutableList()
        if (fromIndex !in list.indices || toIndex !in list.indices) {
            return
        }
        if (fromIndex == toIndex || list[fromIndex] == list[toIndex]) {
            return
        }
        val item = list.removeAt(fromIndex)
        val targetIndex = toIndex.coerceIn(0, list.size)
        list.add(targetIndex, item)
        updateUiState {
            it.copy(selectedRecord = it.selectedRecord.copy(list = list))
        }
        if (currentState.selectSingle) {
            saveSettings()
        }
    }

    fun toggleMiniWindow(appInfo: AppInfo) {
        val switchToMiniWindow = !appInfo.miniWindow
        updateUiState {
            val block: (MutableList<Action>) -> MutableList<Action> = { list ->
                val index = list.indexOfFirst { action ->
                    action.appInfo?.qualifiedName == appInfo.qualifiedName
                }
                if (index != -1) {
                    val current = list[index]
                    val currentAppInfo = current.appInfo ?: appInfo
                    val newAppInfo = currentAppInfo.copy(miniWindow = switchToMiniWindow)
                    list[index] = current.copy(data = JsonHelper.encodeToString(newAppInfo))
                }
                list
            }
            it.copy(
                apps = it.apps.map { app ->
                    if (app.qualifiedName == appInfo.qualifiedName) {
                        app.copy(miniWindow = switchToMiniWindow)
                    } else app
                },
                selectedRecord = it.selectedRecord.copy(
                    list = block(it.selectedRecord.list.filterIsInstance<Action>().toMutableList())
                )
            )
        }
        if (switchToMiniWindow) {
            toast(R.string.enable_mini_window)
        } else {
            toast(R.string.disable_mini_window)
        }
    }

    private fun selectShortcutInfo(shortcutInfo: LauncherInfo.ShortcutInfo, selected: Boolean) {
        updateUiState {
            val record = if (it.selectSingle && selected) {
                it.selectedRecord.copy(list = listOf(shortcutInfo.toAction()))
            } else {
                it.selectedRecord.selectShortcutInfo(shortcutInfo, selected)
            }
            it.copy(selectedRecord = record)
        }
    }

    private fun selectAppInfo(appInfo: AppInfo, selected: Boolean) {
        updateUiState {
            val record = if (it.selectSingle && selected) {
                it.selectedRecord.copy(list = listOf(appInfo.toAction()))
            } else {
                it.selectedRecord.selectAppInfo(appInfo, selected)
            }
            it.copy(selectedRecord = record)
        }
    }

    private fun selectAction(action: Action, selected: Boolean) {
        updateUiState {
            val record = if (it.selectSingle && selected) {
                it.selectedRecord.copy(list = listOf(action))
            } else {
                it.selectedRecord.selectAction(action, selected)
            }
            it.copy(selectedRecord = record)
        }
    }

    private fun updateSelectedAction(index: Int, transform: (Action) -> Action) {
        updateUiState {
            val list = it.selectedRecord.list.toMutableList()
            val current = list.getOrNull(index) as? Action ?: return@updateUiState it
            list[index] = transform(current)
            it.copy(selectedRecord = it.selectedRecord.copy(list = list))
        }
    }

    fun done() {
        val appInfos = uiState
            .selectedRecord
            .list
            .mapNotNull { (it as? Action)?.appInfo }
        val shortcutInfos = uiState
            .selectedRecord
            .list
            .mapNotNull { (it as? Action)?.shortcutInfo }
        if (appInfos.isNotEmpty() || shortcutInfos.isNotEmpty()) {
            val ids = mutableListOf<String>()
            appInfos.forEach { appInfo ->
                val icon = appInfo.getIcon(AppContext.get()) ?: return@forEach
                ids.add(appInfo.qualifiedName)
                QuickLaunchFacade.cacheIcon(appInfo.qualifiedName, icon)
                QuickLaunchFacade.cacheIconBgColor(appInfo.qualifiedName, appInfo.iconBgColor)
            }
            shortcutInfos.forEach { shortcutInfo ->
                val icon = shortcutInfo.getIcon(AppContext.get()) ?: return@forEach
                ids.add(shortcutInfo.qualifiedNameWithIntents)
                QuickLaunchFacade.cacheIcon(shortcutInfo.qualifiedNameWithIntents, icon)
                QuickLaunchFacade.cacheIconBgColor(shortcutInfo.qualifiedNameWithIntents, shortcutInfo.iconBgColor)
            }

            sendUiEvent(UiEvent.GotoIconResize(IconResize(ids)))
        } else {
            saveSettings()
        }
    }

    fun updateShortcutInfos() {
        viewModelScope.launchWithLoading {
            val createLauncherInfos = withContext(Dispatchers.IO) {
                QuickLaunchFacade.queryShortcutActivities(AppContext.get())
            }
            val launchLauncherInfos = withContext(Dispatchers.IO) {
                QuickLaunchFacade.queryShortcuts(AppContext.get())
            }
            if (uiState.selectSingle) {
                updateUiState {
                    it.copy(
                        createShortcuts = createLauncherInfos,
                        launchShortcuts = launchLauncherInfos
                    )
                }
                return@launchWithLoading
            }
            val selectedRecord = withContext(Dispatchers.Default) {
                uiState.selectedRecord.let { selectedRecord ->
                    // 检查是否有被卸载的，然后从选中中移除
                    val uninstalledList = mutableListOf<LauncherInfo.ShortcutInfo>()
                    selectedRecord
                        .list
                        .mapNotNull { (it as? Action)?.shortcutInfo }
                        .forEach { selected ->
                            val uninstalled = !createLauncherInfos.any { launcher ->
                                launcher.qualifiedName == selected.qualifiedName
                            } && !launchLauncherInfos.any { launcher ->
                                launcher.shortcuts.any { shortcut ->
                                    shortcut.qualifiedNameWithIntents == selected.qualifiedNameWithIntents
                                }
                            }
                            if (uninstalled) {
                                uninstalledList.add(selected)
                            }
                        }
                    selectedRecord.removeAllShortcutInfos(uninstalledList)
                }
            }
            val finalCreateList = withContext(Dispatchers.Default) {
                val list1 = mutableListOf<LauncherInfo>()
                val list2 = mutableListOf<LauncherInfo>()
                val selectedShortcutInfos = selectedRecord.list.mapNotNull { (it as? Action)?.shortcutInfo }
                createLauncherInfos.forEach { launcherInfo ->
                    val cache = selectedShortcutInfos.find { info ->
                        info.packageName == launcherInfo.packageName
                    }
                    if (cache != null) {
                        list1.add(launcherInfo)
                    } else {
                        list2.add(launcherInfo)
                    }
                }
                list1 + list2
            }
            val finalLaunchList = withContext(Dispatchers.Default) {
                val list1 = mutableListOf<LauncherInfo>()
                val list2 = mutableListOf<LauncherInfo>()
                val selectedShortcutInfos = selectedRecord.list.mapNotNull { (it as? Action)?.shortcutInfo }
                launchLauncherInfos.forEach { launcherInfo ->
                    val cache = selectedShortcutInfos.find { info ->
                        info.packageName == launcherInfo.packageName
                    }
                    if (cache != null) {
                        list1.add(launcherInfo)
                    } else {
                        list2.add(launcherInfo)
                    }
                }
                list1 + list2
            }
            updateUiState {
                it.copy(
                    createShortcuts = finalCreateList,
                    launchShortcuts = finalLaunchList,
                    selectedRecord = selectedRecord
                )
            }
            uiState
                .selectedRecord
                .list
                .mapNotNull { (it as? Action)?.shortcutInfo }
                .forEach { shortcut ->
                    val launcherInfo = createLauncherInfos.find {
                        it.qualifiedName == shortcut.qualifiedName
                    }
                    if (launcherInfo != null) {
                        addNewShortcut(launcherInfo, shortcut)
                    }
                }
        }
    }

    fun updateAppInfos() {
        viewModelScope.launchWithLoading {
            val appInfos = withContext(Dispatchers.IO) {
                QuickLaunchFacade.queryApps(AppContext.get())
            }
            val frozenApps = FreezeFacade.queryFrozenApps(AppContext.get())
            // 合并普通应用和冻结应用，普通应用优先，冻结应用只添加不存在的
            val normalPackageNames = appInfos.map { it.packageName }.toSet()
            val filteredFrozenApps = frozenApps.filter { it.packageName !in normalPackageNames }
            // Explicitly create merged list to avoid type inference issues
            val mergedApps = mutableListOf<AppInfo>()
            mergedApps.addAll(appInfos)
            mergedApps.addAll(filteredFrozenApps)
            if (uiState.selectSingle) {
                updateUiState {
                    it.copy(apps = mergedApps)
                }
                return@launchWithLoading
            }
            val selectedRecord = withContext(Dispatchers.Default) {
                uiState.selectedRecord.let { selectedRecord ->
                    // 检查是否有被卸载的，然后从选中中移除
                    val uninstalledList = mutableListOf<AppInfo>()
                    selectedRecord
                        .list
                        .mapNotNull { (it as? Action)?.appInfo }
                        .forEach { selectedApp ->
                            val uninstalled = !mergedApps.any { app ->
                                selectedApp.qualifiedName == app.qualifiedName
                            }
                            if (uninstalled) {
                                uninstalledList.add(selectedApp)
                            }
                        }
                    selectedRecord.removeAllAppInfos(uninstalledList)
                }
            }
            val finalList = withContext(Dispatchers.Default) {
                val list1 = mutableListOf<AppInfo>()
                val list2 = mutableListOf<AppInfo>()
                val selectedAppInfos = selectedRecord.list.mapNotNull { (it as? Action)?.appInfo }
                mergedApps.forEach { appInfo ->
                    val cache = selectedAppInfos.find { app ->
                        app.qualifiedName == appInfo.qualifiedName
                    }
                    if (cache != null) {
                        val appInfo2 = appInfo.copy(
                            iconScale = cache.iconScale,
                            miniWindow = cache.miniWindow
                        )
                        list1.add(appInfo2)
                    } else {
                        list2.add(appInfo)
                    }
                }
                // Use explicit addAll to avoid type inference warnings
                val result = mutableListOf<AppInfo>()
                result.addAll(list1)
                result.addAll(list2)
                result
            }
            updateUiState {
                it.copy(
                    apps = finalList,
                    selectedRecord = selectedRecord
                )
            }
        }
    }

    private fun createTitle(): String {
        val context = AppContext.get()
        val actionSelect = actionSelect
        if (actionSelect.isTap) {
            return when (actionSelect.direction) {
                TriggerDirection.Center -> context.getString(R.string.tap_action)
                TriggerDirection.Center2 -> context.getString(R.string.long_press)
                else -> ""
            }
        }
        val str1 = when (actionSelect.direction) {
            TriggerDirection.Center -> when (actionSelect.position) {
                Position.Left -> context.getString(R.string.slide_to_right)
                Position.Right -> context.getString(R.string.slide_to_left)
                Position.Bottom -> context.getString(R.string.slide_to_top)
            }
            TriggerDirection.Up -> when (actionSelect.position) {
                Position.Left -> context.getString(R.string.slide_to_top_right)
                Position.Right -> context.getString(R.string.slide_to_top_left)
                Position.Bottom -> context.getString(R.string.slide_to_top_left)
            }
            TriggerDirection.Down -> when (actionSelect.position) {
                Position.Left -> context.getString(R.string.slide_to_bottom_right)
                Position.Right -> context.getString(R.string.slide_to_bottom_left)
                Position.Bottom -> context.getString(R.string.slide_to_top_right)
            }
            TriggerDirection.Center2 -> context.getString(R.string.long_press)
            TriggerDirection.Up2 -> when (actionSelect.position) {
                Position.Left, Position.Right -> context.getString(R.string.slide_to_top)
                Position.Bottom -> context.getString(R.string.slide_to_left)
            }
            TriggerDirection.Down2 -> when (actionSelect.position) {
                Position.Left, Position.Right -> context.getString(R.string.slide_to_bottom)
                Position.Bottom -> context.getString(R.string.slide_to_right)
            }
        }
        if (actionSelect.direction == TriggerDirection.Center2) {
            return str1
        }
        val str2 = when (actionSelect.isLongSlide) {
            true -> context.getString(R.string.long1)
            else -> context.getString(R.string.short1)
        }
        return "$str1($str2)"
    }

    private fun loadData() {
        viewModelScope.launch {
            val buttons = if (actionSelect.isSideButton) {
                ConfigProvider.sideGestureButtons
            } else {
                ConfigProvider.bottomGestureButtons
            }
            ConfigProvider
                .gestureSettings
                .combine(buttons) { f1, f2 ->
                    f1 to f2
                }
                .take(1)
                .collectLatest { (gestureSettings, gestureButtons) ->
                    val subGestures = ConfigProvider.getSubGestureSettings().subGestures
                    val button = gestureButtons.find {
                        it.id == actionSelect.gestureButtonId && it.position == actionSelect.position
                    }
                    updateUiState {
                        val selectSingle = !actionSelect.isLongSlide || (button != null && !button.longSlideTriggerImmediately)
                        it.copy(
                            selectSingle = selectSingle,
                            maxSelectCount = if (selectSingle) 1 else LONG_SLIDE_SOFT_MAX_SELECT_COUNT,
                            subGestures = subGestures
                        )
                    }
                    if (button != null) {
                        val actionSelect = actionSelect
                        val gestureActions = when {
                            actionSelect.isTap -> button.tapActions
                            actionSelect.isLongSlide -> button.longSlideActions
                            else -> button.slideActions
                        }
                        val actions = when (actionSelect.direction) {
                            TriggerDirection.Center -> gestureActions.center
                            TriggerDirection.Up -> gestureActions.up
                            TriggerDirection.Down -> gestureActions.down
                            TriggerDirection.Center2 -> gestureActions.center2
                            TriggerDirection.Up2 -> gestureActions.up2
                            TriggerDirection.Down2 -> gestureActions.down2
                        }
                        updateUiState {
                            val selectedActions = when (it.selectSingle) {
                                true -> emptyList()
                                else -> actions
                            }
                            val newSelectedRecord = it.selectedRecord.selectAll(selectedActions)
                            it.copy(selectedRecord = newSelectedRecord)
                        }
                        assembleData()
                    }
                }
        }
    }

    private fun assembleData() {
        updateUiState { state ->
            val allActions = ActionCatalog.definitions
                .filter { def -> def.isDisplayed }
                .map { def -> def.toAction() }
                .toMutableList()
                .apply {
                state.subGestures
                    .filter { gesture -> gesture.enabled }
                    .forEach { gesture ->
                        add(
                            Action(
                                value = GlobalActions.SUB_GESTURE,
                                data = JsonHelper.encodeToString(SubGestureActionData(id = gesture.id))
                            )
                        )
                    }
                }
            if (state.selectSingle) {
                return@updateUiState state.copy(actions = allActions)
            }
            val allWithoutNone = allActions.apply { removeAt(0) }
            val list1 = mutableListOf<Action>()
            val list2 = mutableListOf<Action>()
            allWithoutNone.forEach { action ->
                if (state.selectedRecord.isSelected(action) || action == Action.NONE) {
                    list1.add(action)
                } else {
                    list2.add(action)
                }
            }
            val finalList = list1 + list2
            state.copy(actions = finalList)
        }
    }

    private fun saveSettings() {
        viewModelScope.launch {
            val buttonsUpdater = if (actionSelect.isSideButton) {
                ConfigProvider::updateSideGestureButtons
            } else {
                ConfigProvider::updateBottomGestureButtons
            }
            buttonsUpdater { list ->
                val mutableList = list.toMutableList()
                val actionSelect = actionSelect
                var button: GestureButton? = null
                var index = -1
                for (i in mutableList.indices) {
                    index = i
                    val b = mutableList[i]
                    if (b.id == actionSelect.gestureButtonId &&
                        b.position == actionSelect.position
                    ) {
                        button = b
                        break
                    }
                }
                if (button == null) {
                    return@buttonsUpdater mutableList
                }
                val selectedRecord = uiState.selectedRecord
                val selectedList = selectedRecord.list.filterIsInstance<Action>()
                val newActions = when (uiState.selectSingle) {
                    true -> selectedList.takeLast(1)
                    else -> selectedList
                }
                val gestureActions = when {
                    actionSelect.isTap -> button.tapActions
                    actionSelect.isLongSlide -> button.longSlideActions
                    else -> button.slideActions
                }
                fun tryDeleteShortcutIcons(old: List<Action>, new: List<Action>) {
                    fun List<Action>.shortcutIconPaths(): List<String> {
                        return flatMap { action ->
                            listOfNotNull(
                                action.shortcutInfo?.iconPath,
                                action.longPressAction?.shortcutInfo?.iconPath
                            )
                        }.filter { it.isNotEmpty() }
                    }
                    val newPaths = new.shortcutIconPaths().toSet()
                    old.forEach { action ->
                        listOfNotNull(action.shortcutInfo, action.longPressAction?.shortcutInfo).forEach { shortcutInfo ->
                            if (shortcutInfo.iconPath.isNullOrEmpty()) return@forEach
                            if (shortcutInfo.iconPath in newPaths) return@forEach
                            File(shortcutInfo.iconPath).delete()
                        }
                    }
                }
                val newGestureActions = when (actionSelect.direction) {
                    TriggerDirection.Center -> {
                        val oldActions = gestureActions.center
                        tryDeleteShortcutIcons(oldActions, newActions)
                        gestureActions.copy(center = newActions)
                    }
                    TriggerDirection.Up -> {
                        val oldActions = gestureActions.up
                        tryDeleteShortcutIcons(oldActions, newActions)
                        gestureActions.copy(up = newActions)
                    }
                    TriggerDirection.Down -> {
                        val oldActions = gestureActions.down
                        tryDeleteShortcutIcons(oldActions, newActions)
                        gestureActions.copy(down = newActions)
                    }
                    TriggerDirection.Center2 -> {
                        val oldActions = gestureActions.center2
                        tryDeleteShortcutIcons(oldActions, newActions)
                        gestureActions.copy(center2 = newActions)
                    }
                    TriggerDirection.Up2 -> {
                        val oldActions = gestureActions.up2
                        tryDeleteShortcutIcons(oldActions, newActions)
                        gestureActions.copy(up2 = newActions)
                    }
                    TriggerDirection.Down2 -> {
                        val oldActions = gestureActions.down2
                        tryDeleteShortcutIcons(oldActions, newActions)
                        gestureActions.copy(down2 = newActions)
                    }
                }
                button = when {
                    actionSelect.isTap -> button.copy(tapActions = newGestureActions)
                    actionSelect.isLongSlide -> button.copy(longSlideActions = newGestureActions)
                    else -> button.copy(slideActions = newGestureActions)
                }
                mutableList.apply {
                    set(index, button)
                }
            }
        }.invokeOnCompletion {
            if (it == null) {
                toast(R.string.save_success)
                finish()
            } else {
                toast(R.string.save_failure)
            }
        }
    }

    private inner class EventHandler {

        fun init() {
            subscribeEvent(IconResizeEvent::class) { event ->
                val scaleFactors = event.scaleFactors
                val bgColors = event.bgColors
                updateUiState {
                    val selectedList = it.selectedRecord.list.toMutableList()
                    scaleFactors.forEach { (id, scaleFactor) ->
                        val index = selectedList.indexOfFirst { obj ->
                            (obj as? Action)?.appInfo?.qualifiedName == id
                        }
                        if (index != -1) {
                            val old = selectedList[index] as Action
                            val appInfo = old.appInfo
                            if (appInfo != null) {
                                selectedList[index] = old.copy(data = JsonHelper.encodeToString(appInfo.copy(iconScale = scaleFactor)))
                            }
                            return@forEach
                        }
                        val index2 = selectedList.indexOfFirst { obj ->
                            (obj as? Action)?.shortcutInfo?.qualifiedNameWithIntents == id
                        }
                        if (index2 != -1) {
                            val old = selectedList[index2] as Action
                            val shortcutInfo = old.shortcutInfo
                            if (shortcutInfo != null) {
                                selectedList[index2] = old.copy(data = JsonHelper.encodeToString(shortcutInfo.copy(iconScale = scaleFactor)))
                            }
                        }
                    }
                    bgColors.forEach { (id, color) ->
                        val index = selectedList.indexOfFirst { obj ->
                            (obj as? Action)?.appInfo?.qualifiedName == id
                        }
                        if (index != -1) {
                            val old = selectedList[index] as Action
                            val appInfo = old.appInfo
                            if (appInfo != null) {
                                selectedList[index] = old.copy(data = JsonHelper.encodeToString(appInfo.copy(iconBgColor = color)))
                            }
                            return@forEach
                        }
                        val index2 = selectedList.indexOfFirst { obj ->
                            (obj as? Action)?.shortcutInfo?.qualifiedNameWithIntents == id
                        }
                        if (index2 != -1) {
                            val old = selectedList[index2] as Action
                            val shortcutInfo = old.shortcutInfo
                            if (shortcutInfo != null) {
                                selectedList[index2] = old.copy(data = JsonHelper.encodeToString(shortcutInfo.copy(iconBgColor = color)))
                            }
                        }
                    }

                    // 保存Bitmap到本地
                    val shortcutInfos = mutableMapOf<Int, LauncherInfo.ShortcutInfo>()
                    selectedList.forEachIndexed { index, obj ->
                        val action = obj as? Action ?: return@forEachIndexed
                        val shortcutInfo = action.shortcutInfo ?: return@forEachIndexed
                        val iconBitmap = shortcutInfo.iconBitmap ?: return@forEachIndexed
                        val iconPath = "${Paths.Image}/${System.currentTimeMillis()}"
                        val fos = FileOutputStream(iconPath)
                        iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                        shortcutInfos[index] = shortcutInfo.copy(iconPath = iconPath)
                    }
                    shortcutInfos.forEach { (index, shortcutInfo) ->
                        val action = selectedList[index] as? Action ?: return@forEach
                        selectedList[index] = action.copy(data = JsonHelper.encodeToString(shortcutInfo))
                    }

                    it.copy(selectedRecord = UiState.SelectedRecord(selectedList))
                }
                saveSettings()
            }
        }
    }

    inner class ActionSettingsDialog {

        fun show(show: Boolean, action: Action = Action.NONE) {
            updateUiState {
                it.copy(actionSettingsDialog = it.actionSettingsDialog.copy(show = show, action = action))
            }
        }
    }

    data class UiState(
        val title: String = "",
        val selectSingle: Boolean = true,
        val actions: List<Action> = emptyList(),
        val apps: List<AppInfo> = emptyList(),
        val createShortcuts: List<LauncherInfo> = emptyList(),
        val launchShortcuts: List<LauncherInfo> = emptyList(),
        val maxSelectCount: Int = MAX_SELECT_COUNT,
        val selectedRecord: SelectedRecord = SelectedRecord(),
        val longPressTargetIndex: Int? = null,
        val actionSettingsDialog: ActionSettingsDialogValue = ActionSettingsDialogValue(false, Action.NONE),
        val subGestures: List<SubGesture> = emptyList(),
    ) {
        data class SelectedRecord(val list: List<Any> = emptyList()) {

            val size: Int get() = list.size

            fun selectAll(actions: List<Action>): SelectedRecord {
                val newList = list.toMutableList().apply {
                    actions.forEach { action ->
                        add(action)
                    }
                }
                return this.copy(list = newList)
            }

            fun selectAction(action: Action, selected: Boolean): SelectedRecord {
                val newList = list.toMutableList().apply {
                    if (selected) {
                        add(action)
                    } else {
                        removeAll { it is Action && it.sameAction(action) }
                    }
                }
                return this.copy(list = newList)
            }

            fun selectAppInfo(app: AppInfo, selected: Boolean): SelectedRecord {
                val newList = list.toMutableList().apply {
                    if (selected) {
                        add(app.toAction())
                    } else {
                        removeAll { (it as? Action)?.appInfo?.qualifiedName == app.qualifiedName }
                    }
                }
                return this.copy(list = newList)
            }

            fun selectShortcutInfo(shortcut: LauncherInfo.ShortcutInfo, selected: Boolean): SelectedRecord {
                val newList = list.toMutableList().apply {
                    if (selected) {
                        add(shortcut.toAction())
                    } else {
                        removeAll {
                            (it as? Action)?.shortcutInfo?.qualifiedNameWithIntents == shortcut.qualifiedNameWithIntents
                        }
                    }
                }
                return this.copy(list = newList)
            }

            fun removeAllAppInfos(list: List<AppInfo>): SelectedRecord {
                val newList = this.list.toMutableList().apply {
                    removeAll {
                        val appInfo = (it as? Action)?.appInfo ?: return@removeAll false
                        list.any { selected -> appInfo.qualifiedName == selected.qualifiedName }
                    }
                }
                return this.copy(list = newList)
            }

            fun removeAllShortcutInfos(list: List<LauncherInfo.ShortcutInfo>): SelectedRecord {
                val newList = this.list.toMutableList().apply {
                    removeAll {
                        val shortcutInfo = (it as? Action)?.shortcutInfo ?: return@removeAll false
                        list.any { selected ->
                            shortcutInfo.qualifiedNameWithIntents == selected.qualifiedNameWithIntents
                        }
                    }
                }
                return this.copy(list = newList)
            }

            fun isSelected(obj: Any): Boolean {
                if (obj is AppInfo) {
                    return list.find {
                        (it as? Action)?.appInfo?.qualifiedName == obj.qualifiedName
                    } != null
                } else if (obj is LauncherInfo.ShortcutInfo) {
                    return list.find {
                        (it as? Action)?.shortcutInfo?.qualifiedNameWithIntents == obj.qualifiedNameWithIntents
                    } != null
                } else if (obj is Action) {
                    return list.find { it is Action && it.sameAction(obj) } != null
                }
                return obj in list
            }
        }

        data class ActionSettingsDialogValue(
            val show: Boolean,
            val action: Action
        )
    }

    sealed interface UiEvent {
        data class GotoIconResize(val iconResize: IconResize) : UiEvent
    }
}

private fun Any.toAction(): Action {
    return when (this) {
        is Action -> this.copy(extra = null)
        is AppInfo -> Action(
            value = GlobalActions.EXTRA_LAUNCH_APP,
            data = JsonHelper.encodeToString(this)
        )
        is LauncherInfo.ShortcutInfo -> Action(
            value = GlobalActions.EXTRA_LAUNCH_SHORTCUT,
            data = JsonHelper.encodeToString(this)
        )
        else -> error("Unsupported selected action type: ${this::class.java.name}")
    }
}

private fun Action.sameAction(other: Action): Boolean {
    return value == other.value && data == other.data
}
