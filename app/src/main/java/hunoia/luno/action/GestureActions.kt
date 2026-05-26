package hunoia.luno.action

import androidx.annotation.Keep
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

object GestureActionsDefaults {

    val Center = emptyList<Action>()
    val Up = emptyList<Action>()
    val Down = emptyList<Action>()
    val Center2 = emptyList<Action>()
    val Up2 = emptyList<Action>()
    val Down2 = emptyList<Action>()
    const val ActionValue = GlobalActions.NONE
    val ActionNone = Action(value = ActionValue, data = "")
}

@Serializable
@Keep
data class GestureActions(
    val center: List<Action> = GestureActionsDefaults.Center,
    val up: List<Action> = GestureActionsDefaults.Up,
    val down: List<Action> = GestureActionsDefaults.Down,
    val center2: List<Action> = GestureActionsDefaults.Center2,
    val up2: List<Action> = GestureActionsDefaults.Up2,
    val down2: List<Action> = GestureActionsDefaults.Down2
)


