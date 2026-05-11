package hunoia.sideleap.entity

import androidx.annotation.Keep
import hunoia.sideleap.constant.GlobalSettings
import hunoia.sideleap.constant.VibrationDefaults.ActionPanelEnabled
import hunoia.sideleap.constant.VibrationDefaults.CustomVibrationMs
import hunoia.sideleap.constant.VibrationDefaults.LongSlideEnabled
import hunoia.sideleap.constant.VibrationDefaults.MoveScreenEnabled
import hunoia.sideleap.constant.VibrationDefaults.PredefinedEffect
import hunoia.sideleap.constant.VibrationDefaults.SlideEnabled
import hunoia.sideleap.constant.VibrationDefaults.VibrateImmediately
import kotlinx.serialization.Serializable

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/17
 */
@Serializable
@Keep
data class Vibrations(
    val slideEnabled: Boolean = SlideEnabled,
    val longSlideEnabled: Boolean = LongSlideEnabled,
    val actionPanelEnabled: Boolean = ActionPanelEnabled,
    val moveScreenEnabled: Boolean = MoveScreenEnabled,
    val predefinedEffect: VibrationEffects = PredefinedEffect,
    val customVibrationMs: Long = CustomVibrationMs,
    // 识别到手势立即振动
    val vibrateImmediately: Boolean = VibrateImmediately
) {
    init {
        val min = GlobalSettings.MinVibrationDurationMs
        val max = GlobalSettings.MaxVibrationDurationMs
        require(customVibrationMs in min..max) {
            "Illegal customVibrationMs: $customVibrationMs, min: $min, max: $max"
        }
    }
}