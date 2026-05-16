package hunoia.sideleap.gesture

import hunoia.sideleap.action.Action
import hunoia.sideleap.action.GestureActions
import hunoia.sideleap.action.GestureActionsDefaults

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
