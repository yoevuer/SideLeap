package hunoia.luno.settings.model

import androidx.annotation.Keep
import hunoia.luno.core.DensityProvider
import hunoia.luno.settings.defaults.ActionSettingsDefaults.GotoBottomStrength
import hunoia.luno.settings.defaults.ActionSettingsDefaults.HideGestureButtonDelayMs
import hunoia.luno.settings.defaults.ActionSettingsDefaults.MoveScreenHoverDelayMs
import hunoia.luno.settings.defaults.ActionSettingsDefaults.VolumeScrubHorizontalEnabled
import hunoia.luno.settings.defaults.ActionSettingsDefaults.VolumeScrubStepThresholdDp
import hunoia.luno.settings.defaults.ActionSettingsDefaults.MoveScreenRate
import hunoia.luno.settings.defaults.ActionSettingsDefaults.PasswordDefaultLength
import hunoia.luno.settings.defaults.ActionSettingsDefaults.PasswordDigitsEnabled
import hunoia.luno.settings.defaults.ActionSettingsDefaults.PasswordLowercaseEnabled
import hunoia.luno.settings.defaults.ActionSettingsDefaults.PasswordSymbolsEnabled
import hunoia.luno.settings.defaults.ActionSettingsDefaults.PasswordUppercaseEnabled
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class ActionSettings(
    val moveScreen: MoveScreen = MoveScreen(),
    val previousApp: PreviousApp = PreviousApp(),
    val gotoBottom: GotoBottom = GotoBottom(),
    val passwordGenerator: PasswordGenerator = PasswordGenerator(),
    val hideGestureButton: HideGestureButton = HideGestureButton(),
    val volumeScrub: VolumeScrub = VolumeScrub()
) {
    @Serializable
    @Keep
    data class MoveScreen(
        val rate: Float = MoveScreenRate,
        val hoverDelayMs: Long = MoveScreenHoverDelayMs,
        val radius: Int = DensityProvider.dp2px(12f)
    ) {
        enum class Action {
            Tap, DoubleTap, LongPress
        }
    }

    @Serializable
    @Keep
    data class PreviousApp(val packageNames: List<String> = emptyList())

    @Serializable
    @Keep
    data class GotoBottom(val strength: Int = GotoBottomStrength)

    @Serializable
    @Keep
    data class HideGestureButton(
        val delayMs: Long = HideGestureButtonDelayMs
    )

    @Serializable
    @Keep
    data class VolumeScrub(
        val horizontalEnabled: Boolean = VolumeScrubHorizontalEnabled,
        val stepThresholdDp: Int = VolumeScrubStepThresholdDp,
    )

    @Serializable
    @Keep
    data class PasswordGenerator(
        val length: Int = PasswordDefaultLength,
        val lowercase: Boolean = PasswordLowercaseEnabled,
        val uppercase: Boolean = PasswordUppercaseEnabled,
        val digits: Boolean = PasswordDigitsEnabled,
        val symbols: Boolean = PasswordSymbolsEnabled
    )
}
