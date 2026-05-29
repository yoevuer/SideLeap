package hunoia.luno.ui.settings.gesture.angle

import androidx.compose.ui.geometry.Offset
import hunoia.luno.config.model.GESTURE_ANGLE_BASE
import hunoia.luno.config.model.Position
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

fun calcOffset(center: Offset, radius: Float, degree: Float): Offset {
    val radians = Math.toRadians(degree.toDouble())
    return Offset(
        x = center.x + radius * cos(radians).toFloat(),
        y = center.y + radius * sin(radians).toFloat()
    )
}

fun toScreenDegree(position: Position, gestureDegree: Float): Float {
    return when (position) {
        Position.Left -> gestureDegree - 90f
        Position.Right -> 270f - gestureDegree
        Position.Bottom -> 180f + gestureDegree
    }
}

fun toGestureDegree(position: Position, offset: Offset, center: Offset): Float {
    val dx = offset.x - center.x
    val dy = offset.y - center.y
    val screenDegree = normalizeDegree(Math.toDegrees(atan2(dy, dx).toDouble()).toFloat())
    val degree = when (position) {
        Position.Left -> {
            val signed = if (screenDegree > 180f) screenDegree - 360f else screenDegree
            signed + 90f
        }
        Position.Right -> 270f - screenDegree
        Position.Bottom -> when {
            screenDegree >= 180f -> screenDegree - 180f
            screenDegree <= 90f -> GESTURE_ANGLE_BASE
            else -> 0f
        }
    }
    return degree.coerceIn(0f, GESTURE_ANGLE_BASE)
}

fun normalizeDegree(degree: Float): Float {
    val normalized = degree % 360f
    return when (normalized < 0f) {
        true -> normalized + 360f
        else -> normalized
    }
}
