package hunoia.luno.ui.component.panel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import hunoia.luno.config.model.GestureDirection
import kotlin.math.sqrt

internal fun actionPanelOrigin(
    parentSize: Size,
    origin: Offset,
    direction: GestureDirection,
    itemSizePx: Float
): Offset {
    if (parentSize.isEmpty()) return origin
    val safePadding = itemSizePx * 1.5f
    val vector = direction.unitVector()
    val x = when {
        vector.x > 0f -> origin.x.coerceAtMost(parentSize.width / 2f)
        vector.x < 0f -> origin.x.coerceAtLeast(parentSize.width / 2f)
        else -> origin.x.coerceIn(safePadding, parentSize.width - safePadding)
    }
    val y = when {
        vector.y > 0f -> origin.y.coerceAtMost(parentSize.height / 2f)
        vector.y < 0f -> origin.y.coerceAtLeast(parentSize.height / 2f)
        else -> origin.y.coerceIn(safePadding, parentSize.height - safePadding)
    }
    return Offset(x, y)
}

internal fun GestureDirection.unitVector(): Offset {
    val diagonal = 1f / sqrt(2f)
    return when (this) {
        GestureDirection.Left -> Offset(-1f, 0f)
        GestureDirection.UpLeft -> Offset(-diagonal, -diagonal)
        GestureDirection.Up -> Offset(0f, -1f)
        GestureDirection.UpRight -> Offset(diagonal, -diagonal)
        GestureDirection.Right -> Offset(1f, 0f)
        GestureDirection.DownRight -> Offset(diagonal, diagonal)
        GestureDirection.Down -> Offset(0f, 1f)
        GestureDirection.DownLeft -> Offset(-diagonal, diagonal)
    }
}

internal fun Offset.coerceInside(parentSize: Size, itemSizePx: Float): Offset {
    val padding = itemSizePx / 2f
    return Offset(
        x = x.coerceIn(padding, parentSize.width - padding),
        y = y.coerceIn(padding, parentSize.height - padding)
    )
}

internal fun Float.coerceSafely(minimumValue: Float, maximumValue: Float): Float {
    return if (minimumValue <= maximumValue) coerceIn(minimumValue, maximumValue) else minimumValue
}

internal fun arcLayerCapacity(radius: Float, itemSizePx: Float, minGapPx: Float, arcLength: Int): Int {
    val minDistance = itemSizePx + minGapPx
    val diameter = radius * 2f
    if (diameter <= minDistance) return 1
    val minAngle = Math.toDegrees(2.0 * kotlin.math.asin((minDistance / diameter).coerceAtMost(1f).toDouble())).toFloat()
    return kotlin.math.floor(arcLength.toFloat() / minAngle).toInt().coerceAtLeast(1) + 1
}

internal fun actionPanelHitContains(
    finger: Offset,
    panelOrigin: Offset,
    targetAnimOffset: Offset,
    itemSizePx: Float
): Boolean {
    val targetCenter = panelOrigin + targetAnimOffset
    val halfSize = itemSizePx / 2f
    return finger.x in (targetCenter.x - halfSize)..(targetCenter.x + halfSize) &&
            finger.y in (targetCenter.y - halfSize)..(targetCenter.y + halfSize)
}
