package hunoia.sideleap.gesture

import android.os.SystemClock
import androidx.annotation.Keep
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import hunoia.sideleap.action.GestureActions
import kotlinx.serialization.Serializable

object GestureButtonDefaults {

    const val ID_DEFAULT = "1"
    const val Enabled = true
    const val Start = 0.0f
    const val End = 0.1f
    val Width = ConvertUtils.dp2px(16f)
    val SlideActions = GestureActions()
    val LongSlideActions = GestureActions()
    val TapActions = GestureActions()
    const val Color = android.graphics.Color.TRANSPARENT
    const val AlignRegion = true
    const val ExcludeSystemGestureRects = false
    const val LimitMaxExcludeSystemGestureLength = true
    val SideDefaults = listOf(
        GestureButton(
            id = ID_DEFAULT,
            position = Position.Left,
            angle = defaultGestureAngleFor(Position.Left),
            start = 0.0f,
            end = 1.0f,
            slideActions = GestureActions(center = hunoia.sideleap.action.Action.toList(hunoia.sideleap.action.GlobalActions.BACK))
        ),
        GestureButton(
            id = ID_DEFAULT,
            position = Position.Right,
            angle = defaultGestureAngleFor(Position.Right),
            start = 0.0f,
            end = 1.0f,
            slideActions = GestureActions(center = hunoia.sideleap.action.Action.toList(hunoia.sideleap.action.GlobalActions.BACK))
        )
    )
    val BottomDefaults = listOf(
        GestureButton(
            id = ID_DEFAULT,
            position = Position.Bottom,
            angle = defaultGestureAngleFor(Position.Bottom),
            enabled = false,
            start = 0.0f,
            end = 1.0f,
            slideActions = GestureActions(center = hunoia.sideleap.action.Action.toList(hunoia.sideleap.action.GlobalActions.HOME)),
            longSlideActions = GestureActions(center = hunoia.sideleap.action.Action.toList(hunoia.sideleap.action.GlobalActions.RECENT))
        )
    )
}

@Serializable
@Keep
data class GestureButton(
    val id: String,
    val position: Position,
    val angle: GestureAngle,
    val enabled: Boolean = GestureButtonDefaults.Enabled,
    val start: Float = GestureButtonDefaults.Start,
    val end: Float = GestureButtonDefaults.End,
    val width: Int = GestureButtonDefaults.Width,
    val slideActions: GestureActions = GestureButtonDefaults.SlideActions,
    val longSlideActions: GestureActions = GestureButtonDefaults.LongSlideActions,
    val tapActions: GestureActions = GestureButtonDefaults.TapActions,
    val color: Int = GestureButtonDefaults.Color,
    val alignRegion: Boolean = GestureButtonDefaults.AlignRegion,
    val excludeSystemGestureRects: Boolean = GestureButtonDefaults.ExcludeSystemGestureRects,
    val limitMaxExcludeSystemGestureLength: Boolean = GestureButtonDefaults.LimitMaxExcludeSystemGestureLength
) : Comparable<GestureButton> {

    companion object {
        const val ID_DEFAULT = GestureButtonDefaults.ID_DEFAULT

        val SideDefaults: List<GestureButton> get() = GestureButtonDefaults.SideDefaults
        val BottomDefaults: List<GestureButton> get() = GestureButtonDefaults.BottomDefaults

        fun createSidePair(): List<GestureButton> {
            val id = SystemClock.uptimeMillis().toString()
            val colorInt = ColorUtils.getRandomColor(false)
            val color = Color(colorInt).toArgb()
            val b1 = GestureButton(
                id = id,
                position = Position.Left,
                angle = defaultGestureAngleFor(Position.Left),
                color = color
            )
            val b2 = GestureButton(
                id = id,
                position = Position.Right,
                angle = defaultGestureAngleFor(Position.Right),
                color = color
            )
            return listOf(b1, b2)
        }

        fun createBottom(): GestureButton {
            val id = SystemClock.uptimeMillis().toString()
            val colorInt = ColorUtils.getRandomColor(false)
            val color = Color(colorInt).toArgb()
            return GestureButton(
                id = id,
                position = Position.Bottom,
                angle = defaultGestureAngleFor(Position.Bottom),
                color = color
            )
        }
    }

    val isDefault: Boolean = id == ID_DEFAULT

    override fun compareTo(other: GestureButton): Int {
        val idCompared = id.compareTo(other.id)
        if (idCompared == 0) {
            return position.compareTo(other.position)
        }
        return idCompared
    }
}
