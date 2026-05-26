package hunoia.luno.settings.model

import androidx.annotation.Keep
import hunoia.luno.settings.defaults.GestureSettingsDefaults.IsCustomVibration
import hunoia.luno.settings.defaults.GestureSettingsDefaults.IsPreciseSlideType
import hunoia.luno.settings.defaults.GestureSettingsDefaults.LongPressTriggerDelayMs
import hunoia.luno.settings.defaults.GestureSettingsDefaults.LongSlideTriggerDelayMs
import hunoia.luno.settings.defaults.GestureSettingsDefaults.LongSlideTriggerDistance
import hunoia.luno.settings.defaults.GestureSettingsDefaults.LongSlideTriggerImmediately
import hunoia.luno.settings.defaults.GestureSettingsDefaults.SlideTriggerDistance
import hunoia.luno.settings.defaults.GestureSettingsDefaults.SubGestureTimeoutMs
import hunoia.luno.settings.defaults.GestureSettingsDefaults.SubGestureTriggerDistance
import hunoia.luno.settings.defaults.GestureSettingsDefaults.VirtualMouseAcceleration
import hunoia.luno.settings.defaults.GestureSettingsDefaults.VirtualMouseClickAnimationEnabled
import hunoia.luno.settings.defaults.GestureSettingsDefaults.VirtualMouseContinuousMode
import hunoia.luno.settings.defaults.GestureSettingsDefaults.VirtualMouseContinuousModeTimeoutMs
import hunoia.luno.settings.defaults.GestureSettingsDefaults.VirtualMouseCursorAlpha
import hunoia.luno.settings.defaults.GestureSettingsDefaults.VirtualMouseCursorSizeDp
import hunoia.luno.settings.defaults.GestureSettingsDefaults.VirtualMouseEdgeCancelThresholdDp
import hunoia.luno.settings.defaults.GestureSettingsDefaults.VirtualMouseInitialYRatio
import hunoia.luno.settings.defaults.GestureSettingsDefaults.VirtualMouseLongPressDelayMs
import hunoia.luno.settings.defaults.GestureSettingsDefaults.VirtualMouseLongPressEnabled
import hunoia.luno.settings.defaults.GestureSettingsDefaults.VirtualMouseLongPressMoveToleranceDp
import hunoia.luno.settings.defaults.GestureSettingsDefaults.VirtualMouseMovementDeadZoneDp
import hunoia.luno.settings.defaults.GestureSettingsDefaults.VirtualMouseSensitivityX
import hunoia.luno.settings.defaults.GestureSettingsDefaults.VirtualMouseSensitivityY
import hunoia.luno.settings.defaults.GestureSettingsDefaults.VirtualMouseTrailAlpha
import hunoia.luno.settings.defaults.GestureSettingsDefaults.VirtualMouseTrailStrength
import hunoia.luno.settings.defaults.GestureSettingsDefaults.Vibrations as DefaultVibrations
import hunoia.luno.system.vibration.Vibrations
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class GestureSettings(
    val slideTriggerDistance: Int = SlideTriggerDistance,
    val longPressTriggerDelayMs: Long = LongPressTriggerDelayMs,
    val longSlideTriggerDistance: Int = LongSlideTriggerDistance,
    val longSlideTriggerImmediately: Boolean = LongSlideTriggerImmediately,
    val longSlideTriggerDelayMs: Long = LongSlideTriggerDelayMs,
    val isCustomVibration: Boolean = IsCustomVibration,
    val vibrations: Vibrations = DefaultVibrations,
    val isPreciseSlideType: Boolean = IsPreciseSlideType,
    val subGestureTimeoutMs: Long = SubGestureTimeoutMs,
    val subGestureTriggerDistance: Int = SubGestureTriggerDistance,
    val virtualMouse: VirtualMouse = VirtualMouse()
) {
    @Serializable
    @Keep
    data class VirtualMouse(
        val sensitivityX: Float = VirtualMouseSensitivityX,
        val sensitivityY: Float = VirtualMouseSensitivityY,
        val acceleration: Float = VirtualMouseAcceleration,
        val initialYRatio: Float = VirtualMouseInitialYRatio,
        val edgeCancelThresholdDp: Int = VirtualMouseEdgeCancelThresholdDp,
        val continuousMode: Boolean = VirtualMouseContinuousMode,
        val continuousModeTimeoutMs: Long = VirtualMouseContinuousModeTimeoutMs,
        val cursorSizeDp: Int = VirtualMouseCursorSizeDp,
        val cursorAlpha: Float = VirtualMouseCursorAlpha,
        val clickAnimationEnabled: Boolean = VirtualMouseClickAnimationEnabled,
        val trailStyle: VirtualMouseTrailStyle = VirtualMouseTrailStyle.LightBand,
        val trailStrength: Float = VirtualMouseTrailStrength,
        val trailAlpha: Float = VirtualMouseTrailAlpha,
        val movementDeadZoneDp: Int = VirtualMouseMovementDeadZoneDp,
        val longPressEnabled: Boolean = VirtualMouseLongPressEnabled,
        val longPressDelayMs: Long = VirtualMouseLongPressDelayMs,
        val longPressMoveToleranceDp: Int = VirtualMouseLongPressMoveToleranceDp,
    )

    @Serializable
    @Keep
    enum class VirtualMouseTrailStyle {
        None,
        Dots,
        LightBand,
    }
}
