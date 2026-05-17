package hunoia.sideleap.gesture

import hunoia.sideleap.action.Action
import hunoia.sideleap.action.GestureActions
import hunoia.sideleap.action.GestureActionsDefaults
import hunoia.sideleap.settings.model.ActionPanelStyles
import hunoia.sideleap.settings.model.LongSlideActionPanelStyles

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
