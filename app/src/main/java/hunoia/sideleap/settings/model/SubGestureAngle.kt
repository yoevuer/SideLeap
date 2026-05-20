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
    }

    fun directionOf(offset: Offset): SubGestureDirection {
        val norm = normalizedAngle(offset)
        for (i in boundaries.indices) {
            val start = boundaries[i]
            val end = boundaries[(i + 1) % 8]
            val inSector = if (start <= end) norm >= start && norm < end
                           else norm >= start || norm < end
            if (inSector) return SECTOR_DIRECTIONS[i]
        }
        val nearest = boundaries.indices.minByOrNull { i ->
            var d = norm - boundaries[i]
            if (d < 0f) d += 1f; d
        } ?: 0
        return SECTOR_DIRECTIONS[nearest]
    }

    private fun normalizedAngle(offset: Offset): Float {
        val angle = atan2(-offset.y.toDouble(), offset.x.toDouble())
        var norm = (angle / (2 * PI)).toFloat()
        if (norm < 0f) norm += 1f
        return norm
    }

    fun sectorWidth(index: Int): Float {
        val s = boundaries[index]
        val e = boundaries[(index + 1) % 8]
        return if (e >= s) (e - s) * 360f else (e + 1f - s) * 360f
    }

    companion object {
        val SECTOR_DIRECTIONS = listOf(
            SubGestureDirection.UpRight,
            SubGestureDirection.Up,
            SubGestureDirection.UpLeft,
            SubGestureDirection.Left,
            SubGestureDirection.DownLeft,
            SubGestureDirection.Down,
            SubGestureDirection.DownRight,
            SubGestureDirection.Right,
        )
    }
}

fun SubGestureAngle.copyNewNoGap(index: Int, newP: Float): SubGestureAngle {
    val p = ((newP % 1f) + 1f) % 1f
    val prev = boundaries[(index + 7) % 8]
    val next = boundaries[(index + 1) % 8]
    val distToPrev = minOf(
        kotlin.math.abs(p - prev),
        kotlin.math.abs(p + 1f - prev),
        kotlin.math.abs(p - 1f - prev)
    )
    val distToNext = minOf(
        kotlin.math.abs(p - next),
        kotlin.math.abs(p + 1f - next),
        kotlin.math.abs(p - 1f - next)
    )
    val inRange = if (prev < next) p in prev..next
                  else p >= prev || p <= next
    val clamped = if (inRange) p
                  else if (distToPrev < distToNext) prev
                  else next
    val newBoundaries = boundaries.toMutableList()
    newBoundaries[index] = clamped
    return SubGestureAngle(boundaries = newBoundaries)
}
