package hunoia.luno.ui.screen.actionselect

import hunoia.luno.action.api.appInfo
import hunoia.luno.action.api.shortcutInfo
import hunoia.luno.config.ConfigProvider
import hunoia.luno.config.model.Action
import hunoia.luno.config.model.GestureButton
import hunoia.luno.config.model.TriggerDirection
import hunoia.luno.core.AppContext
import hunoia.luno.quicklaunch.model.qualifiedName
import hunoia.luno.quicklaunch.model.qualifiedNameWithIntents
import hunoia.luno.freeze.FreezeFacade
import hunoia.luno.quicklaunch.QuickLaunchFacade
import hunoia.luno.quicklaunch.model.AppInfo
import hunoia.luno.quicklaunch.model.LauncherInfo
import hunoia.luno.ui.navigation.ActionSelect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withContext

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
