package hunoia.sideleap.entity

import android.os.SystemClock
import androidx.annotation.Keep
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import hunoia.sideleap.constant.GestureButtonDefaults
import hunoia.sideleap.constant.GestureButtonDefaults.AlignRegion
import hunoia.sideleap.constant.GestureButtonDefaults.Color
import hunoia.sideleap.constant.GestureButtonDefaults.Enabled
import hunoia.sideleap.constant.GestureButtonDefaults.End
import hunoia.sideleap.constant.GestureButtonDefaults.ExcludeSystemGestureRects
import hunoia.sideleap.constant.GestureButtonDefaults.LimitMaxExcludeSystemGestureLength
import hunoia.sideleap.constant.GestureButtonDefaults.LongSlideActions
import hunoia.sideleap.constant.GestureButtonDefaults.SlideActions
import hunoia.sideleap.constant.GestureButtonDefaults.Start
import hunoia.sideleap.constant.GestureButtonDefaults.Width
import com.blankj.utilcode.util.ColorUtils
import kotlinx.serialization.Serializable

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/18
 */
@Serializable
@Keep
data class GestureButton(
    val id: String,
    val position: Position,
    val enabled: Boolean = Enabled,
    val start: Float = Start,
    val end: Float = End,
    val width: Int = Width,
    val slideActions: GestureActions = SlideActions,
    val longSlideActions: GestureActions = LongSlideActions,
    val color: Int = Color,
    val alignRegion: Boolean = AlignRegion,
    val excludeSystemGestureRects: Boolean = ExcludeSystemGestureRects,
    val limitMaxExcludeSystemGestureLength: Boolean = LimitMaxExcludeSystemGestureLength
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
                color = color
            )
            val b2 = GestureButton(
                id = id,
                position = Position.Right,
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
                color = color
            )
        }
    }

    val isDefault: Boolean = id == ID_DEFAULT

    override fun compareTo(other: GestureButton): Int {
        val idCompared = id.compareTo(other.id)
        if (idCompared == 0) {
            // id相同，意味着是一组的，比较position
            return position.compareTo(other.position)
        }
        return idCompared
    }
}