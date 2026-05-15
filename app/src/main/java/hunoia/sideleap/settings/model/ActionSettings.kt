package hunoia.sideleap.settings.model

import androidx.annotation.Keep
import com.blankj.utilcode.util.ConvertUtils
import hunoia.sideleap.settings.ActionSettingsDefaults.GotoBottomStrength
import hunoia.sideleap.settings.ActionSettingsDefaults.MoveScreenHoverDelayMs
import hunoia.sideleap.settings.ActionSettingsDefaults.MoveScreenRate
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class ActionSettings(
    val moveScreen: MoveScreen = MoveScreen(),
    val previousApp: PreviousApp = PreviousApp(),
    val gotoBottom: GotoBottom = GotoBottom()
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
}
