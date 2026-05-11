package hunoia.sideleap.entity

import androidx.annotation.Keep
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

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/18
 */

@Serializable
@Keep
data class GestureActions(
    // oho
    val center: List<Action> = Center,
    val up: List<Action> = Up,
    val down: List<Action> = Down,

    // 平行手势
    val center2: List<Action> = Center2,
    val up2: List<Action> = Up2,
    val down2: List<Action> = Down2
)

@Serializable
@Keep
data class OpenAppOrUrlData(
    val type: Int = TYPE_ACTIVITY,
    val packageName: String = "",
    val activityClassName: String = "",
    val url: String = ""
) {
    companion object {
        const val TYPE_ACTIVITY = 0
        const val TYPE_URL = 1
    }
}


@Serializable
@Keep
data class Action(
    val value: String = ActionValue,
    val data: String = "",
    @Transient
    val extra: Any? = null
) {
    companion object {
        val NONE: Action get() = ActionNone

        fun toList(vararg value: String): List<Action> {
            return value.map { Action(it) }
        }
    }
}
