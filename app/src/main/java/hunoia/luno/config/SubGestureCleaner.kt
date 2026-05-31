package hunoia.luno.config

import hunoia.luno.action.api.ActionFacade
import hunoia.luno.config.model.Action
import hunoia.luno.config.model.GestureButton
import kotlinx.serialization.json.Json

object SubGestureCleaner {

    suspend fun cleanSubGestureReferences(
        deletedId: String,
        shouldRemove: (Action) -> Boolean,
    ) {
        val sideButtons = ConfigProvider.getSideGestureButtons()
        val bottomButtons = ConfigProvider.getBottomGestureButtons()
        val subSettings = ConfigProvider.getSubGestureSettings()

        fun cleanActions(buttons: List<GestureButton>): List<GestureButton> {
            return buttons.map { button ->
                button.copy(
                    slideActions = button.slideActions.copy(
                        center = button.slideActions.center.filterNot { shouldRemove(it) },
                        up = button.slideActions.up.filterNot { shouldRemove(it) },
                        down = button.slideActions.down.filterNot { shouldRemove(it) },
                        center2 = button.slideActions.center2.filterNot { shouldRemove(it) },
                        up2 = button.slideActions.up2.filterNot { shouldRemove(it) },
                        down2 = button.slideActions.down2.filterNot { shouldRemove(it) },
                    ),
                    longSlideActions = button.longSlideActions.copy(
                        center = button.longSlideActions.center.filterNot { shouldRemove(it) },
                        up = button.longSlideActions.up.filterNot { shouldRemove(it) },
                        down = button.longSlideActions.down.filterNot { shouldRemove(it) },
                        center2 = button.longSlideActions.center2.filterNot { shouldRemove(it) },
                        up2 = button.longSlideActions.up2.filterNot { shouldRemove(it) },
                        down2 = button.longSlideActions.down2.filterNot { shouldRemove(it) },
                    ),
                    tapActions = button.tapActions.copy(
                        center = button.tapActions.center.filterNot { shouldRemove(it) },
                        up = button.tapActions.up.filterNot { shouldRemove(it) },
                        down = button.tapActions.down.filterNot { shouldRemove(it) },
                        center2 = button.tapActions.center2.filterNot { shouldRemove(it) },
                        up2 = button.tapActions.up2.filterNot { shouldRemove(it) },
                        down2 = button.tapActions.down2.filterNot { shouldRemove(it) },
                    )
                )
            }
        }

        ConfigProvider.updateSideGestureButtons { cleanActions(it) }
        ConfigProvider.updateBottomGestureButtons { cleanActions(it) }
        ConfigProvider.updateSubGestureSettings { settings ->
            fun clean(action: Action?) = action?.takeUnless(shouldRemove)
            val cleanedSubGestures = settings.subGestures.map { gesture ->
                gesture.copy(
                    upAction = clean(gesture.upAction),
                    downAction = clean(gesture.downAction),
                    leftAction = clean(gesture.leftAction),
                    rightAction = clean(gesture.rightAction),
                    upRightAction = clean(gesture.upRightAction),
                    downRightAction = clean(gesture.downRightAction),
                    downLeftAction = clean(gesture.downLeftAction),
                    upLeftAction = clean(gesture.upLeftAction),
                )
            }
            settings.copy(subGestures = cleanedSubGestures)
        }
    }

    fun isSubGestureAction(action: Action): Boolean =
        action.value == ActionFacade.SUB_GESTURE

    fun matchesDeletedSubGesture(action: Action, deletedId: String): Boolean {
        if (!isSubGestureAction(action)) return false
        return try {
            val data = Json.decodeFromString<hunoia.luno.action.payload.SubGestureActionData>(action.data)
            data.id == deletedId
        } catch (_: Exception) {
            false
        }
    }
}
