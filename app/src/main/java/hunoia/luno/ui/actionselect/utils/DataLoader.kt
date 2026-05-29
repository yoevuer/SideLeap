package hunoia.luno.ui.actionselect

import android.graphics.Bitmap
import hunoia.luno.action.api.appInfo
import hunoia.luno.action.api.shortcutInfo
import hunoia.luno.config.ConfigProvider
import hunoia.luno.config.model.Action
import hunoia.luno.config.model.GestureButton
import hunoia.luno.config.model.TriggerDirection
import hunoia.luno.core.AppContext
import hunoia.luno.core.JsonSerializer
import hunoia.luno.core.Paths
import hunoia.luno.freeze.FreezeFacade
import hunoia.luno.quicklaunch.QuickLaunchFacade
import hunoia.luno.quicklaunch.model.AppInfo
import hunoia.luno.quicklaunch.model.LauncherInfo
import hunoia.luno.quicklaunch.model.qualifiedName
import hunoia.luno.quicklaunch.model.qualifiedNameWithIntents
import hunoia.luno.ui.component.IconResizeEvent
import hunoia.luno.ui.navigation.ActionSelect
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withContext

class EventHandler(
    private val onUpdateUiState: ((UiState) -> UiState) -> Unit,
    private val onSaveSettings: () -> Unit,
    private val subscribeToIconResizeEvent: ((IconResizeEvent) -> Unit) -> Unit
) {
    fun init() {
        subscribeToIconResizeEvent { event ->
            val scaleFactors = event.scaleFactors
            val bgColors = event.bgColors
            onUpdateUiState { state ->
                val selectedList = state.selectedRecord.list.toMutableList()
                scaleFactors.forEach { (id, scaleFactor) ->
                    val index = selectedList.indexOfFirst { obj ->
                        (obj as? Action)?.appInfo?.qualifiedName == id
                    }
                    if (index != -1) {
                        val old = selectedList[index] as Action
                        val appInfo = old.appInfo
                        if (appInfo != null) {
                            selectedList[index] = old.copy(data = JsonSerializer.encodeToString(appInfo.copy(iconScale = scaleFactor)))
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
                            selectedList[index2] = old.copy(data = JsonSerializer.encodeToString(shortcutInfo.copy(iconScale = scaleFactor)))
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
                            selectedList[index] = old.copy(data = JsonSerializer.encodeToString(appInfo.copy(iconBgColor = color)))
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
                            selectedList[index2] = old.copy(data = JsonSerializer.encodeToString(shortcutInfo.copy(iconBgColor = color)))
                        }
                    }
                }

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
                    selectedList[index] = action.copy(data = JsonSerializer.encodeToString(shortcutInfo))
                }

                state.copy(selectedRecord = UiState.SelectedRecord(selectedList))
            }
            onSaveSettings()
        }
    }
}

internal suspend fun saveSettingsAction(
    actionSelect: ActionSelect,
    getUiState: () -> UiState,
    updateUiState: ((UiState) -> UiState) -> Unit
) {
    val buttonsUpdater = if (actionSelect.isSideButton) {
        ConfigProvider::updateSideGestureButtons
    } else {
        ConfigProvider::updateBottomGestureButtons
    }
    buttonsUpdater { list ->
        val mutableList = list.toMutableList()
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
        val selectedRecord = getUiState().selectedRecord
        val selectedList = selectedRecord.list.filterIsInstance<Action>()
        val newActions = when (getUiState().selectSingle) {
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
}

internal suspend fun updateShortcutInfosBody(
    getUiState: () -> UiState,
    updateUiState: ((UiState) -> UiState) -> Unit,
    addNewShortcut: (LauncherInfo, LauncherInfo.ShortcutInfo) -> Unit
) {
    val createLauncherInfos = withContext(Dispatchers.IO) {
        QuickLaunchFacade.queryShortcutActivities(AppContext.get())
    }
    val launchLauncherInfos = withContext(Dispatchers.IO) {
        QuickLaunchFacade.queryShortcuts(AppContext.get())
    }
    if (getUiState().selectSingle) {
        updateUiState {
            it.copy(
                createShortcuts = createLauncherInfos,
                launchShortcuts = launchLauncherInfos
            )
        }
        return
    }
    val selectedRecord = withContext(Dispatchers.Default) {
        getUiState().selectedRecord.let { selectedRecord ->
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
    getUiState()
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

internal suspend fun updateAppInfosBody(
    getUiState: () -> UiState,
    updateUiState: ((UiState) -> UiState) -> Unit
) {
    val appInfos = withContext(Dispatchers.IO) {
        QuickLaunchFacade.queryApps(AppContext.get())
    }
    val frozenApps = FreezeFacade.queryFrozenApps(AppContext.get())
    val normalPackageNames = appInfos.map { it.packageName }.toSet()
    val filteredFrozenApps = frozenApps.filter { it.packageName !in normalPackageNames }
    val mergedApps = mutableListOf<AppInfo>()
    mergedApps.addAll(appInfos)
    mergedApps.addAll(filteredFrozenApps)
    if (getUiState().selectSingle) {
        updateUiState {
            it.copy(apps = mergedApps)
        }
        return
    }
    val selectedRecord = withContext(Dispatchers.Default) {
        getUiState().selectedRecord.let { selectedRecord ->
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

internal suspend fun loadDataBody(
    actionSelect: ActionSelect,
    onUpdateState: (update: (UiState) -> UiState) -> Unit
) {
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
            onUpdateState { state ->
                val selectSingle = !actionSelect.isLongSlide || (button != null && !button.longSlideTriggerImmediately)
                state.copy(
                    selectSingle = selectSingle,
                    maxSelectCount = if (selectSingle) 1 else LONG_SLIDE_SOFT_MAX_SELECT_COUNT,
                    subGestures = subGestures
                )
            }
            if (button != null) {
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
                onUpdateState { state ->
                    val selectedActions = when (state.selectSingle) {
                        true -> emptyList()
                        else -> actions
                    }
                    val newSelectedRecord = state.selectedRecord.selectAll(selectedActions)
                    state.copy(selectedRecord = newSelectedRecord)
                }
            }
        }
}
