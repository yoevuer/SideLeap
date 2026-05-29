package hunoia.luno.ui.screen.actionselect

import hunoia.luno.action.Action
import hunoia.luno.quicklaunch.model.AppInfo
import hunoia.luno.quicklaunch.model.LauncherInfo
import hunoia.luno.ui.screen.actionselect.ActionSelectVM.UiState.SelectedRecord

internal const val MAX_SELECT_COUNT = 5
internal const val LONG_SLIDE_SOFT_MAX_SELECT_COUNT = 50

internal fun canActionEnabled(
    selectedRecord: SelectedRecord,
    item: Action,
    maxSelectCount: Int
): Boolean {
    if (maxSelectCount <= 1) return true
    return !(selectedRecord.size >= maxSelectCount && !selectedRecord.isSelected(item))
}

internal fun canAppInfoEnabled(
    selectedRecord: SelectedRecord,
    item: AppInfo,
    maxSelectCount: Int
): Boolean {
    if (maxSelectCount <= 1) return true
    return !(selectedRecord.size >= maxSelectCount && !selectedRecord.isSelected(item))
}

internal fun canLauncherInfoEnabled(
    selectedRecord: SelectedRecord,
    item: LauncherInfo,
    maxSelectCount: Int
): Boolean {
    if (maxSelectCount <= 1) return true
    return !(selectedRecord.size >= maxSelectCount && !item.shortcuts.any { selectedRecord.isSelected(it) })
}

internal fun canShortcutInfoEnabled(
    selectedRecord: SelectedRecord,
    item: LauncherInfo.ShortcutInfo,
    maxSelectCount: Int
): Boolean {
    if (maxSelectCount <= 1) return true
    return !(selectedRecord.size >= maxSelectCount && !selectedRecord.isSelected(item))
}
