package hunoia.sideleap.ui.screen.actionselect

import hunoia.sideleap.action.GlobalActions
import hunoia.sideleap.action.Action
import hunoia.sideleap.launcher.model.AppInfo
import hunoia.sideleap.launcher.model.LauncherInfo
import hunoia.sideleap.ui.screen.actionselect.ActionSelectVM.UiState.SelectedRecord

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
