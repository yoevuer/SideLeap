package hunoia.sideleap.gesture

import androidx.annotation.Keep
import hunoia.sideleap.action.Action
import hunoia.sideleap.constant.GestureActionsDefaults.ActionNone
import hunoia.sideleap.constant.GestureActionsDefaults.ActionValue
import hunoia.sideleap.constant.GestureActionsDefaults.Center
import hunoia.sideleap.constant.GestureActionsDefaults.Center2
import hunoia.sideleap.constant.GestureActionsDefaults.Down
import hunoia.sideleap.constant.GestureActionsDefaults.Down2
import hunoia.sideleap.constant.GestureActionsDefaults.Up
import hunoia.sideleap.constant.GestureActionsDefaults.Up2
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Keep
data class GestureActions(
    val center: List<Action> = Center,
    val up: List<Action> = Up,
    val down: List<Action> = Down,
    val center2: List<Action> = Center2,
    val up2: List<Action> = Up2,
    val down2: List<Action> = Down2
)

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
    return isEmpty() || first() == ActionNone
}
