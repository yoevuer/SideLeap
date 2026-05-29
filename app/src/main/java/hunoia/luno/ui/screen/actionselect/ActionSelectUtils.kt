package hunoia.luno.ui.screen.actionselect

import android.content.Context
import hunoia.luno.R
import hunoia.luno.config.model.Action
import hunoia.luno.config.model.Position
import hunoia.luno.config.model.TriggerDirection
import hunoia.luno.core.AppContext
import hunoia.luno.quicklaunch.model.AppInfo
import hunoia.luno.quicklaunch.model.LauncherInfo
import hunoia.luno.ui.navigation.ActionSelect
import hunoia.luno.ui.screen.actionselect.UiState.SelectedRecord

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

internal fun createTitle(actionSelect: ActionSelect): String {
    val context = AppContext.get()
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
