package hunoia.sideleap.settings.model

import androidx.annotation.Keep
import hunoia.sideleap.settings.api.GestureSettingsDefaults.IsCustomVibration
import hunoia.sideleap.settings.api.GestureSettingsDefaults.IsPreciseSlideType
import hunoia.sideleap.settings.api.GestureSettingsDefaults.LongPressTriggerDelayMs
import hunoia.sideleap.settings.api.GestureSettingsDefaults.LongSlideTriggerDelayMs
import hunoia.sideleap.settings.api.GestureSettingsDefaults.LongSlideTriggerDistance
import hunoia.sideleap.settings.api.GestureSettingsDefaults.LongSlideTriggerImmediately
import hunoia.sideleap.settings.api.GestureSettingsDefaults.SlideTriggerDistance
import hunoia.sideleap.settings.api.GestureSettingsDefaults.VirtualMouseAcceleration
import hunoia.sideleap.settings.api.GestureSettingsDefaults.VirtualMouseClickAnimationEnabled
import hunoia.sideleap.settings.api.GestureSettingsDefaults.VirtualMouseContinuousMode
import hunoia.sideleap.settings.api.GestureSettingsDefaults.VirtualMouseContinuousModeTimeoutMs
import hunoia.sideleap.settings.api.GestureSettingsDefaults.VirtualMouseCursorAlpha
import hunoia.sideleap.settings.api.GestureSettingsDefaults.VirtualMouseCursorSizeDp
import hunoia.sideleap.settings.api.GestureSettingsDefaults.VirtualMouseEdgeCancelThresholdDp
import hunoia.sideleap.settings.api.GestureSettingsDefaults.VirtualMouseInitialYRatio
import hunoia.sideleap.settings.api.GestureSettingsDefaults.VirtualMouseLongPressDelayMs
import hunoia.sideleap.settings.api.GestureSettingsDefaults.VirtualMouseLongPressEnabled
import hunoia.sideleap.settings.api.GestureSettingsDefaults.VirtualMouseMovementDeadZoneDp
import hunoia.sideleap.settings.api.GestureSettingsDefaults.VirtualMouseSensitivityX
import hunoia.sideleap.settings.api.GestureSettingsDefaults.VirtualMouseSensitivityY
import hunoia.sideleap.settings.api.GestureSettingsDefaults.Vibrations as DefaultVibrations
import hunoia.sideleap.system.vibration.Vibrations
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
        val trailStyle: VirtualMouseTrailStyle = VirtualMouseTrailStyle.Dots,
        val movementDeadZoneDp: Int = VirtualMouseMovementDeadZoneDp,
        val longPressEnabled: Boolean = VirtualMouseLongPressEnabled,
        val longPressDelayMs: Long = VirtualMouseLongPressDelayMs,
    )

    @Serializable
    @Keep
    enum class VirtualMouseTrailStyle {
        None,
        Dots,
        LightBand,
    }
}
