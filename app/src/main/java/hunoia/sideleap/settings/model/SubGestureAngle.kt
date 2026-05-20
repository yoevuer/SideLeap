package hunoia.sideleap.settings.model

import androidx.annotation.Keep
import androidx.compose.ui.geometry.Offset
import hunoia.sideleap.gesture.SubGestureDirection
import kotlinx.serialization.Serializable
import kotlin.math.atan2
import kotlin.math.PI

@Serializable
@Keep
data class SubGestureAngle(
    val boundaries: List<Float> = listOf(
        0.0625f, 0.1875f, 0.3125f, 0.4375f,
        0.5625f, 0.6875f, 0.8125f, 0.9375f,
    )
) {
    init {
        require(boundaries.size == 8)
        require(boundaries.all { it >= 0f && it < 1f })
        require(boundaries.zipWithNext().all { (a, b) -> a < b })
    }

    fun directionOf(offset: Offset): SubGestureDirection {
        val norm = normalizedAngle(offset)
        val index = boundaries.indexOfFirst { norm < it }
        val sectorIndex = if (index == -1) 0 else index
        return SECTOR_DIRECTIONS[sectorIndex]
    }

    private fun normalizedAngle(offset: Offset): Float {
        val angle = atan2(-offset.y.toDouble(), offset.x.toDouble())
        var norm = (angle / (2 * PI)).toFloat()
        if (norm < 0f) norm += 1f
        return norm
    }

    companion object {
        val SECTOR_DIRECTIONS = listOf(
            SubGestureDirection.Right,
            SubGestureDirection.UpRight,
            SubGestureDirection.Up,
            SubGestureDirection.UpLeft,
            SubGestureDirection.Left,
            SubGestureDirection.DownLeft,
            SubGestureDirection.Down,
            SubGestureDirection.DownRight,
        )
    }
}

fun SubGestureAngle.copyNewNoGap(index: Int, newP: Float): SubGestureAngle {
    val p = newP.coerceIn(0f, 1f)
    val prev = if (index == 0) boundaries.last() - 1f else boundaries[index - 1]
    val next = if (index == 7) boundaries.first() + 1f else boundaries[index + 1]
    val clamped = p.coerceIn(prev, next)
    val wrapped = if (clamped < 0f) clamped + 1f else if (clamped >= 1f) clamped - 1f else clamped
    val newBoundaries = boundaries.toMutableList()
    newBoundaries[index] = wrapped
    return SubGestureAngle(boundaries = newBoundaries)
}
