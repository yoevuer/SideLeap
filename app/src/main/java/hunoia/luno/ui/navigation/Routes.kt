package hunoia.luno.ui.navigation

import androidx.annotation.Keep
import hunoia.luno.gesture.Position
import hunoia.luno.gesture.SubGestureDirection
import hunoia.luno.gesture.TriggerDirection
import kotlinx.serialization.Serializable



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
data object Home

@Keep
@Serializable
data class SubGestureEditor(
    val subGestureId: String
)

@Keep
@Serializable
data class SubGestureActionSelect(
    val id: String,
    val direction: SubGestureDirection
)


