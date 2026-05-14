package hunoia.sideleap.settings.model

import androidx.annotation.Keep
import hunoia.sideleap.gesture.GestureAngles
import hunoia.sideleap.settings.GestureSettingsDefaults.Angles
import hunoia.sideleap.settings.GestureSettingsDefaults.IsCustomVibration
import hunoia.sideleap.settings.GestureSettingsDefaults.IsPreciseSlideType
import hunoia.sideleap.settings.GestureSettingsDefaults.LongPressTriggerDelayMs
import hunoia.sideleap.settings.GestureSettingsDefaults.LongSlideTriggerDelayMs
import hunoia.sideleap.settings.GestureSettingsDefaults.LongSlideTriggerDistance
import hunoia.sideleap.settings.GestureSettingsDefaults.LongSlideTriggerImmediately
import hunoia.sideleap.settings.GestureSettingsDefaults.SlideTriggerDistance
import hunoia.sideleap.settings.GestureSettingsDefaults.Vibrations
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class GestureSettings(
    val angles: GestureAngles = Angles,
    val slideTriggerDistance: Int = SlideTriggerDistance,
    val longPressTriggerDelayMs: Long = LongPressTriggerDelayMs,
    val longSlideTriggerDistance: Int = LongSlideTriggerDistance,
    val longSlideTriggerImmediately: Boolean = LongSlideTriggerImmediately,
    val longSlideTriggerDelayMs: Long = LongSlideTriggerDelayMs,
    val isCustomVibration: Boolean = IsCustomVibration,
    val vibrations: Vibrations = Vibrations,
    val isPreciseSlideType: Boolean = IsPreciseSlideType
)
