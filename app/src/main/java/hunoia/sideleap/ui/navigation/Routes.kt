package hunoia.sideleap.ui.navigation

import androidx.annotation.Keep
import hunoia.sideleap.gesture.Position
import hunoia.sideleap.gesture.SubGestureDirection
import hunoia.sideleap.gesture.TriggerDirection
import kotlinx.serialization.Serializable

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/12/7
 */

@Keep
@Serializable
data class ActionSelect(
    val gestureButtonId: String,
    val position: Position,
    val direction: TriggerDirection,
    val isLongSlide: Boolean,
    val isSideButton: Boolean,
    val isTap: Boolean = false
)

@Keep
@Serializable
data class IconResize(val ids: List<String>)

@Keep
@Serializable
data object AdvancedSettings


@Serializable
@Keep
data class GestureButtonSettings(
    val buttonId: String,
    val position: Position
) {
    val isSideButton: Boolean
        get() = position == Position.Left || position == Position.Right
}

@Keep
@Serializable
data object GestureSettings

@Keep
@Serializable
data object Home

@Keep
@Serializable
data object Unlock

@Keep
@Serializable
data object FrozenAppManage

@Keep
@Serializable
data class SubGestureEditor(
    val subGestureId: String
)


