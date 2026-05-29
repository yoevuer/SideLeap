package hunoia.luno.ui.screen.actionselect

import hunoia.luno.action.api.ActionFacade
import hunoia.luno.action.api.appInfo
import hunoia.luno.action.api.shortcutInfo
import hunoia.luno.action.payload.SubGestureActionData
import hunoia.luno.config.model.Action
import hunoia.luno.core.JsonHelper
import hunoia.luno.quicklaunch.model.AppInfo
import hunoia.luno.quicklaunch.model.LauncherInfo
import hunoia.luno.quicklaunch.model.qualifiedName

internal fun assembleDataTransform(state: UiState): UiState {
    val allActions = ActionFacade.definitions
        .filter { def -> def.isDisplayed }
        .map { def -> def.toAction() }
        .toMutableList()
        .apply {
            state.subGestures
                .filter { gesture -> gesture.enabled }
                .forEach { gesture ->
                    add(
                        Action(
                            value = ActionFacade.SUB_GESTURE,
                            data = JsonHelper.encodeToString(SubGestureActionData(id = gesture.id))
                        )
                    )
                }
        }
    if (state.selectSingle) {
        return state.copy(actions = allActions)
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
    return state.copy(actions = finalList)
}

internal fun selectActionTransform(state: UiState, action: Action, selected: Boolean): UiState {
    val record = if (state.selectSingle && selected) {
        state.selectedRecord.copy(list = listOf(action))
    } else {
        state.selectedRecord.selectAction(action, selected)
    }
    return state.copy(selectedRecord = record)
}

internal fun selectAppInfoTransform(state: UiState, appInfo: AppInfo, selected: Boolean): UiState {
    val record = if (state.selectSingle && selected) {
        state.selectedRecord.copy(list = listOf(appInfo.toAction()))
    } else {
        state.selectedRecord.selectAppInfo(appInfo, selected)
    }
    return state.copy(selectedRecord = record)
}

internal fun selectShortcutInfoTransform(state: UiState, shortcut: LauncherInfo.ShortcutInfo, selected: Boolean): UiState {
    val record = if (state.selectSingle && selected) {
        state.selectedRecord.copy(list = listOf(shortcut.toAction()))
    } else {
        state.selectedRecord.selectShortcutInfo(shortcut, selected)
    }
    return state.copy(selectedRecord = record)
}

internal fun updateSelectedActionTransform(state: UiState, index: Int, transform: (Action) -> Action): UiState {
    val list = state.selectedRecord.list.toMutableList()
    val current = list.getOrNull(index) as? Action ?: return state
    list[index] = transform(current)
    return state.copy(selectedRecord = state.selectedRecord.copy(list = list))
}

internal fun toggleMiniWindowTransform(state: UiState, appInfo: AppInfo, switchToMiniWindow: Boolean): UiState {
    return state.copy(
        apps = state.apps.map { app ->
            if (app.qualifiedName == appInfo.qualifiedName) {
                app.copy(miniWindow = switchToMiniWindow)
            } else app
        },
        selectedRecord = state.selectedRecord.copy(
            list = state.selectedRecord.list.filterIsInstance<Action>().toMutableList().let { list ->
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
        )
    )
}

internal fun updateActionDataTransform(state: UiState, action: Action, data: String): UiState {
    val newList = state.selectedRecord.list.toMutableList()
    val index = newList.indexOfFirst { obj -> obj is Action && obj.sameAction(action) }
    if (index == -1) return state
    val current = newList[index] as Action
    if (current.data == data) return state
    newList[index] = current.copy(data = data)
    return state.copy(selectedRecord = state.selectedRecord.copy(list = newList))
}

internal fun moveSelectedActionTransform(state: UiState, fromIndex: Int, toIndex: Int): UiState {
    val list = state.selectedRecord.list.toMutableList()
    if (fromIndex !in list.indices || toIndex !in list.indices) return state
    if (fromIndex == toIndex || list[fromIndex] == list[toIndex]) return state
    val item = list.removeAt(fromIndex)
    val targetIndex = toIndex.coerceIn(0, list.size)
    list.add(targetIndex, item)
    return state.copy(selectedRecord = state.selectedRecord.copy(list = list))
}
