package hunoia.sideleap.gesture

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import hunoia.sideleap.system.window.rootSize

fun List<GestureButton>.find(offset: Offset, imePadding: Int = 0): GestureButton? {
    return find { it.contains(offset, imePadding) }
}

fun GestureButton.contains(offset: Offset, imePadding: Int = 0): Boolean {
    val bounds = bounds(imePadding)
    return bounds.contains(offset)
}

fun GestureButton.bounds(imePadding: Int = 0): Rect {
    val y = rootSize.height * start
    val topLeft = when (position) {
        Position.Left -> Offset(0f, y - imePadding)
        Position.Right -> Offset((rootSize.width - width).toFloat(), y - imePadding)
        Position.Bottom -> Offset(rootSize.width * start, (rootSize.height - width).toFloat())
    }
    val boundsSize = when (position) {
        Position.Left, Position.Right -> Size(width.toFloat(), rootSize.height * fraction)
        Position.Bottom -> Size(rootSize.width * fraction, width.toFloat())
    }
    return Rect(topLeft, boundsSize)
}

val GestureButton.fraction: Float get() = end - start
