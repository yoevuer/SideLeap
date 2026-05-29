package hunoia.luno.ui.screen.home

import hunoia.luno.R
import hunoia.luno.config.model.Action
import hunoia.luno.config.model.GestureButton
import hunoia.luno.config.model.Position
import hunoia.luno.config.model.SubGesture
import hunoia.luno.config.ConfigProvider
import hunoia.luno.core.AppContext
import hunoia.luno.action.GlobalActions

internal fun GestureButton.resolveDisplayName(): String {
    if (name.isNotEmpty()) return name
    return AppContext.get().getString(
        when (position) {
            Position.Left -> R.string.left_gesture_button
            Position.Right -> R.string.right_gesture_button
            Position.Bottom -> R.string.bottom_gesture_button
        }
    )
}

internal fun parseNumberSuffix(text: String): Int {
    val match = Regex("""(\d+)$""").find(text)
    return match?.groupValues?.get(1)?.toIntOrNull() ?: 0
}

internal suspend fun cleanSubGestureReferences(deletedId: String) {
    val sideButtons = ConfigProvider.getSideGestureButtons()
    val bottomButtons = ConfigProvider.getBottomGestureButtons()
    val subSettings = ConfigProvider.getSubGestureSettings()
    fun cleanIfSubGesture(action: Action?): Action? {
        if (action == null) return null
        if (action.value == GlobalActions.SUB_GESTURE) return null
        val cleanedLongPress = cleanIfSubGesture(action.longPressAction)
        return if (cleanedLongPress != action.longPressAction) {
            action.copy(longPressAction = cleanedLongPress)
        } else {
            action
        }
    }
    fun cleanActions(buttons: List<GestureButton>): List<GestureButton> {
        return buttons.map { button ->
            button.copy(
                slideActions = button.slideActions.copy(
                    center = button.slideActions.center.mapNotNull { cleanIfSubGesture(it) },
                    up = button.slideActions.up.mapNotNull { cleanIfSubGesture(it) },
                    down = button.slideActions.down.mapNotNull { cleanIfSubGesture(it) },
                    center2 = button.slideActions.center2.mapNotNull { cleanIfSubGesture(it) },
                    up2 = button.slideActions.up2.mapNotNull { cleanIfSubGesture(it) },
                    down2 = button.slideActions.down2.mapNotNull { cleanIfSubGesture(it) },
                ),
                longSlideActions = button.longSlideActions.copy(
                    center = button.longSlideActions.center.mapNotNull { cleanIfSubGesture(it) },
                    up = button.longSlideActions.up.mapNotNull { cleanIfSubGesture(it) },
                    down = button.longSlideActions.down.mapNotNull { cleanIfSubGesture(it) },
                    center2 = button.longSlideActions.center2.mapNotNull { cleanIfSubGesture(it) },
                    up2 = button.longSlideActions.up2.mapNotNull { cleanIfSubGesture(it) },
                    down2 = button.longSlideActions.down2.mapNotNull { cleanIfSubGesture(it) },
                ),
                tapActions = button.tapActions.copy(
                    center = button.tapActions.center.mapNotNull { cleanIfSubGesture(it) },
                    up = button.tapActions.up.mapNotNull { cleanIfSubGesture(it) },
                    down = button.tapActions.down.mapNotNull { cleanIfSubGesture(it) },
                    center2 = button.tapActions.center2.mapNotNull { cleanIfSubGesture(it) },
                    up2 = button.tapActions.up2.mapNotNull { cleanIfSubGesture(it) },
                    down2 = button.tapActions.down2.mapNotNull { cleanIfSubGesture(it) },
                )
            )
        }
    }
    ConfigProvider.updateSideGestureButtons { cleanActions(it) }
    ConfigProvider.updateBottomGestureButtons { cleanActions(it) }
    ConfigProvider.updateSubGestureSettings { settings ->
        fun clean(id: String?) = if (id == deletedId || id == GlobalActions.SUB_GESTURE) null else id
        val cleanedSubGestures = settings.subGestures.map { gesture ->
            gesture.copy(
                upActionId = clean(gesture.upActionId),
                downActionId = clean(gesture.downActionId),
                leftActionId = clean(gesture.leftActionId),
                rightActionId = clean(gesture.rightActionId),
                upRightActionId = clean(gesture.upRightActionId),
                downRightActionId = clean(gesture.downRightActionId),
                downLeftActionId = clean(gesture.downLeftActionId),
                upLeftActionId = clean(gesture.upLeftActionId),
            )
        }
        settings.copy(subGestures = cleanedSubGestures)
    }
}
