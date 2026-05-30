package hunoia.luno.config.model

import android.graphics.Color
import androidx.annotation.Keep
import hunoia.luno.bridge.DensityProvider
import hunoia.luno.config.defaults.GestureSettingsDefaults.SubGestureTimeoutMs
import hunoia.luno.config.model.SubGestureDirection
import hunoia.luno.bridge.vibration.VibrationEffects
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class SubGesture(
    val id: String,
    val name: String = "",
    val angle: SubGestureAngle = SubGestureAngle(),
    val upActionId: String? = null,
    val downActionId: String? = null,
    val leftActionId: String? = null,
    val rightActionId: String? = null,
    val upRightActionId: String? = null,
    val downRightActionId: String? = null,
    val downLeftActionId: String? = null,
    val upLeftActionId: String? = null,
    val enabled: Boolean = true,
    val color: Int = Color.TRANSPARENT,
    val vibrate: Boolean = true,
    val vibrateImmediately: Boolean = false,
    val vibrationEffect: VibrationEffects = VibrationEffects.Click,
    val customVibrationMs: Long = 50L,
    val timeoutMs: Long = SubGestureTimeoutMs,
    val triggerDistance: Int = DensityProvider.dp2px(30f),
) {
    fun actionFor(direction: SubGestureDirection): String? = when (direction) {
        SubGestureDirection.Up -> upActionId
        SubGestureDirection.Down -> downActionId
        SubGestureDirection.Left -> leftActionId
        SubGestureDirection.Right -> rightActionId
        SubGestureDirection.UpRight -> upRightActionId
        SubGestureDirection.DownRight -> downRightActionId
        SubGestureDirection.DownLeft -> downLeftActionId
        SubGestureDirection.UpLeft -> upLeftActionId
    }

    fun withAction(direction: SubGestureDirection, actionId: String?): SubGesture = when (direction) {
        SubGestureDirection.Up -> copy(upActionId = actionId)
        SubGestureDirection.Down -> copy(downActionId = actionId)
        SubGestureDirection.Left -> copy(leftActionId = actionId)
        SubGestureDirection.Right -> copy(rightActionId = actionId)
        SubGestureDirection.UpRight -> copy(upRightActionId = actionId)
        SubGestureDirection.DownRight -> copy(downRightActionId = actionId)
        SubGestureDirection.DownLeft -> copy(downLeftActionId = actionId)
        SubGestureDirection.UpLeft -> copy(upLeftActionId = actionId)
    }
}
