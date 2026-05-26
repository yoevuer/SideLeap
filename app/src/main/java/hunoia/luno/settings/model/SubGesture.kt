package hunoia.luno.settings.model

import android.graphics.Color
import androidx.annotation.Keep
import hunoia.luno.action.Action
import hunoia.luno.gesture.SubGestureDirection
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class SubGesture(
    val id: String,
    val name: String = "",
    val angle: SubGestureAngle = SubGestureAngle(),
    val upAction: Action? = null,
    val downAction: Action? = null,
    val leftAction: Action? = null,
    val rightAction: Action? = null,
    val upRightAction: Action? = null,
    val downRightAction: Action? = null,
    val downLeftAction: Action? = null,
    val upLeftAction: Action? = null,
    val enabled: Boolean = true,
    val color: Int = Color.TRANSPARENT,
) {
    fun actionFor(direction: SubGestureDirection): Action? = when (direction) {
        SubGestureDirection.Up -> upAction
        SubGestureDirection.Down -> downAction
        SubGestureDirection.Left -> leftAction
        SubGestureDirection.Right -> rightAction
        SubGestureDirection.UpRight -> upRightAction
        SubGestureDirection.DownRight -> downRightAction
        SubGestureDirection.DownLeft -> downLeftAction
        SubGestureDirection.UpLeft -> upLeftAction
    }

    fun withAction(direction: SubGestureDirection, action: Action?): SubGesture = when (direction) {
        SubGestureDirection.Up -> copy(upAction = action)
        SubGestureDirection.Down -> copy(downAction = action)
        SubGestureDirection.Left -> copy(leftAction = action)
        SubGestureDirection.Right -> copy(rightAction = action)
        SubGestureDirection.UpRight -> copy(upRightAction = action)
        SubGestureDirection.DownRight -> copy(downRightAction = action)
        SubGestureDirection.DownLeft -> copy(downLeftAction = action)
        SubGestureDirection.UpLeft -> copy(upLeftAction = action)
    }
}
