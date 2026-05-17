package hunoia.sideleap.settings.model

import androidx.annotation.Keep
import com.blankj.utilcode.util.ConvertUtils
import hunoia.sideleap.settings.api.ActionSettingsDefaults.GotoBottomStrength
import hunoia.sideleap.settings.api.ActionSettingsDefaults.HideGestureButtonDelayMs
import hunoia.sideleap.settings.api.ActionSettingsDefaults.MoveScreenHoverDelayMs
import hunoia.sideleap.settings.api.ActionSettingsDefaults.MoveScreenRate
import hunoia.sideleap.settings.api.ActionSettingsDefaults.PasswordDefaultLength
import hunoia.sideleap.settings.api.ActionSettingsDefaults.PasswordDigitsEnabled
import hunoia.sideleap.settings.api.ActionSettingsDefaults.PasswordLowercaseEnabled
import hunoia.sideleap.settings.api.ActionSettingsDefaults.PasswordSymbolsEnabled
import hunoia.sideleap.settings.api.ActionSettingsDefaults.PasswordUppercaseEnabled
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class ActionSettings(
    val moveScreen: MoveScreen = MoveScreen(),
    val previousApp: PreviousApp = PreviousApp(),
    val gotoBottom: GotoBottom = GotoBottom(),
    val passwordGenerator: PasswordGenerator = PasswordGenerator(),
    val hideGestureButton: HideGestureButton = HideGestureButton()
) {
    @Serializable
    @Keep
    data class MoveScreen(
        val rate: Float = MoveScreenRate,
        val hoverDelayMs: Long = MoveScreenHoverDelayMs,
        val radius: Int = ConvertUtils.dp2px(12f)
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
    data class PasswordGenerator(
        val length: Int = PasswordDefaultLength,
        val lowercase: Boolean = PasswordLowercaseEnabled,
        val uppercase: Boolean = PasswordUppercaseEnabled,
        val digits: Boolean = PasswordDigitsEnabled,
        val symbols: Boolean = PasswordSymbolsEnabled
    )
}
