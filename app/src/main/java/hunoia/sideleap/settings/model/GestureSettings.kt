package hunoia.sideleap.settings.model

import androidx.annotation.Keep
import hunoia.sideleap.settings.api.GestureSettingsDefaults.IsCustomVibration
import hunoia.sideleap.settings.api.GestureSettingsDefaults.IsPreciseSlideType
import hunoia.sideleap.settings.api.GestureSettingsDefaults.LongPressTriggerDelayMs
import hunoia.sideleap.settings.api.GestureSettingsDefaults.LongSlideTriggerDelayMs
import hunoia.sideleap.settings.api.GestureSettingsDefaults.LongSlideTriggerDistance
import hunoia.sideleap.settings.api.GestureSettingsDefaults.LongSlideTriggerImmediately
import hunoia.sideleap.settings.api.GestureSettingsDefaults.SlideTriggerDistance
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
    val isPreciseSlideType: Boolean = IsPreciseSlideType
)
