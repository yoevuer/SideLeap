package hunoia.luno.ui.screen.actionselect

import hunoia.luno.action.GlobalActions
import hunoia.luno.action.Action
import hunoia.luno.launcher.model.AppInfo
import hunoia.luno.launcher.model.LauncherInfo
import hunoia.luno.ui.screen.actionselect.ActionSelectVM.UiState.SelectedRecord

internal const val MAX_SELECT_COUNT = 5
internal const val LONG_SLIDE_SOFT_MAX_SELECT_COUNT = 50

internal fun canActionEnabled(
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
    if (maxSelectCount <= 1) return true
    return !(selectedRecord.size >= maxSelectCount && !selectedRecord.isSelected(item))
}

internal fun canAppInfoEnabled(
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
    if (maxSelectCount <= 1) return true
    return !(selectedRecord.size >= maxSelectCount && !selectedRecord.isSelected(item))
}

internal fun canLauncherInfoEnabled(
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
    if (maxSelectCount <= 1) return true
    return !(selectedRecord.size >= maxSelectCount && !item.shortcuts.any { selectedRecord.isSelected(it) })
}

internal fun canShortcutInfoEnabled(
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
    if (maxSelectCount <= 1) return true
    return !(selectedRecord.size >= maxSelectCount && !selectedRecord.isSelected(item))
}
