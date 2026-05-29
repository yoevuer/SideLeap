package hunoia.luno.ui.screen.actionselect

import hunoia.luno.config.ConfigProvider
import hunoia.luno.config.model.Action
import hunoia.luno.config.model.GestureButton
import hunoia.luno.config.model.TriggerDirection
import hunoia.luno.action.api.shortcutInfo
import hunoia.luno.ui.navigation.ActionSelect
import java.io.File

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
