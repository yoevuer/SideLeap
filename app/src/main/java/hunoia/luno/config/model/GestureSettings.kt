package hunoia.luno.config.model

import androidx.annotation.Keep
import hunoia.luno.config.defaults.GestureSettingsDefaults.PointerAcceleration
import hunoia.luno.config.defaults.GestureSettingsDefaults.PointerClickAnimationEnabled
import hunoia.luno.config.defaults.GestureSettingsDefaults.PointerContinuousMode
import hunoia.luno.config.defaults.GestureSettingsDefaults.PointerContinuousModeTimeoutMs
import hunoia.luno.config.defaults.GestureSettingsDefaults.PointerCursorAlpha
import hunoia.luno.config.defaults.GestureSettingsDefaults.PointerCursorSizeDp
import hunoia.luno.config.defaults.GestureSettingsDefaults.PointerEdgeCancelThresholdDp
import hunoia.luno.config.defaults.GestureSettingsDefaults.PointerInitialYRatio
import hunoia.luno.config.defaults.GestureSettingsDefaults.PointerLongPressDelayMs
import hunoia.luno.config.defaults.GestureSettingsDefaults.PointerLongPressEnabled
import hunoia.luno.config.defaults.GestureSettingsDefaults.PointerLongPressMoveToleranceDp
import hunoia.luno.config.defaults.GestureSettingsDefaults.PointerMovementDeadZoneDp
import hunoia.luno.config.defaults.GestureSettingsDefaults.PointerSensitivityX
import hunoia.luno.config.defaults.GestureSettingsDefaults.PointerSensitivityY
import hunoia.luno.config.defaults.GestureSettingsDefaults.PointerTrailAlpha
import hunoia.luno.config.defaults.GestureSettingsDefaults.PointerTrailStrength
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class GestureSettings(
    val actionPanelVibrate: Boolean = true,
    val pointer: Pointer = Pointer()
) {
    @Serializable
    @Keep
    data class Pointer(
        val sensitivityX: Float = PointerSensitivityX,
        val sensitivityY: Float = PointerSensitivityY,
        val acceleration: Float = PointerAcceleration,
        val initialYRatio: Float = PointerInitialYRatio,
        val edgeCancelThresholdDp: Int = PointerEdgeCancelThresholdDp,
        val continuousMode: Boolean = PointerContinuousMode,
        val continuousModeTimeoutMs: Long = PointerContinuousModeTimeoutMs,
        val cursorSizeDp: Int = PointerCursorSizeDp,
        val cursorAlpha: Float = PointerCursorAlpha,
        val clickAnimationEnabled: Boolean = PointerClickAnimationEnabled,
        val trailStyle: PointerTrailStyle = PointerTrailStyle.LightBand,
        val trailStrength: Float = PointerTrailStrength,
        val trailAlpha: Float = PointerTrailAlpha,
        val movementDeadZoneDp: Int = PointerMovementDeadZoneDp,
        val longPressEnabled: Boolean = PointerLongPressEnabled,
        val longPressDelayMs: Long = PointerLongPressDelayMs,
        val longPressMoveToleranceDp: Int = PointerLongPressMoveToleranceDp,
    )

    @Serializable
    @Keep
    enum class PointerTrailStyle {
        None,
        Dots,
        LightBand,
    }
}
