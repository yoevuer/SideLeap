package hunoia.luno.gesture

import hunoia.luno.action.Action
import hunoia.luno.action.GestureActions
import hunoia.luno.action.GestureActionsDefaults
import hunoia.luno.config.model.ActionPanelStyles
import hunoia.luno.config.model.LongSlideActionPanelStyles
import hunoia.luno.config.model.TriggerDirection

fun GestureActions.actionsBy(direction: TriggerDirection): List<Action> {
    return when (direction) {
        TriggerDirection.Up -> up
        TriggerDirection.Center -> center
        TriggerDirection.Down -> down
        TriggerDirection.Up2 -> up2
        TriggerDirection.Center2 -> emptyList()
        TriggerDirection.Down2 -> down2
    }
}

fun List<Action>.isEmptyOrNone(): Boolean {
    return isEmpty() || first() == GestureActionsDefaults.ActionNone
}

fun LongSlideActionPanelStyles.styleBy(direction: TriggerDirection): ActionPanelStyles {
    return when (direction) {
        TriggerDirection.Center -> center
        TriggerDirection.Up -> up
        TriggerDirection.Down -> down
        TriggerDirection.Up2 -> up2
        TriggerDirection.Down2 -> down2
        TriggerDirection.Center2 -> center
    }
}
