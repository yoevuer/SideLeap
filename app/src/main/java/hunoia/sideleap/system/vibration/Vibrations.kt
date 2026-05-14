package hunoia.sideleap.system.vibration

import androidx.annotation.Keep
import hunoia.sideleap.constant.GlobalSettings
import kotlinx.serialization.Serializable

object VibrationDefaults {

    const val SlideEnabled = true
    const val LongSlideEnabled = true
    const val ActionPanelEnabled = true
    const val MoveScreenEnabled = true
    val PredefinedEffect = VibrationEffects.Click
    const val CustomVibrationMs = 50L
    const val VibrateImmediately = false
}

@Serializable
@Keep
data class Vibrations(
    val slideEnabled: Boolean = VibrationDefaults.SlideEnabled,
    val longSlideEnabled: Boolean = VibrationDefaults.LongSlideEnabled,
    val actionPanelEnabled: Boolean = VibrationDefaults.ActionPanelEnabled,
    val moveScreenEnabled: Boolean = VibrationDefaults.MoveScreenEnabled,
    val predefinedEffect: VibrationEffects = VibrationDefaults.PredefinedEffect,
    val customVibrationMs: Long = VibrationDefaults.CustomVibrationMs,
    val vibrateImmediately: Boolean = VibrationDefaults.VibrateImmediately
) {
    init {
        val min = GlobalSettings.MinVibrationDurationMs
        val max = GlobalSettings.MaxVibrationDurationMs
        require(customVibrationMs in min..max) {
            "Illegal customVibrationMs: $customVibrationMs, min: $min, max: $max"
        }
    }
}
