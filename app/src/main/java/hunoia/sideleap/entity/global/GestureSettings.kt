package hunoia.sideleap.entity.global

import androidx.annotation.Keep
import hunoia.sideleap.constant.GestureSettingsDefaults.Angles
import hunoia.sideleap.constant.GestureSettingsDefaults.IsCustomVibration
import hunoia.sideleap.constant.GestureSettingsDefaults.IsPreciseSlideType
import hunoia.sideleap.constant.GestureSettingsDefaults.LongPressTriggerDelayMs
import hunoia.sideleap.constant.GestureSettingsDefaults.LongSlideTriggerDelayMs
import hunoia.sideleap.constant.GestureSettingsDefaults.LongSlideTriggerDistance
import hunoia.sideleap.constant.GestureSettingsDefaults.LongSlideTriggerImmediately
import hunoia.sideleap.constant.GestureSettingsDefaults.SlideTriggerDistance
import hunoia.sideleap.constant.GestureSettingsDefaults.Vibrations
import hunoia.sideleap.entity.GestureAngles
import hunoia.sideleap.entity.Vibrations
import kotlinx.serialization.Serializable

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/25
 */
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